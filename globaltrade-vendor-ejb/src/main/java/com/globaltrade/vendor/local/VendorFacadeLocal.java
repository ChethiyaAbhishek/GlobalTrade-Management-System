package com.globaltrade.vendor.local;

import com.globaltrade.core.dto.VendorReportDTO;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.entity.VendorPerformance;
import com.globaltrade.core.exception.VendorValidationException;
import jakarta.ejb.Local;

import java.util.List;
import java.util.Map;

@Local
public interface VendorFacadeLocal {
    void createVendor(Vendor vendor) throws VendorValidationException;

    Vendor findById(Long id);

    List<Vendor> findAll();

    List<Vendor> findByCountry(String country);

    Vendor findPreferredVendor();

    void suspendVendor(Long id);

    String whoAmI();

    VendorReportDTO getVendorReport(Long vendorId);

    void reactivateVendor(Long id);

    Map<Long, VendorPerformance> getLatestPerformancesByVendor();

    VendorPerformance getLatestPerformance(Long vendorId);

    List<Vendor> getMyVendors();

    VendorPerformance recordEvaluation(Long vendorId, double onTimeDeliveryRate,
                                       double defectRate, String evaluationPeriod);
}
