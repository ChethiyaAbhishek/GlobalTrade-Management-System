package com.globaltrade.order.local;

import com.globaltrade.core.dto.OrderStatusDTO;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.exception.InsufficientInventoryException;
import com.globaltrade.core.exception.OrderProcessingException;
import jakarta.ejb.Local;

import java.util.List;
import java.util.Map;

@Local
public interface OrderFacadeLocal {

    Order placeOrder(String customerName, String shippingDestination, boolean international, Map<Long, Integer> itemQuantities)
            throws OrderProcessingException, InsufficientInventoryException;

    Order findById(Long id);

    Order findByOrderNumber(String orderNumber);

    List<Order> findAll();

    long count();

    OrderStatusDTO getOrderStatus(String orderNumber);
}
