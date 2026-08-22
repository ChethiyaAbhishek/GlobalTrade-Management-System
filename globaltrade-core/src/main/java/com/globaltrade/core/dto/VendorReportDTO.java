package com.globaltrade.core.dto;

import java.io.Serializable;

public record VendorReportDTO(
        Long vendorId,
        String vendorName,
        String country,
        Double rating,
        Double onTimeRate,
        Double overallScore
) implements Serializable {
}
