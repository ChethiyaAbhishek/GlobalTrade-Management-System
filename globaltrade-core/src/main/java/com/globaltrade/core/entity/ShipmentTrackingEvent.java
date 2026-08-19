package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "shipment_tracking_event", indexes = {
        @Index(name = "idx_shipment_tracking_event_shipment", columnList = "shipment_id")
})
@NamedQueries({
        @NamedQuery(name = "ShipmentTrackingEvent.findByShipment",
                query = "SELECT e FROM ShipmentTrackingEvent e WHERE e.shipment.id = :shipmentId ORDER BY e.timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class ShipmentTrackingEvent implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "event_timestamp", nullable = false)
    private Instant timestamp;

    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @PrePersist
    protected void prePersist(){
        this.timestamp = Instant.now();
    }
}
