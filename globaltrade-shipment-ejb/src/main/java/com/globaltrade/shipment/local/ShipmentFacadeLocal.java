package com.globaltrade.shipment.local;

import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.entity.ShipmentTrackingEvent;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.enums.ShipmentType;
import com.globaltrade.core.exception.ShipmentNotFoundException;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface ShipmentFacadeLocal {

    void create(Shipment shipment);

    Shipment findByTrackingNumber(String trackingNumber) throws ShipmentNotFoundException;

    void updateStatus(Long shipmentId, ShipmentStatus newStatus) throws ShipmentNotFoundException;

    List<ShipmentTrackingEvent> getTrackingHistory(Long shipmentId);

    List<Shipment> findDelayedShipments();

    List<Shipment> findByType(ShipmentType type);

    void notifyCarrier(Shipment shipment);

    Shipment findById(Long id);

    List<Shipment> findAll();

    long count();
}
