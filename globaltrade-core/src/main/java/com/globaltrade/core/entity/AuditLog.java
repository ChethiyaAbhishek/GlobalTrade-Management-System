package com.globaltrade.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_log_timestamp", columnList = "log_timestamp")
})
@NamedQueries({
        @NamedQuery(name = "AuditLog.findAll",
                query = "SELECT a FROM AuditLog a ORDER BY a.timestamp DESC"),
        @NamedQuery(name = "AuditLog.findByUsername",
                query = "SELECT a FROM AuditLog a WHERE a.username = :username ORDER BY a.timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String username;

    @Column(name = "bean_name", length = 150)
    private String beanName;

    @Column(name = "method_name", length = 150)
    private String methodName;

    @Column(length = 1000)
    private String parameters;

    @Column(length = 50)
    private String result;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "log_timestamp")
    private Instant timestamp;

    @PrePersist
    protected void prePersist(){
        this.timestamp = Instant.now();
    }
}
