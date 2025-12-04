package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.FuelPriceDTO;
import com.grcontrol.grcontrol_backend.entity.FuelPriceHistory;
import com.grcontrol.grcontrol_backend.entity.Station;
import com.grcontrol.grcontrol_backend.entity.User;
import com.grcontrol.grcontrol_backend.repository.FuelPriceHistoryRepository;
import com.grcontrol.grcontrol_backend.repository.StationRepository;
import com.grcontrol.grcontrol_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de precios de combustibles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FuelPriceService {

    private final FuelPriceHistoryRepository priceRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    // ==================== CONSULTAS ====================

    /**
     * Obtener precio actual de un combustible
     */
    @Transactional(readOnly = true)
    public FuelPriceDTO.CurrentPriceResponse getCurrentPrice(Long stationId, String fuelType) {
        log.info("Obteniendo precio actual de {} en estación {}", fuelType, stationId);
        
        FuelPriceHistory.FuelType type = FuelPriceHistory.FuelType.valueOf(fuelType);
        FuelPriceHistory price = priceRepository.findCurrentPrice(stationId, type)
            .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró precio actual para " + fuelType + " en estación " + stationId));
        
        return toCurrentPriceResponse(price);
    }

    /**
     * Obtener todos los precios actuales de una estación
     */
    @Transactional(readOnly = true)
    public FuelPriceDTO.StationCurrentPricesResponse getAllCurrentPrices(Long stationId) {
        log.info("Obteniendo todos los precios actuales de estación {}", stationId);
        
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Estación no encontrada: " + stationId));
        
        List<FuelPriceHistory> prices = priceRepository.findAllCurrentPrices(stationId);
        
        List<FuelPriceDTO.CurrentPriceResponse> priceResponses = prices.stream()
            .map(this::toCurrentPriceResponse)
            .collect(Collectors.toList());
        
        return new FuelPriceDTO.StationCurrentPricesResponse(
            stationId,
            station.getName(),
            priceResponses,
            LocalDateTime.now()
        );
    }

    /**
     * Obtener precio vigente en una fecha específica
     */
    @Transactional(readOnly = true)
    public BigDecimal getPriceAt(Long stationId, String fuelType, LocalDateTime dateTime) {
        log.debug("Obteniendo precio de {} en estación {} para fecha {}", 
            fuelType, stationId, dateTime);
        
        FuelPriceHistory.FuelType type = FuelPriceHistory.FuelType.valueOf(fuelType);
        FuelPriceHistory price = priceRepository.findPriceAt(stationId, type, dateTime)
            .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró precio para " + fuelType + " en fecha " + dateTime));
        
        return price.getPricePerGallon();
    }

    /**
     * Obtener historial de precios de un combustible
     */
    @Transactional(readOnly = true)
    public List<FuelPriceDTO.PriceHistoryResponse> getPriceHistory(
        Long stationId, 
        String fuelType
    ) {
        log.info("Obteniendo historial de precios de {} en estación {}", fuelType, stationId);
        
        FuelPriceHistory.FuelType type = FuelPriceHistory.FuelType.valueOf(fuelType);
        List<FuelPriceHistory> history = priceRepository
            .findHistoryByStationAndFuelType(stationId, type);
        
        return history.stream()
            .map(this::toPriceHistoryResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener cambios de precio en un rango de fechas
     */
    @Transactional(readOnly = true)
    public List<FuelPriceDTO.PriceHistoryResponse> getPriceChangesBetween(
        Long stationId,
        String fuelType,
        LocalDateTime fromDate,
        LocalDateTime toDate
    ) {
        log.info("Obteniendo cambios de precio de {} entre {} y {}", 
            fuelType, fromDate, toDate);
        
        FuelPriceHistory.FuelType type = FuelPriceHistory.FuelType.valueOf(fuelType);
        List<FuelPriceHistory> changes = priceRepository
            .findPriceChangesBetween(stationId, type, fromDate, toDate);
        
        return changes.stream()
            .map(this::toPriceHistoryResponse)
            .collect(Collectors.toList());
    }

    // ==================== OPERACIONES ====================

    /**
     * Actualizar precio de un combustible
     */
    @Transactional
    public FuelPriceDTO.OperationResponse updatePrice(FuelPriceDTO.UpdatePriceRequest request) {
        log.info("Actualizando precio de {} a {} en estación {}", 
            request.fuelType(), request.newPrice(), request.stationId());
        
        // Validaciones
        Station station = stationRepository.findById(request.stationId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Estación no encontrada: " + request.stationId()));
        
        FuelPriceHistory.FuelType fuelType = FuelPriceHistory.FuelType.valueOf(request.fuelType());
        
        if (request.newPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Cerrar precio actual si existe
        priceRepository.findCurrentPrice(request.stationId(), fuelType)
            .ifPresent(currentPrice -> {
                currentPrice.setEffectiveUntil(now);
                priceRepository.save(currentPrice);
                log.info("Precio anterior cerrado: {} -> {}", 
                    currentPrice.getPricePerGallon(), now);
            });
        
        // Crear nuevo precio
        FuelPriceHistory newPrice = new FuelPriceHistory();
        newPrice.setStation(station);
        newPrice.setFuelType(fuelType);
        newPrice.setPricePerGallon(request.newPrice());
        newPrice.setEffectiveFrom(now);
        newPrice.setEffectiveUntil(null); // Precio actual
        newPrice.setNotes(request.notes());
        
        // Obtener usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            userRepository.findByUsername(username).ifPresent(newPrice::setChangedBy);
        }
        
        newPrice = priceRepository.save(newPrice);
        
        log.info("Nuevo precio creado: ID={}, Precio={}", 
            newPrice.getId(), newPrice.getPricePerGallon());
        
        return new FuelPriceDTO.OperationResponse(
            true,
            "Precio actualizado exitosamente",
            newPrice.getId(),
            toCurrentPriceResponse(newPrice)
        );
    }

    /**
     * Establecer precio inicial para un combustible
     */
    @Transactional
    public FuelPriceDTO.OperationResponse setInitialPrice(
        FuelPriceDTO.SetInitialPriceRequest request
    ) {
        log.info("Estableciendo precio inicial de {} en estación {}", 
            request.fuelType(), request.stationId());
        
        Station station = stationRepository.findById(request.stationId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Estación no encontrada: " + request.stationId()));
        
        FuelPriceHistory.FuelType fuelType = FuelPriceHistory.FuelType.valueOf(request.fuelType());
        
        // Verificar que no exista precio actual
        if (priceRepository.findCurrentPrice(request.stationId(), fuelType).isPresent()) {
            throw new IllegalArgumentException(
                "Ya existe un precio actual para " + request.fuelType());
        }
        
        FuelPriceHistory price = new FuelPriceHistory();
        price.setStation(station);
        price.setFuelType(fuelType);
        price.setPricePerGallon(request.price());
        price.setEffectiveFrom(request.effectiveFrom() != null 
            ? request.effectiveFrom() 
            : LocalDateTime.now());
        price.setEffectiveUntil(null);
        price.setNotes(request.notes());
        
        // Obtener usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            userRepository.findByUsername(username).ifPresent(price::setChangedBy);
        }
        
        price = priceRepository.save(price);
        
        return new FuelPriceDTO.OperationResponse(
            true,
            "Precio inicial establecido exitosamente",
            price.getId(),
            toCurrentPriceResponse(price)
        );
    }

    // ==================== HELPERS ====================

    private FuelPriceDTO.CurrentPriceResponse toCurrentPriceResponse(FuelPriceHistory price) {
        return new FuelPriceDTO.CurrentPriceResponse(
            price.getId(),
            price.getFuelType().name(),
            price.getPricePerGallon(),
            price.getEffectiveFrom(),
            price.getStation().getId(),
            price.getStation().getName()
        );
    }

    private FuelPriceDTO.PriceHistoryResponse toPriceHistoryResponse(FuelPriceHistory price) {
        return new FuelPriceDTO.PriceHistoryResponse(
            price.getId(),
            price.getFuelType().name(),
            price.getPricePerGallon(),
            price.getEffectiveFrom(),
            price.getEffectiveUntil(),
            price.getChangedBy() != null ? price.getChangedBy().getUsername() : null,
            price.getNotes(),
            price.isCurrent()
        );
    }
}
