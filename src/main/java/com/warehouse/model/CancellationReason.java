package com.warehouse.model;

import java.util.Arrays;
import java.util.Optional;

public enum CancellationReason {
    CUSTOMER_CHANGED_REQUEST("CUSTOMER_CHANGED_REQUEST", "Customer changed request"),
    WRONG_CARGO_TYPE("WRONG_CARGO_TYPE", "Wrong cargo type"),
    WRONG_LOCATION("WRONG_LOCATION", "Wrong location"),
    DUPLICATE_REQUEST("DUPLICATE_REQUEST", "Duplicate request"),
    ROBOT_ISSUE("ROBOT_ISSUE", "Robot issue"),
    PACKAGE_NOT_FOUND("PACKAGE_NOT_FOUND", "Package not found"),
    OTHER("OTHER", "Other");

    private final String code;
    private final String displayName;

    CancellationReason(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<CancellationReason> fromCode(String code) {
        return Arrays.stream(values())
                .filter(reason -> reason.code.equals(code))
                .findFirst();
    }

    public static String displayNameFor(String code) {
        return fromCode(code)
                .map(CancellationReason::getDisplayName)
                .orElse(code);
    }
}
