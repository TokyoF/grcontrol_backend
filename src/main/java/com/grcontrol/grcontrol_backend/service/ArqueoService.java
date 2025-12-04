package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.ArqueoDTO;
import com.grcontrol.grcontrol_backend.entity.Arqueo;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import com.grcontrol.grcontrol_backend.repository.ArqueoRepository;
import com.grcontrol.grcontrol_backend.repository.ShiftSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service para gestión de arqueos
 */
@Service
@RequiredArgsConstructor
public class ArqueoService {

    private final ArqueoRepository arqueoRepository;
    private final ShiftSessionRepository shiftSessionRepository;

    /**
     * Crear nuevo arqueo
     */
    @Transactional
    public ArqueoDTO.ArqueoResponse createArqueo(ArqueoDTO.CreateArqueoRequest request) {
        ShiftSession session = shiftSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        // Validar que no exista arqueo para esta sesión
        if (arqueoRepository.existsBySession(session)) {
            throw new IllegalArgumentException("Ya existe un arqueo para esta sesión");
        }

        Arqueo arqueo = new Arqueo();
        arqueo.setArqueoId(UUID.randomUUID().toString());
        arqueo.setSession(session);
        arqueo.setEfectivo(request.efectivo());
        arqueo.setTarjetaCredito(request.tarjetaCredito());
        arqueo.setTarjetaDebito(request.tarjetaDebito());
        arqueo.setValeInterno(request.valeInterno());
        arqueo.setDeposito(request.deposito());
        arqueo.setTotalCash(request.totalCash());
        arqueo.setTotalSales(request.totalSales());
        arqueo.setDifference(request.difference());
        arqueo.setStatus(Arqueo.ArqueoStatus.valueOf(request.status()));
        arqueo.setNotes(request.notes());
        arqueo.setArqueoTimestamp(LocalDateTime.now());
        arqueo.setSyncStatus(Arqueo.SyncStatus.SYNCED);

        Arqueo saved = arqueoRepository.save(arqueo);
        return mapToArqueoResponse(saved);
    }

    /**
     * Obtener arqueo por ID
     */
    @Transactional(readOnly = true)
    public ArqueoDTO.ArqueoResponse getArqueoById(Long id) {
        Arqueo arqueo = arqueoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Arqueo no encontrado"));
        return mapToArqueoResponse(arqueo);
    }

    /**
     * Obtener arqueo por sesión
     */
    @Transactional(readOnly = true)
    public ArqueoDTO.ArqueoResponse getArqueoBySession(Long sessionId) {
        Arqueo arqueo = arqueoRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Arqueo no encontrado para esta sesión"));
        return mapToArqueoResponse(arqueo);
    }

    /**
     * Actualizar arqueo
     */
    @Transactional
    public ArqueoDTO.ArqueoResponse updateArqueo(Long id, ArqueoDTO.UpdateArqueoRequest request) {
        Arqueo arqueo = arqueoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Arqueo no encontrado"));

        if (request.efectivo() != null) arqueo.setEfectivo(request.efectivo());
        if (request.tarjetaCredito() != null) arqueo.setTarjetaCredito(request.tarjetaCredito());
        if (request.tarjetaDebito() != null) arqueo.setTarjetaDebito(request.tarjetaDebito());
        if (request.valeInterno() != null) arqueo.setValeInterno(request.valeInterno());
        if (request.deposito() != null) arqueo.setDeposito(request.deposito());
        if (request.totalCash() != null) arqueo.setTotalCash(request.totalCash());
        if (request.totalSales() != null) arqueo.setTotalSales(request.totalSales());
        if (request.difference() != null) arqueo.setDifference(request.difference());
        if (request.status() != null) arqueo.setStatus(Arqueo.ArqueoStatus.valueOf(request.status()));
        if (request.notes() != null) arqueo.setNotes(request.notes());

        Arqueo saved = arqueoRepository.save(arqueo);
        return mapToArqueoResponse(saved);
    }

    /**
     * Aprobar arqueo
     */
    @Transactional
    public void approveArqueo(Long id) {
        Arqueo arqueo = arqueoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Arqueo no encontrado"));

        arqueo.setStatus(Arqueo.ArqueoStatus.APPROVED);
        arqueoRepository.save(arqueo);
    }

    /**
     * Mapear a ArqueoResponse
     */
    private ArqueoDTO.ArqueoResponse mapToArqueoResponse(Arqueo arqueo) {
        return new ArqueoDTO.ArqueoResponse(
            arqueo.getId(),
            arqueo.getArqueoId(),
            arqueo.getSession().getId(),
            arqueo.getEfectivo(),
            arqueo.getTarjetaCredito(),
            arqueo.getTarjetaDebito(),
            arqueo.getValeInterno(),
            arqueo.getDeposito(),
            arqueo.getTotalCash(),
            arqueo.getTotalSales(),
            arqueo.getDifference(),
            arqueo.getStatus().name(),
            arqueo.getNotes(),
            arqueo.getArqueoTimestamp(),
            arqueo.getSyncStatus().name(),
            arqueo.getCreatedAt(),
            arqueo.getUpdatedAt()
        );
    }
}
