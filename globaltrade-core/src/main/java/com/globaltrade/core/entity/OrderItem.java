package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id")
})
//@NamedQueries({
//        @NamedQuery(name = "Order.findByOrderNumber",
//                query = "SELECT o FROM Order o WHERE o.orderNumber = :orderNumber"),
//        @NamedQuery(name = "Order.findAll",
//                query = "SELECT o FROM Order o ORDER BY o.createdAt DESC"),
//        @NamedQuery(name = "Order.findByStatus",
//                query = "SELECT o FROM Order o WHERE o.status = :status")
//})
@Getter
@Setter
@NoArgsConstructor
public class OrderItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;
}
