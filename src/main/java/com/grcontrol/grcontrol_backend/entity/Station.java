package com.grcontrol.grcontrol_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad Station - Representa una estación de servicio
 * Cada estación puede tener múltiples islas y administradores
 */
@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"islands", "administrators", "schedules"})
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // "Acobamba", "Tarma", "Paccha", "Santa Ana"

    @Column(length = 200)
    private String address;

    @Column(length = 20)
    private String phone; // Teléfono de la estación

    @Column(length = 100)
    private String coordinates; // lat,lng format

    @Column(name = "osinergmin_code", length = 50)
    private String osinergminCode;

    @Column(nullable = false)
    private Boolean active = true;

    // Relaciones
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Island> islands = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "station_administrators",
        joinColumns = @JoinColumn(name = "station_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> administrators = new HashSet<>();

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftSchedule> schedules = new ArrayList<>();

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

    // Helper methods
    public void addIsland(Island island) {
        islands.add(island);
        island.setStation(this);
    }

    public void removeIsland(Island island) {
        islands.remove(island);
        island.setStation(null);
    }

    public void addAdministrator(User admin) {
        administrators.add(admin);
    }

    public void removeAdministrator(User admin) {
        administrators.remove(admin);
    }

    public void addSchedule(ShiftSchedule schedule) {
        schedules.add(schedule);
        schedule.setStation(this);
    }
}
