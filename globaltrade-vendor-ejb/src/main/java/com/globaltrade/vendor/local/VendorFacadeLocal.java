package com.globaltrade.vendor.local;

import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.entity.VendorPerformance;
import com.globaltrade.core.exception.VendorValidationException;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface VendorFacadeLocal {
    void createVendor(Vendor vendor) throws VendorValidationException;

    Vendor findById(Long id);

    List<Vendor> findAll();

    List<Vendor> findByCountry(String country);

    Vendor findPreferredVendor();

    void suspendVendor(Long id);

    VendorPerformance recordEvaluation(Long vendorId, double onTimeDeliveryRate,
                                       double defectRate, String evaluationPeriod);
}
