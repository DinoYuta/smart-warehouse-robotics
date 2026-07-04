package com.warehouse.controller;

import com.warehouse.dto.LiveMapStateDto;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.service.LiveMapStateService;
import com.warehouse.service.MissionService;
import com.warehouse.service.RobotService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StaffLiveMapController {

    private static final List<RobotDisplay> ROBOT_DISPLAYS = List.of(
            new RobotDisplay("picker", "Picker Alpha", "robot-picker", "a"),
            new RobotDisplay("mover", "Mover Beta", "robot-mover", "b"),
            new RobotDisplay("carrier", "Carrier Gamma", "robot-carrier", "c")
    );

    private final MissionService missionService;
    private final RobotService robotService;
    private final LiveMapStateService liveMapStateService;

    public StaffLiveMapController(MissionService missionService,
                                  RobotService robotService,
                                  LiveMapStateService liveMapStateService) {
        this.missionService = missionService;
        this.robotService = robotService;
        this.liveMapStateService = liveMapStateService;
    }

    @GetMapping("/staff/live-map")
    public String showLiveMap(Model model) {
        List<Mission> missions = missionService.getMissionsNewestFirst();
        List<Mission> activeMissions = missionService.getActiveMissions(missions);
        List<Robot> robots = robotService.getRobots();

        List<LiveMapRobotMission> robotMissionFlows = ROBOT_DISPLAYS.stream()
                .map(display -> buildRobotMissionFlow(display, robots, activeMissions))
                .toList();

        model.addAttribute("robotMissionFlows", robotMissionFlows);
        return "staff-live-map";
    }

    @GetMapping("/staff/live-map/state")
    @ResponseBody
    public LiveMapStateDto showLiveMapState() {
        return liveMapStateService.getLiveMapState();
    }

    private LiveMapRobotMission buildRobotMissionFlow(RobotDisplay display,
                                                      List<Robot> robots,
                                                      List<Mission> activeMissions) {
        Robot robot = findDisplayRobot(robots, display.robotName());
        Mission mission = findCurrentMission(activeMissions, robot, display.robotName());
        String focusZoneKey = mission != null
                ? resolveZoneKey(mission.getZone(), display.defaultZoneKey())
                : display.defaultZoneKey();

        return new LiveMapRobotMission(
                display.key(),
                display.robotName(),
                display.colorClass(),
                focusZoneKey,
                mission != null ? normalizeLocationCode(mission.getLocationCode()) : null,
                mission,
                mission != null ? buildTargetLabel(mission) : null,
                resolveActiveStepKey(mission)
        );
    }

    private Robot findDisplayRobot(List<Robot> robots, String robotName) {
        return robots.stream()
                .filter(robot -> textMatches(robot.getName(), robotName))
                .findFirst()
                .orElse(null);
    }

    private Mission findCurrentMission(List<Mission> activeMissions, Robot robot, String displayRobotName) {
        List<Mission> assignedMissions = activeMissions.stream()
                .filter(mission -> isMissionAssignedToRobot(mission, robot, displayRobotName))
                .toList();

        return findOldestMissionWithStatus(assignedMissions, MissionStatus.IN_PROGRESS)
                .or(() -> findOldestMissionWithStatus(assignedMissions, MissionStatus.ASSIGNED))
                .or(() -> findOldestMissionWithStatus(assignedMissions, MissionStatus.PENDING))
                .or(() -> findOldestMissionWithStatus(assignedMissions, MissionStatus.WAITING_CONFIRMATION))
                .orElse(null);
    }

    private Optional<Mission> findOldestMissionWithStatus(List<Mission> missions, MissionStatus status) {
        return missions.stream()
                .filter(mission -> mission.getStatus() == status)
                .min(Comparator
                        .comparing(Mission::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Mission::getId, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private boolean isMissionAssignedToRobot(Mission mission, Robot robot, String displayRobotName) {
        if (mission.getAssignedRobotId() != null
                && robot != null
                && mission.getAssignedRobotId().equals(robot.getId())) {
            return true;
        }

        String assignedRobotName = mission.getAssignedRobotName();
        if (isBlank(assignedRobotName)) {
            return false;
        }

        String normalizedAssignedName = normalize(assignedRobotName);
        if (normalizedAssignedName.contains(normalize(displayRobotName))) {
            return true;
        }

        return robot != null
                && !isBlank(robot.getCode())
                && normalizedAssignedName.contains(normalize(robot.getCode()));
    }

    private String resolveActiveStepKey(Mission mission) {
        if (mission == null) {
            return null;
        }

        MissionStatus status = mission.getStatus();
        if (status == MissionStatus.PENDING || status == MissionStatus.ASSIGNED) {
            return "assigned";
        }
        if (status == MissionStatus.IN_PROGRESS) {
            return "move";
        }
        if (status == MissionStatus.WAITING_CONFIRMATION) {
            return "returned";
        }
        return null;
    }

    private String buildTargetLabel(Mission mission) {
        String zone = isBlank(mission.getZone()) ? "selected zone" : mission.getZone();
        if (isBlank(mission.getLocationCode())) {
            return zone;
        }
        return zone + " - " + normalizeLocationCode(mission.getLocationCode());
    }

    private String resolveZoneKey(String zone, String fallbackZoneKey) {
        String normalizedZone = normalize(zone);
        if (normalizedZone.endsWith("a") || normalizedZone.contains("zone a")) {
            return "a";
        }
        if (normalizedZone.endsWith("b") || normalizedZone.contains("zone b")) {
            return "b";
        }
        if (normalizedZone.endsWith("c") || normalizedZone.contains("zone c")) {
            return "c";
        }
        return fallbackZoneKey;
    }

    private boolean textMatches(String value, String expected) {
        return normalize(value).equals(normalize(expected));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private String normalizeLocationCode(String value) {
        return isBlank(value) ? null : value.trim().toUpperCase(Locale.US);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record RobotDisplay(String key,
                                String robotName,
                                String colorClass,
                                String defaultZoneKey) {
    }

    public record LiveMapRobotMission(String key,
                                      String robotName,
                                      String colorClass,
                                      String focusZoneKey,
                                      String locationCode,
                                      Mission mission,
                                      String targetText,
                                      String activeStepKey) {

        public boolean hasMission() {
            return mission != null;
        }
    }
}
