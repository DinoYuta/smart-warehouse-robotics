package com.warehouse.service;

import com.warehouse.dto.MissionExecutionProgressDto;
import com.warehouse.dto.MissionLifecycleResult;
import com.warehouse.dto.MissionRouteStep;
import com.warehouse.dto.StaffPickupRequestDto;
import com.warehouse.model.CancellationReason;
import com.warehouse.model.CargoType;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotMovementMode;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.service.RobotChargingService.ChargingDecision;
import com.warehouse.service.RobotChargingService.ChargingWorkflowResult;
import com.warehouse.service.RobotExecutionBehaviorService.MovementPlan;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MissionService {

    private static final List<String> CARGO_TYPES = CargoType.displayNames();
    private static final List<String> WAREHOUSE_ZONES = List.of("Zone A", "Zone B", "Zone C");
    private static final List<Integer> PRIORITIES = List.of(1, 2, 3);
    private static final Map<String, String> ZONE_BY_CARGO_TYPE = CargoType.zoneByCargoType();
    private static final Map<String, Integer> LOAD_BY_CARGO_TYPE = CargoType.loadByCargoType();
    private static final Map<String, List<String>> LOCATIONS_BY_ZONE = createLocationsByZone();
    private static final DateTimeFormatter REQUEST_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String BASE_STATION_POSITION_KEY = "base-station";
    public static final String COMPLETION_REQUIRES_RETURN_MESSAGE =
            "Mission can only be completed after the robot returns to Base Station.";
    public static final String MISSION_NOT_FOUND_MESSAGE = "Mission not found.";
    public static final String MISSION_ALREADY_COMPLETED_MESSAGE = "Mission is already completed.";
    public static final String CANCELLED_MISSION_CANNOT_BE_COMPLETED_MESSAGE =
            "Cancelled missions cannot be completed.";
    public static final String CANCELLATION_REASON_REQUIRED_MESSAGE =
            "Please select a cancellation reason.";

    private final MissionRepository missionRepository;
    private final RobotRepository robotRepository;
    private final WarehouseRouteService warehouseRouteService;
    private final MissionExecutionProgressService missionExecutionProgressService;
    private final RobotExecutionBehaviorService robotExecutionBehaviorService;
    private final RobotChargingService robotChargingService;
    private final RobotMissionBatteryService robotMissionBatteryService;

    public MissionService(MissionRepository missionRepository,
                          RobotRepository robotRepository,
                          WarehouseRouteService warehouseRouteService,
                          MissionExecutionProgressService missionExecutionProgressService,
                          RobotExecutionBehaviorService robotExecutionBehaviorService,
                          RobotChargingService robotChargingService,
                          RobotMissionBatteryService robotMissionBatteryService) {
        this.missionRepository = missionRepository;
        this.robotRepository = robotRepository;
        this.warehouseRouteService = warehouseRouteService;
        this.missionExecutionProgressService = missionExecutionProgressService;
        this.robotExecutionBehaviorService = robotExecutionBehaviorService;
        this.robotChargingService = robotChargingService;
        this.robotMissionBatteryService = robotMissionBatteryService;
    }

    public Mission createMission(StaffPickupRequestDto pickupRequest) {
        normalize(pickupRequest);
        List<String> validationErrors = validatePickupRequest(pickupRequest);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", validationErrors));
        }

        String confirmedZone = ZONE_BY_CARGO_TYPE.get(pickupRequest.getCargoType());
        pickupRequest.setWarehouseZone(confirmedZone);

        Mission mission = new Mission(
                resolveRequestCode(pickupRequest),
                pickupRequest.getCustomerName(),
                pickupRequest.getCargoType(),
                confirmedZone,
                pickupRequest.getLocationCode(),
                pickupRequest.getPriority(),
                MissionStatus.PENDING,
                pickupRequest.getNotes()
        );

        return missionRepository.save(mission);
    }

    @Transactional(readOnly = true)
    public List<Mission> getMissionsNewestFirst() {
        return missionRepository.findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Mission> findMissionById(Long missionId) {
        return missionRepository.findByIdAndDeletedAtIsNull(missionId);
    }

    public List<Mission> getActiveMissions(List<Mission> missions) {
        return missions.stream()
                .filter(mission -> !mission.isDeleted())
                .filter(mission -> isActiveStatus(mission.getStatus()))
                .toList();
    }

    public List<Mission> getHistoryMissions(List<Mission> missions) {
        return missions.stream()
                .filter(mission -> !mission.isDeleted())
                .filter(mission -> mission.getStatus() == MissionStatus.COMPLETED
                        || mission.getStatus() == MissionStatus.CANCELLED)
                .toList();
    }

    public List<Mission> getCompletedMissions(List<Mission> missions) {
        return missions.stream()
                .filter(mission -> !mission.isDeleted())
                .filter(mission -> mission.getStatus() == MissionStatus.COMPLETED)
                .toList();
    }

    public List<Mission> getCancelledMissions(List<Mission> missions) {
        return missions.stream()
                .filter(mission -> !mission.isDeleted())
                .filter(mission -> mission.getStatus() == MissionStatus.CANCELLED)
                .toList();
    }

    public MissionLifecycleResult completeMission(Long missionId) {
        Mission mission = missionRepository.findByIdAndDeletedAtIsNull(missionId)
                .orElseThrow(() -> new IllegalArgumentException(MISSION_NOT_FOUND_MESSAGE));
        if (mission.getStatus() == MissionStatus.COMPLETED) {
            throw new IllegalArgumentException(MISSION_ALREADY_COMPLETED_MESSAGE);
        }
        if (mission.getStatus() == MissionStatus.CANCELLED) {
            throw new IllegalArgumentException(CANCELLED_MISSION_CANNOT_BE_COMPLETED_MESSAGE);
        }
        if (!isCompletionCandidateStatus(mission.getStatus())) {
            throw new IllegalArgumentException(COMPLETION_REQUIRES_RETURN_MESSAGE);
        }
        if (!isMissionReadyForCompletion(mission)) {
            throw new IllegalArgumentException(COMPLETION_REQUIRES_RETURN_MESSAGE);
        }

        MissionLifecycleResult returnLifecycle = MissionStatus.IN_PROGRESS == mission.getStatus()
                ? markReturnedToBaseInternal(mission)
                : new MissionLifecycleResult(mission, false, 0, 0);
        mission = returnLifecycle.mission();
        mission.setStatus(MissionStatus.COMPLETED);
        mission.setCompletedAt(LocalDateTime.now());
        Mission completedMission = missionRepository.save(mission);
        return new MissionLifecycleResult(
                completedMission,
                returnLifecycle.chargingStarted(),
                returnLifecycle.reassignedMissionCount(),
                returnLifecycle.unassignedMissionCount()
        );
    }

    @Transactional(readOnly = true)
    public boolean isMissionReadyForCompletion(Mission mission) {
        if (mission == null || mission.isDeleted()) {
            return false;
        }
        if (mission.getStatus() == MissionStatus.WAITING_CONFIRMATION) {
            return true;
        }
        if (mission.getStatus() == MissionStatus.ASSIGNED) {
            return mission.hasReturnedToBase();
        }
        return mission.getStatus() == MissionStatus.IN_PROGRESS && hasMissionReturnedToBase(mission);
    }

    public MissionLifecycleResult markReturnedToBase(Long missionId) {
        return markReturnedToBaseInternal(getVisibleMissionOrThrow(missionId));
    }

    public Mission startExecution(Long missionId) {
        Mission mission = getVisibleMissionOrThrow(missionId);
        if (mission.getStatus() != MissionStatus.ASSIGNED) {
            throw new IllegalArgumentException("Only ASSIGNED missions can start execution.");
        }
        if (!hasAssignedRobot(mission)) {
            throw new IllegalArgumentException("Mission must have an assigned robot before execution can start.");
        }
        if (!mission.isExecutionNotStarted()) {
            throw new IllegalArgumentException("Only missions with NOT_STARTED execution can start execution.");
        }
        warehouseRouteService.buildExecutionRoute(mission);

        mission.setStatus(MissionStatus.IN_PROGRESS);
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setExecutionStartedAt(LocalDateTime.now());
        robotMissionBatteryService.captureExecutionStartBattery(mission);
        mission.setCurrentPositionKey(BASE_STATION_POSITION_KEY);
        mission.setPickupReachedAt(null);
        mission.setReturnedAt(null);
        return missionRepository.save(mission);
    }

    public MissionLifecycleResult stopMission(Long missionId,
                                              String cancellationReasonCode,
                                              String cancellationNote,
                                              String cancelledBy) {
        Mission mission = getVisibleMissionOrThrow(missionId);
        if (!mission.isStoppableMission()) {
            throw new IllegalArgumentException(
                    "Only PENDING, ASSIGNED, IN_PROGRESS, or WAITING_CONFIRMATION missions can be stopped."
            );
        }
        String normalizedReasonCode = trimToNull(cancellationReasonCode);
        if (normalizedReasonCode == null || CancellationReason.fromCode(normalizedReasonCode).isEmpty()) {
            throw new IllegalArgumentException(CANCELLATION_REASON_REQUIRED_MESSAGE);
        }

        ChargingDecision chargingDecision = mission.getStatus() == MissionStatus.IN_PROGRESS
                ? robotChargingService.prepareChargingDecision(mission)
                : null;
        mission.setStatus(MissionStatus.CANCELLED);
        mission.setCancelledAt(LocalDateTime.now());
        // Cancellation reason is stored so managers can review operational issues.
        mission.setCancellationReasonCode(normalizedReasonCode);
        mission.setCancellationNote(trimToNull(cancellationNote));
        mission.setCancelledBy(trimToNull(cancelledBy));
        Mission stoppedMission = missionRepository.save(mission);
        ChargingWorkflowResult chargingWorkflow = chargingDecision != null
                ? robotChargingService.updateRobotAvailabilityAfterMissionReturn(stoppedMission, chargingDecision)
                : ChargingWorkflowResult.none();
        return new MissionLifecycleResult(
                stoppedMission,
                chargingWorkflow.chargingStarted(),
                chargingWorkflow.reassignedMissionCount(),
                chargingWorkflow.unassignedMissionCount()
        );
    }

    public Mission deleteStoppedMission(Long missionId) {
        Mission mission = getVisibleMissionOrThrow(missionId);
        if (!mission.isDeletableMission()) {
            throw new IllegalArgumentException("Only CANCELLED missions can be deleted.");
        }

        mission.setDeletedAt(LocalDateTime.now());
        return missionRepository.save(mission);
    }

    public List<String> validatePickupRequest(StaffPickupRequestDto pickupRequest) {
        List<String> errors = new ArrayList<>();
        String expectedZone = null;

        if (isBlank(pickupRequest.getRequestCode()) && isBlank(pickupRequest.getCustomerName())) {
            errors.add("Request code or customer name is required.");
        }
        if (!CARGO_TYPES.contains(pickupRequest.getCargoType())) {
            errors.add("Cargo type must be Small Cargo, Medium Cargo, or Large Cargo.");
        } else {
            expectedZone = ZONE_BY_CARGO_TYPE.get(pickupRequest.getCargoType());
            if (!isBlank(pickupRequest.getWarehouseZone()) && !expectedZone.equals(pickupRequest.getWarehouseZone())) {
                errors.add(pickupRequest.getCargoType() + " must be assigned to " + expectedZone + ".");
            }
        }
        if (isBlank(pickupRequest.getLocationCode())) {
            errors.add("Cargo location is required.");
        } else {
            String zoneForLocationValidation = expectedZone != null ? expectedZone : pickupRequest.getWarehouseZone();
            List<String> validLocations = LOCATIONS_BY_ZONE.get(zoneForLocationValidation);
            if (validLocations == null || !validLocations.contains(pickupRequest.getLocationCode())) {
                errors.add("Cargo location must match the assigned zone.");
            }
        }
        if (!isBlank(pickupRequest.getWarehouseZone()) && !WAREHOUSE_ZONES.contains(pickupRequest.getWarehouseZone())) {
            errors.add("Warehouse zone must be Zone A, Zone B, or Zone C.");
        }
        if (!PRIORITIES.contains(pickupRequest.getPriority())) {
            errors.add("Priority must be 1 = High, 2 = Medium, or 3 = Low.");
        }

        return errors;
    }

    public void normalize(StaffPickupRequestDto pickupRequest) {
        pickupRequest.setRequestCode(trimToNull(pickupRequest.getRequestCode()));
        pickupRequest.setCustomerName(trimToNull(pickupRequest.getCustomerName()));
        pickupRequest.setCargoType(trimToNull(pickupRequest.getCargoType()));
        pickupRequest.setLocationCode(trimToNull(pickupRequest.getLocationCode()));
        pickupRequest.setWarehouseZone(trimToNull(pickupRequest.getWarehouseZone()));
        pickupRequest.setNotes(trimToNull(pickupRequest.getNotes()));
    }

    @Transactional(readOnly = true)
    public List<String> getCargoTypes() {
        return CARGO_TYPES;
    }

    @Transactional(readOnly = true)
    public List<String> getWarehouseZones() {
        return WAREHOUSE_ZONES;
    }

    @Transactional(readOnly = true)
    public List<Integer> getPriorities() {
        return PRIORITIES;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getZoneByCargoType() {
        return ZONE_BY_CARGO_TYPE;
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getLoadByCargoType() {
        return LOAD_BY_CARGO_TYPE;
    }

    @Transactional(readOnly = true)
    public List<CancellationReason> getCancellationReasonOptions() {
        return Arrays.asList(CancellationReason.values());
    }

    @Transactional(readOnly = true)
    public int estimatedLoadPercentForCargoType(String cargoType) {
        return CargoType.estimatedLoadPercentFor(cargoType);
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getLocationsByZone() {
        return LOCATIONS_BY_ZONE;
    }

    private String resolveRequestCode(StaffPickupRequestDto pickupRequest) {
        if (!isBlank(pickupRequest.getRequestCode())) {
            return pickupRequest.getRequestCode();
        }
        return "REQ-" + REQUEST_CODE_FORMATTER.format(LocalDateTime.now());
    }

    private Mission getVisibleMissionOrThrow(Long missionId) {
        return missionRepository.findByIdAndDeletedAtIsNull(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
    }

    private boolean hasAssignedRobot(Mission mission) {
        return mission.getAssignedRobotId() != null || !isBlank(mission.getAssignedRobotName());
    }

    private boolean hasMissionReturnedToBase(Mission mission) {
        if (mission.hasReturnedToBase()) {
            return true;
        }
        if (mission.getStatus() != MissionStatus.IN_PROGRESS || mission.getExecutionStartedAt() == null) {
            return false;
        }

        try {
            List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);
            MovementPlan movementPlan = findAssignedRobot(mission)
                    .map(robot -> robotExecutionBehaviorService.movementPlanFor(mission, robot))
                    .orElse(new MovementPlan(RobotMovementMode.NORMAL, RobotMovementMode.NORMAL));
            MissionExecutionProgressDto progress = missionExecutionProgressService.calculateProgress(
                    mission,
                    route,
                    movementPlan.outboundMovementMode(),
                    movementPlan.returnMovementMode()
            );
            return progress.executionStep() == MissionExecutionStep.RETURNED_TO_BASE;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private Optional<Robot> findAssignedRobot(Mission mission) {
        if (mission.getAssignedRobotId() != null) {
            return robotRepository.findById(mission.getAssignedRobotId());
        }

        String assignedRobotName = mission.getAssignedRobotName();
        if (isBlank(assignedRobotName)) {
            return Optional.empty();
        }

        String normalizedAssignedRobotName = normalize(assignedRobotName);
        return robotRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(robot -> isAssignedRobotMatch(normalizedAssignedRobotName, robot))
                .findFirst();
    }

    private boolean isAssignedRobotMatch(String normalizedAssignedRobotName, Robot robot) {
        if (!isBlank(robot.getName()) && normalizedAssignedRobotName.contains(normalize(robot.getName()))) {
            return true;
        }
        return !isBlank(robot.getCode()) && normalizedAssignedRobotName.contains(normalize(robot.getCode()));
    }

    private MissionLifecycleResult markReturnedToBaseInternal(Mission mission) {
        if (mission.getStatus() == MissionStatus.COMPLETED || mission.getStatus() == MissionStatus.CANCELLED) {
            return new MissionLifecycleResult(mission, false, 0, 0);
        }
        if (mission.getStatus() == MissionStatus.WAITING_CONFIRMATION) {
            return new MissionLifecycleResult(mission, false, 0, 0);
        }
        if (mission.getStatus() != MissionStatus.IN_PROGRESS || !hasMissionReturnedToBase(mission)) {
            throw new IllegalArgumentException(COMPLETION_REQUIRES_RETURN_MESSAGE);
        }

        ChargingDecision chargingDecision = robotChargingService.prepareChargingDecision(mission);
        // Robot availability is released after returning to Base; Staff confirmation closes the mission record.
        mission.setStatus(MissionStatus.WAITING_CONFIRMATION);
        mission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        mission.setCurrentPositionKey(BASE_STATION_POSITION_KEY);
        if (mission.getReturnedAt() == null) {
            mission.setReturnedAt(LocalDateTime.now());
        }
        Mission returnedMission = missionRepository.save(mission);
        ChargingWorkflowResult chargingWorkflow = robotChargingService.updateRobotAvailabilityAfterMissionReturn(
                returnedMission,
                chargingDecision
        );
        return new MissionLifecycleResult(
                returnedMission,
                chargingWorkflow.chargingStarted(),
                chargingWorkflow.reassignedMissionCount(),
                chargingWorkflow.unassignedMissionCount()
        );
    }

    private boolean isCompletionCandidateStatus(MissionStatus status) {
        return status == MissionStatus.ASSIGNED
                || status == MissionStatus.IN_PROGRESS
                || status == MissionStatus.WAITING_CONFIRMATION;
    }

    private boolean isActiveStatus(MissionStatus status) {
        return status == MissionStatus.PENDING
                || status == MissionStatus.ASSIGNED
                || status == MissionStatus.IN_PROGRESS
                || status == MissionStatus.WAITING_CONFIRMATION;
    }

    private static Map<String, List<String>> createLocationsByZone() {
        Map<String, List<String>> locationsByZone = new LinkedHashMap<>();
        locationsByZone.put("Zone A", createLocations("A"));
        locationsByZone.put("Zone B", createLocations("B"));
        locationsByZone.put("Zone C", createLocations("C"));
        return locationsByZone;
    }

    private static List<String> createLocations(String prefix) {
        return List.of(
                prefix + "1", prefix + "2", prefix + "3",
                prefix + "4", prefix + "5", prefix + "6",
                prefix + "7", prefix + "8", prefix + "9"
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }
}
