package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_item", indexes = {
        @Index(name = "idx_inventory_item_reorder_level", columnList = "quantity, reorder_level")
})
@NamedQueries({
        @NamedQuery(name = "InventoryItem.findBelowReorderLevel",
                query = "SELECT i FROM InventoryItem i WHERE i.quantity < i.reorderLevel"),
        @NamedQuery(name = "InventoryItem.findBySku",
                query = "SELECT i FROM InventoryItem i WHERE i.sku = :sku"),
        @NamedQuery(name = "InventoryItem.findAll",
                query = "SELECT i FROM InventoryItem i ORDER BY i.name")
})
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @PrePersist
    protected void prePersist(){
        this.reorderLevel = 10;
    }

}
