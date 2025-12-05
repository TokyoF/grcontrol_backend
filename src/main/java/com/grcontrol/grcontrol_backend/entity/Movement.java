package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad Movement - Movimientos de caja (VISA, depósitos, vales, etc.)
 */
@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "session")
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    // VISA-specific fields (only for credit/debit card payments)
    @Column(name = "visa_worker_name", length = 100)
    private String visaWorkerName;

    @Column(name = "vehicle_type", length = 10)
    private String vehicleType; // AUTO, MOTO

    @Column(name = "vehicle_brand", length = 50)
    private String vehicleBrand;

    @Column(name = "vehicle_plate", length = 10)
    private String vehiclePlate;

    @Column(name = "vehicle_color", length = 30)
    private String vehicleColor;

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
