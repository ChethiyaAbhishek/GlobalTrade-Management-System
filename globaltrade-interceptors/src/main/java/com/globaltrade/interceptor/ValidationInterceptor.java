package com.globaltrade.interceptor;

import com.globaltrade.core.annotations.Validated;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.exception.VendorValidationException;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Validated
@Priority(Interceptor.Priority.APPLICATION + 100)
public class ValidationInterceptor {

    @AroundInvoke
    public Object validateVendorData(InvocationContext ctx) throws Exception {

        Object[] params = ctx.getParameters();

        for (Object param : params) {
            if (param instanceof Vendor vendor) {
                validateVendor(vendor);
            }
        }
        return ctx.proceed();
    }

    private void validateVendor(Vendor vendor) throws VendorValidationException {

        if (vendor.getName() == null || vendor.getName().trim().isEmpty()) {
            throw new VendorValidationException("Vendor name is required");
        }

        if (vendor.getCountry() == null || vendor.getCountry().trim().isEmpty()) {
            throw new VendorValidationException("Vendor country is required");
        }

        if (vendor.getContactEmail() != null && !vendor.getContactEmail().contains("@")) {
            throw new VendorValidationException("Invalid email format");
        }
    }
}
