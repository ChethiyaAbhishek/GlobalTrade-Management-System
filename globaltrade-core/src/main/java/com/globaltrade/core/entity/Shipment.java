package com.globaltrade.core.entity;

import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.enums.ShipmentType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "shipment", indexes = {
        @Index(name = "idx_shipment_status", columnList = "status"),
        @Index(name = "idx_shipment_created", columnList = "created_at"),
        @Index(name = "idx_shipment_type", columnList = "shipment_type"),
        @Index(name = "idx_shipment_delayed_shipments", columnList = "status, estimated_delivery"),
})
@NamedQueries({
        @NamedQuery(name = "Shipment.findByStatus",
                query = "SELECT s FROM Shipment s WHERE s.status = :status"),
        @NamedQuery(name = "Shipment.findDelayed",
                query = "SELECT s FROM Shipment s WHERE s.status = com.globaltrade.core.enums.ShipmentStatus.DELAYED " +
                        "AND s.estimatedDelivery < :now ORDER BY s.estimatedDelivery"),
        @NamedQuery(name = "Shipment.findByTrackingNumber",
                query = "SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber"),
        @NamedQuery(name = "Shipment.findAll",
                query = "SELECT s FROM Shipment s ORDER BY s.createdAt DESC"),
        @NamedQuery(name = "Shipment.findByType",
                query = "SELECT s FROM Shipment s WHERE s.shipmentType = :type ORDER BY s.createdAt DESC"),
        @NamedQuery(name = "Shipment.findByOrder",
                query = "SELECT s FROM Shipment s WHERE s.order.id = :orderId"),
        @NamedQuery(name = "Shipment.findByPurchaseRequest",
                query = "SELECT s FROM Shipment s WHERE s.purchaseRequest.id = :requestId"),
        @NamedQuery(name = "Shipment.findInboundByVendor",
                query = "SELECT s FROM Shipment s WHERE s.vendor.id = :vendorId " +
                        "AND s.shipmentType = com.globaltrade.core.enums.ShipmentType.INBOUND"),
        @NamedQuery(name = "Shipment.findInternationalOutbound",
                query = "SELECT s FROM Shipment s WHERE s.shipmentType = com.globaltrade.core.enums.ShipmentType.OUTBOUND " +
                        "AND s.order.international = true"),
        @NamedQuery(name = "Shipment.findInternationalOutboundWithoutDocuments",
                query = "SELECT s FROM Shipment s LEFT JOIN CustomsDocument d ON d.shipment.id = s.id " +
                        "WHERE s.shipmentType = com.globaltrade.core.enums.ShipmentType.OUTBOUND " +
                        "AND s.order.international = true AND d.id IS NULL")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_type", nullable = false, length = 20)
    private ShipmentType shipmentType;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "current_location", length = 100)
    private String currentLocation;

    @Column(name = "route_description", length = 300)
    private String routeDescription;

    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDate actualDelivery;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @PrePersist
    protected void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = ShipmentStatus.PENDING;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
