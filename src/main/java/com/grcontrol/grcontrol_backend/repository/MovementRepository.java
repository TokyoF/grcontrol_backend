package com.grcontrol.grcontrol_backend.repository;

import com.grcontrol.grcontrol_backend.entity.Movement;
import com.grcontrol.grcontrol_backend.entity.ShiftSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    Optional<Movement> findByMovementId(String movementId);

    List<Movement> findBySessionOrderByMovementTimestampDesc(ShiftSession session);

    @Query("SELECT m FROM Movement m WHERE m.session = :session " +
           "AND m.paymentMethod = :paymentMethod")
    List<Movement> findBySessionAndPaymentMethod(
        @Param("session") ShiftSession session,
        @Param("paymentMethod") Movement.PaymentMethod paymentMethod
    );

    @Query("SELECT SUM(m.amount) FROM Movement m WHERE m.session = :session")
    Double sumAmountBySession(@Param("session") ShiftSession session);

    @Query("SELECT SUM(m.amount) FROM Movement m WHERE m.session = :session " +
           "AND m.paymentMethod = :paymentMethod")
    Double sumAmountBySessionAndPaymentMethod(
        @Param("session") ShiftSession session,
        @Param("paymentMethod") Movement.PaymentMethod paymentMethod
    );

    // Nuevos métodos para MovementController
    @Query("SELECT m FROM Movement m WHERE m.session.id = :sessionId ORDER BY m.movementTimestamp DESC")
    List<Movement> findBySessionId(@Param("sessionId") Long sessionId);
}
