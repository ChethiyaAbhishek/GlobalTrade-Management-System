package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "vendor_performance", indexes = {
        @Index(name = "idx_vendor_performance_vendor", columnList = "vendor_id")
})
@NamedQueries({
        @NamedQuery(name = "VendorPerformance.findByVendor",
                query = "SELECT vp FROM VendorPerformance vp WHERE vp.vendor.id = :vendorId ORDER BY vp.evaluatedAt DESC"),
        @NamedQuery(name = "VendorPerformance.findAllLatestFirst", query = "SELECT vp FROM VendorPerformance vp " +
                "ORDER BY vp.evaluatedAt DESC, vp.id DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class VendorPerformance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluation_period", length = 50)
    private String evaluationPeriod;

    @Column(name = "on_time_delivery_rate")
    private Double onTimeDeliveryRate;

    @Column(name = "defect_rate")
    private Double defectRate;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @PrePersist
    protected void prePersist() {
        this.evaluatedAt = Instant.now();
    }
}
