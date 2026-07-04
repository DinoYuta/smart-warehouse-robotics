package com.warehouse.dto;

import com.warehouse.model.CustomerPickupCode;
import com.warehouse.model.Mission;

public record CustomerPickupCodeProcessResult(CustomerPickupCode pickupCode, Mission mission) {
}
