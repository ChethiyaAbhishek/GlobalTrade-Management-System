package com.globaltrade.core.entity;

import com.globaltrade.core.enums.VendorStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "vendor", indexes = {
        @Index(name = "idx_vendor_status", columnList = "status"),
        @Index(name = "idx_vendor_country", columnList = "country")
})
@NamedQueries({
        @NamedQuery(name = "Vendor.findAll",
                query = "SELECT v FROM Vendor v ORDER BY v.name"),
        @NamedQuery(name = "Vendor.findByStatus",
                query = "SELECT v FROM Vendor v WHERE v.status = :status"),
        @NamedQuery(name = "Vendor.findByCountry",
                query = "SELECT v FROM Vendor v WHERE v.country = :country"),
        @NamedQuery(name = "Vendor.findTopRatedActive",
                query = "SELECT v FROM Vendor v WHERE v.status = com.globaltrade.core.enums.VendorStatus.ACTIVE ORDER BY v.rating DESC"),
        @NamedQuery(name = "Vendor.findByRepresentative",
                query = "SELECT v FROM Vendor v WHERE v.representative.username = :username")
})
@Getter
@Setter
@NoArgsConstructor
public class Vendor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    private Double rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VendorStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rep_user_id")
    private User representative;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void prePersist(){
        this.status = VendorStatus.ACTIVE;
        this.rating = 0.0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void preUpdate(){
        this.updatedAt = Instant.now();
    }
}
