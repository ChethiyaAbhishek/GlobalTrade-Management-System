package com.globaltrade.vendor.remote;

import com.globaltrade.core.dto.VendorReportDTO;
import jakarta.ejb.Remote;

@Remote
public interface VendorFacadeRemote {
    VendorReportDTO getVendorReport(Long vendorId);
}
