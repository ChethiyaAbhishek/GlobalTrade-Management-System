package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_alert_type", columnList = "alert_type")
})
@NamedQueries({
        @NamedQuery(name = "Alert.findAll",
                query = "SELECT a FROM Alert a ORDER BY a.processedAt DESC"),
        @NamedQuery(name = "Alert.findByType",
                query = "SELECT a FROM Alert a WHERE a.alertType = :alertType ORDER BY a.processedAt DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class Alert implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 150)
    private String source;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    protected void prePersist(){
        this.processedAt = Instant.now();
    }
}
