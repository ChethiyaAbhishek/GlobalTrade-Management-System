package com.globaltrade.core.entity;

import com.globaltrade.core.enums.PurchaseRequestStatus;
import com.globaltrade.core.enums.PurchaseRequestTriggerType;
import com.globaltrade.core.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_request", indexes = {
        @Index(name = "idx_pr_status", columnList = "request_status"),
        @Index(name = "idx_pr_trigger_type", columnList = "trigger_type")
})
@NamedQueries({
        @NamedQuery(name = "PurchaseRequest.findAll",
                query = "SELECT p FROM PurchaseRequest p ORDER BY p.createdAt DESC"),
        @NamedQuery(name = "PurchaseRequest.findByStatus",
                query = "SELECT p FROM PurchaseRequest p WHERE p.requestStatus = :status ORDER BY p.createdAt ASC"),
        @NamedQuery(name = "PurchaseRequest.findByItem",
                query = "SELECT p FROM PurchaseRequest p WHERE p.inventoryItem.id = :itemId ORDER BY p.createdAt DESC"),
        @NamedQuery(name = "PurchaseRequest.findByItemAndStatus",
                query = "SELECT p FROM PurchaseRequest p WHERE p.inventoryItem.id = :itemId AND p.requestStatus = :status ORDER BY p.createdAt DESC"),
        @NamedQuery(name = "PurchaseRequest.findByTriggerType",
                query = "SELECT p FROM PurchaseRequest p WHERE p.triggerType = :triggerType ORDER BY p.createdAt DESC"),
        @NamedQuery(name = "PurchaseRequest.countOpenByItem", query = "SELECT COUNT(p) FROM PurchaseRequest p " +
                "WHERE p.inventoryItem.id = :itemId " +
                "AND p.requestStatus IN (" +
                "com.globaltrade.core.enums.PurchaseRequestStatus.PENDING, " +
                "com.globaltrade.core.enums.PurchaseRequestStatus.APPROVED, " +
                "com.globaltrade.core.enums.PurchaseRequestStatus.ORDERED)")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "req_qty", nullable = false)
    private int requestedQty;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 30)
    private PurchaseRequestStatus requestStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private PurchaseRequestTriggerType triggerType;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.requestStatus == null) {
            this.requestStatus = PurchaseRequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = Instant.now();
    }

}
