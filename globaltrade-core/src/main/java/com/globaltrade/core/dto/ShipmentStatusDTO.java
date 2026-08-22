package com.globaltrade.core.dto;

import java.io.Serializable;
import java.time.Instant;

public record ShipmentStatusDTO(
        String trackingNo,
        String status,
        String currentLocation,
        Instant lastUpdate
) implements Serializable {
}
