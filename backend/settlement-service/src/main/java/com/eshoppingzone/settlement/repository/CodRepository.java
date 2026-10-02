package com.eshoppingzone.settlement.repository;

import com.eshoppingzone.settlement.entity.CodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CodRepository extends JpaRepository<CodRecord, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CodRecord c where c.orderId = :orderId")
    Optional<CodRecord> findByOrderIdForUpdate(@Param("orderId") Long orderId);
    Optional<CodRecord> findByLastEventId(String eventId);
}
