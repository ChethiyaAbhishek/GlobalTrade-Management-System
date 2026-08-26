package com.globaltrade.core.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderStatusDTO(
        String orderNumber,
        String customerNam,
        String status,
        BigDecimal totalAmount,
        String shippingDestination,
        Instant createdAt
) implements Serializable {
}
