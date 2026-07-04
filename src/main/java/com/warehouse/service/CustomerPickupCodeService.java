package com.warehouse.service;

import com.warehouse.dto.CustomerPickupCodeFormDto;
import com.warehouse.dto.CustomerPickupCodeProcessResult;
import com.warehouse.dto.StaffPickupRequestDto;
import com.warehouse.model.CargoType;
import com.warehouse.model.CustomerPickupCode;
import com.warehouse.model.CustomerPickupCodeStatus;
import com.warehouse.model.Mission;
import com.warehouse.repository.CustomerPickupCodeRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerPickupCodeService {

    public static final String PICKUP_CODE_NOT_FOUND_MESSAGE = "Pickup code not found.";
    public static final String PICKUP_CODE_USED_MESSAGE = "This pickup code has already been used.";
    public static final String PICKUP_CODE_REQUIRED_MESSAGE = "Pickup code is required.";
    public static final String CUSTOMER_NAME_REQUIRED_MESSAGE = "Customer name is required.";
    public static final String INVALID_CUSTOMER_EMAIL_MESSAGE = "Invalid customer email.";
    public static final String INVALID_CUSTOMER_PHONE_MESSAGE = "Invalid customer phone number.";
    public static final String CARGO_TYPE_REQUIRED_MESSAGE = "Cargo type is required.";
    public static final String PICKUP_LOCATION_REQUIRED_MESSAGE = "Pickup location is required.";

    private static final int CUSTOMER_NAME_MAX_LENGTH = 150;
    private static final int CUSTOMER_EMAIL_MAX_LENGTH = 254;
    private static final int CUSTOMER_PHONE_MAX_LENGTH = 30;
    private static final int NOTE_MAX_LENGTH = 1000;
    private static final int HIGH_PRIORITY = 1;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final char[] CODE_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final CustomerPickupCodeRepository pickupCodeRepository;
    private final MissionService missionService;
    private final MissionProcessingService missionProcessingService;
    private final SecureRandom secureRandom = new SecureRandom();

    public CustomerPickupCodeService(CustomerPickupCodeRepository pickupCodeRepository,
                                     MissionService missionService,
                                     MissionProcessingService missionProcessingService) {
        this.pickupCodeRepository = pickupCodeRepository;
        this.missionService = missionService;
        this.missionProcessingService = missionProcessingService;
    }

    @Transactional(readOnly = true)
    public List<CustomerPickupCode> getRecentPickupCodes() {
        return pickupCodeRepository.findTop20ByOrderByCreatedAtDescIdDesc();
    }

    public CustomerPickupCode createPickupCode(CustomerPickupCodeFormDto form, String createdBy) {
        normalize(form);
        List<String> errors = validateCreateForm(form);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        CustomerPickupCode pickupCode = new CustomerPickupCode(
                generateUniqueCode(),
                form.getCustomerName(),
                form.getCustomerEmail(),
                form.getCustomerPhone(),
                form.getCargoType(),
                form.getPickupLocation(),
                HIGH_PRIORITY,
                CustomerPickupCodeStatus.UNUSED,
                trimToNull(createdBy),
                form.getNote()
        );
        return pickupCodeRepository.save(pickupCode);
    }

    @Transactional(readOnly = true)
    public CustomerPickupCode lookupUnusedCode(String codeInput) {
        CustomerPickupCode pickupCode = findByCodeInput(codeInput);
        if (pickupCode.getStatus() == CustomerPickupCodeStatus.USED) {
            throw new IllegalArgumentException(PICKUP_CODE_USED_MESSAGE);
        }
        return pickupCode;
    }

    public CustomerPickupCodeProcessResult processPickupCode(String codeInput, String usedBy) {
        CustomerPickupCode pickupCode = findByCodeInput(codeInput);
        if (pickupCode.getStatus() == CustomerPickupCodeStatus.USED) {
            throw new IllegalArgumentException(PICKUP_CODE_USED_MESSAGE);
        }

        StaffPickupRequestDto pickupRequest = new StaffPickupRequestDto();
        pickupRequest.setRequestCode(pickupCode.getCode());
        pickupRequest.setCustomerName(pickupCode.getCustomerName());
        pickupRequest.setCargoType(pickupCode.getCargoType());
        pickupRequest.setLocationCode(pickupCode.getPickupLocation());
        pickupRequest.setWarehouseZone(pickupCode.getWarehouseZone());
        pickupRequest.setPriority(HIGH_PRIORITY);
        pickupRequest.setNotes(buildMissionNote(pickupCode));

        Mission savedMission = missionService.createMission(pickupRequest);
        Mission processedMission = missionProcessingService.processPendingMission(savedMission.getId());

        pickupCode.setStatus(CustomerPickupCodeStatus.USED);
        pickupCode.setMissionId(processedMission.getId());
        pickupCode.setUsedBy(trimToNull(usedBy));
        pickupCode.setUsedAt(LocalDateTime.now());
        CustomerPickupCode usedPickupCode = pickupCodeRepository.save(pickupCode);
        return new CustomerPickupCodeProcessResult(usedPickupCode, processedMission);
    }

    public List<String> validateCreateForm(CustomerPickupCodeFormDto form) {
        List<String> errors = new ArrayList<>();
        String customerName = form.getCustomerName();
        String customerEmail = form.getCustomerEmail();
        String customerPhone = form.getCustomerPhone();
        String cargoType = form.getCargoType();
        String pickupLocation = form.getPickupLocation();

        if (isBlank(customerName) || customerName.length() > CUSTOMER_NAME_MAX_LENGTH) {
            errors.add(CUSTOMER_NAME_REQUIRED_MESSAGE);
        }
        if (isBlank(customerEmail)
                || customerEmail.length() > CUSTOMER_EMAIL_MAX_LENGTH
                || !EMAIL_PATTERN.matcher(customerEmail).matches()) {
            errors.add(INVALID_CUSTOMER_EMAIL_MESSAGE);
        }
        if (isBlank(customerPhone)
                || customerPhone.length() > CUSTOMER_PHONE_MAX_LENGTH
                || !isValidVietnamesePhone(customerPhone)) {
            errors.add(INVALID_CUSTOMER_PHONE_MESSAGE);
        }
        if (isBlank(cargoType) || CargoType.fromDisplayName(cargoType).isEmpty()) {
            errors.add(CARGO_TYPE_REQUIRED_MESSAGE);
        }
        if (isBlank(pickupLocation) || !isValidPickupLocation(cargoType, pickupLocation)) {
            errors.add(PICKUP_LOCATION_REQUIRED_MESSAGE);
        }

        return errors;
    }

    public void normalize(CustomerPickupCodeFormDto form) {
        form.setCustomerName(limit(trimToNull(form.getCustomerName()), CUSTOMER_NAME_MAX_LENGTH + 1));
        form.setCustomerEmail(limit(trimToNull(form.getCustomerEmail()), CUSTOMER_EMAIL_MAX_LENGTH + 1));
        form.setCustomerPhone(limit(normalizePhone(form.getCustomerPhone()), CUSTOMER_PHONE_MAX_LENGTH + 1));
        form.setCargoType(trimToNull(form.getCargoType()));
        form.setPickupLocation(toUpperTrimmed(form.getPickupLocation()));
        form.setNote(limit(trimToNull(form.getNote()), NOTE_MAX_LENGTH));
    }

    @Transactional(readOnly = true)
    public List<String> getCargoTypes() {
        return missionService.getCargoTypes();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getZoneByCargoType() {
        return missionService.getZoneByCargoType();
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getLocationsByZone() {
        return missionService.getLocationsByZone();
    }

    private CustomerPickupCode findByCodeInput(String codeInput) {
        String normalizedCode = normalizeCodeInput(codeInput);
        if (normalizedCode == null) {
            throw new IllegalArgumentException(PICKUP_CODE_REQUIRED_MESSAGE);
        }
        return pickupCodeRepository.findByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException(PICKUP_CODE_NOT_FOUND_MESSAGE));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 30; attempt++) {
            String code = "CUS-" + LocalDate.now().getYear() + "-" + randomCodeSegment(6);
            if (!pickupCodeRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate a unique pickup code.");
    }

    private String randomCodeSegment(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)]);
        }
        return builder.toString();
    }

    private String buildMissionNote(CustomerPickupCode pickupCode) {
        String note = "Customer pickup code " + pickupCode.getCode()
                + ". Email: " + pickupCode.getCustomerEmail()
                + ". Phone: " + pickupCode.getCustomerPhone() + ".";
        if (!isBlank(pickupCode.getNote())) {
            note += " Note: " + pickupCode.getNote();
        }
        return note;
    }

    private boolean isValidPickupLocation(String cargoType, String pickupLocation) {
        return CargoType.fromDisplayName(cargoType)
                .map(CargoType::getWarehouseZone)
                .map(zone -> missionService.getLocationsByZone().get(zone))
                .map(locations -> locations.contains(pickupLocation))
                .orElse(false);
    }

    private boolean isValidVietnamesePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return false;
        }
        return phone.chars().distinct().count() > 1;
    }

    private String normalizePhone(String value) {
        String trimmedValue = trimToNull(value);
        if (trimmedValue == null) {
            return null;
        }
        String normalizedValue = trimmedValue
                .replace(" ", "")
                .replace("-", "")
                .replace(".", "")
                .replace("(", "")
                .replace(")", "");
        if (normalizedValue.startsWith("+84")) {
            normalizedValue = "0" + normalizedValue.substring(3);
        } else if (normalizedValue.startsWith("84") && normalizedValue.length() >= 11) {
            normalizedValue = "0" + normalizedValue.substring(2);
        }
        return normalizedValue;
    }

    private String normalizeCodeInput(String value) {
        String normalizedCode = trimToNull(value);
        if (normalizedCode == null) {
            return null;
        }
        return normalizedCode.toUpperCase(Locale.US);
    }

    private String toUpperTrimmed(String value) {
        String trimmedValue = trimToNull(value);
        return trimmedValue == null ? null : trimmedValue.toUpperCase(Locale.US);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
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
}
