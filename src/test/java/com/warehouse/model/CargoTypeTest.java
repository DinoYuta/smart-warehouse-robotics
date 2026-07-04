package com.warehouse.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CargoTypeTest {

    @Test
    void mapsCargoTypesToEstimatedRobotLoadPercentagesAndZones() {
        assertThat(CargoType.estimatedLoadPercentFor("Small Cargo")).isEqualTo(30);
        assertThat(CargoType.estimatedLoadPercentFor("Medium Cargo")).isEqualTo(60);
        assertThat(CargoType.estimatedLoadPercentFor("Large Cargo")).isEqualTo(90);

        assertThat(CargoType.SMALL.getWarehouseZone()).isEqualTo("Zone A");
        assertThat(CargoType.MEDIUM.getWarehouseZone()).isEqualTo("Zone B");
        assertThat(CargoType.LARGE.getWarehouseZone()).isEqualTo("Zone C");
    }
}
