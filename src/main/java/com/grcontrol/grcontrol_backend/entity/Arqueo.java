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
 * Entidad Arqueo - Cuadre de caja al cierre de turno
 */
@Entity
@Table(name = "arqueos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "session")
public class Arqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "arqueo_id", unique = true, length = 100)
    private String arqueoId; // ID generado por el cliente

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ShiftSession session;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double efectivo = 0.0;

    @Column(
        name = "tarjeta_credito",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double tarjetaCredito = 0.0;

    @Column(
        name = "tarjeta_debito",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double tarjetaDebito = 0.0;

    @Column(
        name = "vale_interno",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double valeInterno = 0.0;

    @Column(
        name = "deposito",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double deposito = 0.0;

    @Column(
        name = "total_cash",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double totalCash;

    @Column(
        name = "total_sales",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double totalSales;

    @Column(
        name = "difference",
        nullable = false,
        columnDefinition = "DECIMAL(10,2)"
    )
    private Double difference; // totalSales - totalCash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArqueoStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "arqueo_timestamp", nullable = false)
    private LocalDateTime arqueoTimestamp;

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

    public enum ArqueoStatus {
        OPEN,
        BALANCED,
        UNBALANCED,
        APPROVED,
    }

    public enum SyncStatus {
        PENDING,
        SYNCED,
        ERROR,
    }
}
