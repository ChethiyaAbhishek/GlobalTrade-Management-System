package com.globaltrade.shipment.remote;

import com.globaltrade.core.dto.ShipmentStatusDTO;
import com.globaltrade.core.exception.ShipmentNotFoundException;

public interface ShipmentFacadeRemote {

    ShipmentStatusDTO getShipmentStatus(String trackingNumber) throws ShipmentNotFoundException;
}
