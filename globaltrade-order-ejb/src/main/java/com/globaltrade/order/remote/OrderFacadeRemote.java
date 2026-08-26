package com.globaltrade.order.remote;

import com.globaltrade.core.dto.OrderStatusDTO;
import jakarta.ejb.Remote;

@Remote
public interface OrderFacadeRemote {

    OrderStatusDTO getOrderStatus(String orderNumber);

}
