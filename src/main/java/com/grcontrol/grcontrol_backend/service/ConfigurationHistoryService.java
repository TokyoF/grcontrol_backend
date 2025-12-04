package com.grcontrol.grcontrol_backend.service;

import com.grcontrol.grcontrol_backend.dto.PumpConfigurationHistoryDTO;
import com.grcontrol.grcontrol_backend.entity.PumpConfigurationHistory;
import com.grcontrol.grcontrol_backend.repository.PumpConfigurationHistoryRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestión de historial de configuración de bombas
 */
@Service
@RequiredArgsConstructor
public class ConfigurationHistoryService {

    private final PumpConfigurationHistoryRepository historyRepository;

    // ==================== QUERY OPERATIONS ====================

    @Transactional(readOnly = true)
    public PumpConfigurationHistoryDTO.ConfigChangeResponse getConfigChange(
        Long id
    ) {
        PumpConfigurationHistory history = historyRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Configuration change not found with id: " + id
                )
            );

        return toConfigChangeResponse(history);
    }

    @Transactional(readOnly = true)
    public PumpConfigurationHistoryDTO.NozzleHistoryResponse getNozzleHistory(
        Long nozzleId
    ) {
        List<PumpConfigurationHistory> changes =
            historyRepository.findByNozzleIdOrderByChangeTimestampDesc(
                nozzleId
            );

        if (changes.isEmpty()) {
            throw new IllegalArgumentException(
                "No history found for nozzle with id: " + nozzleId
            );
        }

        PumpConfigurationHistory firstChange = changes.get(0);

        List<PumpConfigurationHistoryDTO.ConfigChangeResponse> changeResponses =
            changes
                .stream()
                .map(this::toConfigChangeResponse)
                .collect(Collectors.toList());

        return new PumpConfigurationHistoryDTO.NozzleHistoryResponse(
            nozzleId,
            firstChange.getNozzle().getSide().name(),
            firstChange.getNozzle().getFuelType().name(),
            firstChange.getNozzle().getPump().getId(),
            firstChange.getNozzle().getPump().getName(),
            changeResponses,
            changes.size(),
            changes.get(changes.size() - 1).getChangeTimestamp(),
            changes.get(0).getChangeTimestamp()
        );
    }

    @Transactional(readOnly = true)
    public PumpConfigurationHistoryDTO.DateRangeHistoryResponse getHistoryByDateRange(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        List<PumpConfigurationHistory> changes =
            historyRepository.findByChangeTimestampBetween(startDate, endDate);

        List<PumpConfigurationHistoryDTO.ConfigChangeResponse> changeResponses =
            changes
                .stream()
                .map(this::toConfigChangeResponse)
                .collect(Collectors.toList());

        PumpConfigurationHistoryDTO.ChangeStatistics statistics =
            calculateStatistics(changes);

        return new PumpConfigurationHistoryDTO.DateRangeHistoryResponse(
            startDate,
            endDate,
            changeResponses,
            changes.size(),
            statistics
        );
    }

    @Transactional(readOnly = true)
    public PumpConfigurationHistoryDTO.SessionAffectedChangesResponse getSessionAffectedChanges(
        Long sessionId
    ) {
        List<PumpConfigurationHistory> changes =
            historyRepository.findByAffectedSessionId(sessionId);

        List<PumpConfigurationHistoryDTO.ConfigChangeResponse> changeResponses =
            changes
                .stream()
                .map(this::toConfigChangeResponse)
                .collect(Collectors.toList());

        String sessionIdentifier = changes.isEmpty()
            ? ""
            : (changes.get(0).getAffectedSession() != null
                  ? changes.get(0).getAffectedSession().getSessionId()
                  : "");

        LocalDateTime sessionStart = changes.isEmpty()
            ? null
            : (changes.get(0).getAffectedSession() != null
                  ? changes.get(0).getAffectedSession().getStartTime()
                  : null);

        LocalDateTime sessionEnd = changes.isEmpty()
            ? null
            : (changes.get(0).getAffectedSession() != null
                  ? changes.get(0).getAffectedSession().getEndTime()
                  : null);

        return new PumpConfigurationHistoryDTO.SessionAffectedChangesResponse(
            sessionId,
            sessionIdentifier,
            changeResponses,
            changes.size(),
            sessionStart,
            sessionEnd
        );
    }

    @Transactional(readOnly = true)
    public List<
        PumpConfigurationHistoryDTO.ConfigChangeResponse
    > getChangesByType(
        Long nozzleId,
        PumpConfigurationHistory.ConfigChangeType changeType
    ) {
        return historyRepository
            .findByNozzleIdAndChangeTypeOrderByChangeTimestampDesc(
                nozzleId,
                changeType
            )
            .stream()
            .map(this::toConfigChangeResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<
        PumpConfigurationHistoryDTO.ConfigChangeResponse
    > getChangesByUser(Long userId) {
        return historyRepository
            .findByChangedByIdOrderByChangeTimestampDesc(userId)
            .stream()
            .map(this::toConfigChangeResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<
        PumpConfigurationHistoryDTO.ConfigChangeResponse
    > getChangesByPump(Long pumpId) {
        return historyRepository
            .findByPumpId(pumpId)
            .stream()
            .map(this::toConfigChangeResponse)
            .collect(Collectors.toList());
    }

    // ==================== REPORTING ====================

    @Transactional(readOnly = true)
    public PumpConfigurationHistoryDTO.AuditReportResponse generateAuditReport(
        LocalDateTime startDate,
        LocalDateTime endDate,
        String generatedBy
    ) {
        List<PumpConfigurationHistory> changes =
            historyRepository.findByChangeTimestampBetween(startDate, endDate);

        List<PumpConfigurationHistoryDTO.AuditSummary> summaries = changes
            .stream()
            .map(this::toAuditSummary)
            .collect(Collectors.toList());

        return new PumpConfigurationHistoryDTO.AuditReportResponse(
            LocalDateTime.now(),
            startDate,
            endDate,
            summaries,
            changes.size(),
            generatedBy
        );
    }

    // ==================== STATISTICS ====================

    private PumpConfigurationHistoryDTO.ChangeStatistics calculateStatistics(
        List<PumpConfigurationHistory> changes
    ) {
        int totalChanges = changes.size();
        long priceChanges = changes
            .stream()
            .filter(
                c ->
                    c.getChangeType() ==
                    PumpConfigurationHistory.ConfigChangeType.PRICE_CHANGE
            )
            .count();
        long activations = changes
            .stream()
            .filter(
                c ->
                    c.getChangeType() ==
                    PumpConfigurationHistory.ConfigChangeType.ACTIVATION
            )
            .count();
        long deactivations = changes
            .stream()
            .filter(
                c ->
                    c.getChangeType() ==
                    PumpConfigurationHistory.ConfigChangeType.DEACTIVATION
            )
            .count();
        long fuelTypeChanges = changes
            .stream()
            .filter(
                c ->
                    c.getChangeType() ==
                    PumpConfigurationHistory.ConfigChangeType.FUEL_TYPE_CHANGE
            )
            .count();

        // Top changers
        Map<Long, Integer> changerCounts = new HashMap<>();
        for (PumpConfigurationHistory change : changes) {
            if (change.getChangedBy() != null) {
                Long userId = change.getChangedBy().getId();
                changerCounts.put(
                    userId,
                    changerCounts.getOrDefault(userId, 0) + 1
                );
            }
        }

        List<PumpConfigurationHistoryDTO.TopChanger> topChangers = changerCounts
            .entrySet()
            .stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .map(entry -> {
                var user = changes
                    .stream()
                    .filter(
                        c ->
                            c.getChangedBy() != null &&
                            c.getChangedBy().getId().equals(entry.getKey())
                    )
                    .findFirst()
                    .get()
                    .getChangedBy();
                return new PumpConfigurationHistoryDTO.TopChanger(
                    user.getId(),
                    user.getFirstName() + " " + user.getLastName(),
                    entry.getValue()
                );
            })
            .collect(Collectors.toList());

        return new PumpConfigurationHistoryDTO.ChangeStatistics(
            totalChanges,
            (int) priceChanges,
            (int) activations,
            (int) deactivations,
            (int) fuelTypeChanges,
            topChangers
        );
    }

    // ==================== HELPERS ====================

    private PumpConfigurationHistoryDTO.ConfigChangeResponse toConfigChangeResponse(
        PumpConfigurationHistory history
    ) {
        return new PumpConfigurationHistoryDTO.ConfigChangeResponse(
            history.getId(),
            history.getNozzle().getId(),
            history.getNozzle().getSide().name(),
            history.getNozzle().getFuelType().name(),
            history.getNozzle().getPump().getId(),
            history.getNozzle().getPump().getName(),
            history.getNozzle().getPump().getIsland().getId(),
            history.getNozzle().getPump().getIsland().getName(),
            history.getChangeType().name(),
            history.getPreviousValue(),
            history.getNewValue(),
            history.getChangeTimestamp(),
            history.getChangedBy() != null
                ? history.getChangedBy().getId()
                : null,
            history.getChangedBy() != null
                ? history.getChangedBy().getFirstName() +
                  " " +
                  history.getChangedBy().getLastName()
                : "System",
            history.getAffectedSession() != null
                ? history.getAffectedSession().getId()
                : null,
            history.getReason(),
            history.getNotes()
        );
    }

    private PumpConfigurationHistoryDTO.AuditSummary toAuditSummary(
        PumpConfigurationHistory history
    ) {
        String location = String.format(
            "%s - %s - Manguera %s",
            history.getNozzle().getPump().getIsland().getName(),
            history.getNozzle().getPump().getName(),
            history.getNozzle().getSide().name()
        );

        String summary = formatChangeSummary(history);

        String changedBy = history.getChangedBy() != null
            ? history.getChangedBy().getFirstName() +
              " " +
              history.getChangedBy().getLastName()
            : "System";

        return new PumpConfigurationHistoryDTO.AuditSummary(
            history.getChangeTimestamp(),
            history.getChangeType().name(),
            location,
            changedBy,
            summary,
            history.getReason()
        );
    }

    private String formatChangeSummary(PumpConfigurationHistory history) {
        return switch (history.getChangeType()) {
            case PRICE_CHANGE, PRICE_UPDATE -> String.format(
                "Precio: %s → %s",
                history.getPreviousValue(),
                history.getNewValue()
            );
            case ACTIVATION -> "Activado";
            case DEACTIVATION -> "Desactivado";
            case STATUS_CHANGE -> String.format(
                "Estado: %s → %s",
                history.getPreviousValue(),
                history.getNewValue()
            );
            case FUEL_TYPE_CHANGE -> String.format(
                "Combustible: %s → %s",
                history.getPreviousValue(),
                history.getNewValue()
            );
            case NOZZLE_ADDED -> "Manguera añadida";
            case NOZZLE_REMOVED -> "Manguera eliminada";
        };
    }
}
