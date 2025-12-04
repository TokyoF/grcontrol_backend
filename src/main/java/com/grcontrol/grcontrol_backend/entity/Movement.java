package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Movement - Movimientos de caja (VISA, depósitos, vales, etc.)
 */
@Entity
@Table(name = "movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movement_id", unique = true, length = 100)
    private String movementId; // ID generado por el cliente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ShiftSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "movement_timestamp", nullable = false)
    private LocalDateTime movementTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 20)
    private SyncStatus syncStatus = SyncStatus.SYNCED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentMethod {
        EFECTIVO,
        TARJETA_CREDITO,
        TARJETA_DEBITO,
        VALE_INTERNO,
        DEPOSITO,
    }

    public enum SyncStatus {
        PENDING,
        SYNCED,
        ERROR,
    }
}
