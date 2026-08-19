package com.globaltrade.core.entity;

import com.globaltrade.core.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "shipment", indexes = {
        @Index(name = "idx_shipment_status", columnList = "status"),
        @Index(name = "idx_shipment_created", columnList = "created_at")
})
@NamedQueries({
        @NamedQuery(name = "Shipment.findByStatus",
                query = "SELECT s FROM Shipment s WHERE s.status = :status"),
        @NamedQuery(name = "Shipment.findDelayed",
                query = "SELECT s FROM Shipment s WHERE s.status = com.globaltrade.core.enums.ShipmentStatus.DELAYED"),
        @NamedQuery(name = "Shipment.findByTrackingNumber",
                query = "SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber"),
        @NamedQuery(name = "Shipment.findAll",
                query = "SELECT s FROM Shipment s ORDER BY s.createdAt DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class Shipment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "current_location", length = 100)
    private String currentLocation;

    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @PrePersist
    protected void prePersist(){
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = ShipmentStatus.PENDING;
    }

    @PreUpdate
    protected void preUpdate(){
        this.updatedAt = Instant.now();
    }
}
