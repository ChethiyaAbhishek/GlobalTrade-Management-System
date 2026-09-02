package com.globaltrade.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentStatusDTO implements Serializable {

    private String trackingNo;
    private String status;
    private String currentLocation;
    private Instant lastUpdate;
}
