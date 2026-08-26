package com.globaltrade.core.entity;

import com.globaltrade.core.enums.DocumentStatus;
import com.globaltrade.core.enums.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customs_document", indexes = {
        @Index(name = "idx_customs_document_deadline", columnList = "deadline, status")
})
@NamedQueries({
        @NamedQuery(name = "CustomsDocument.findApproachingDeadlines",
                query = "SELECT d FROM CustomsDocument d WHERE d.deadline <= :deadline AND d.status <> com.globaltrade.core.enums.DocumentStatus.APPROVED ORDER BY d.deadline ASC"),
        @NamedQuery(name = "CustomsDocument.findByShipment",
                query = "SELECT d FROM CustomsDocument d WHERE d.shipment.id = :shipmentId"),
        @NamedQuery(name = "CustomsDocument.findAwaitingClearance",
                query = "SELECT d FROM CustomsDocument d WHERE d.status = com.globaltrade.core.enums.DocumentStatus.SUBMITTED ORDER BY d.filedAt ASC"),
})
@Getter
@Setter
@NoArgsConstructor
public class CustomsDocument implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private DocumentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Column(name = "filed_at")
    private Instant filedAt;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @PrePersist
    protected void prePersist() {
        this.status = DocumentStatus.DRAFT;
        this.filedAt = Instant.now();
    }

    public CustomsDocument(Shipment shipment, DocumentType type) {
        this.shipment = shipment;
        this.type = type;
    }
}
