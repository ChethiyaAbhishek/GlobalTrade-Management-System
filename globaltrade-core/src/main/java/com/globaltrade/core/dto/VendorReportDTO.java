package com.globaltrade.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VendorReportDTO implements Serializable {

    private Long vendorId;
    private String vendorName;
    private String country;
    private Double rating;
    private Double onTimeRate;
    private Double overallScore;
}
