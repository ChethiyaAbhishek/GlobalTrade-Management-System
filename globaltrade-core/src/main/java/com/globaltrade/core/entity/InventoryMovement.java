package com.globaltrade.core.entity;

import com.globaltrade.core.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_movement", indexes = {
        @Index(name = "idx_inventory_mov_type", columnList = "movement_type")
})
@NamedQueries({
        @NamedQuery(name = "InventoryMovement.findAll",
                query = "SELECT m FROM InventoryMovement m ORDER BY m.movementTimestamp DESC"),
        @NamedQuery(name = "InventoryMovement.findByItem",
                query = "SELECT m FROM InventoryMovement m WHERE m.inventoryItem.id = :itemId ORDER BY m.movementTimestamp DESC"),
        @NamedQuery(name = "InventoryMovement.findByType",
                query = "SELECT m FROM InventoryMovement m WHERE m.movementType = :type ORDER BY m.movementTimestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "movement_timestamp")
    private Instant movementTimestamp;

    @PrePersist
    protected void prePersist() {
        if (this.movementTimestamp == null) {
            this.movementTimestamp = Instant.now();
        }
    }
}
