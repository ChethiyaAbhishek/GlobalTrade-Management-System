package com.globaltrade.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusDTO implements Serializable {
    private String orderNumber;
    private String customerNam;
    private String status;
    private BigDecimal totalAmount;
    private String shippingDestination;
    private Instant createdAt;
}
