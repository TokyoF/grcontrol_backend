package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Station
 */
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    /**
     * Buscar estación por nombre
     */
    Optional<Station> findByName(String name);

    /**
     * Buscar todas las estaciones activas
     */
    List<Station> findByActiveTrue();

    /**
     * Buscar estación por ID con sus islas precargadas
     */
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.islands WHERE s.id = :stationId")
    Optional<Station> findByIdWithIslands(@Param("stationId") Long stationId);

    /**
     * Buscar estaciones administradas por un usuario específico
     */
    @Query("SELECT s FROM Station s JOIN s.administrators a WHERE a.id = :adminId")
    List<Station> findByAdministratorId(@Param("adminId") Long adminId);

    /**
     * Verificar si una estación existe por nombre
     */
    boolean existsByName(String name);

    /**
     * Buscar estación por código OSINERGMIN
     */
    Optional<Station> findByOsinergminCode(String osinergminCode);

    /**
     * Buscar estación por ID con sus administradores precargados
     */
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.administrators WHERE s.id = :stationId")
    Optional<Station> findByIdWithAdministrators(@Param("stationId") Long stationId);
}
