package com.eshoppingzone.settlement.repository;

import com.eshoppingzone.settlement.entity.SettlementEntry;
import com.eshoppingzone.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<SettlementEntry, Long> {
    Optional<SettlementEntry> findByEventId(String eventId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM SettlementEntry e WHERE e.eventId = :eventId")
    Optional<SettlementEntry> findByEventIdForUpdate(@Param("eventId") String eventId);

    boolean existsByMerchantIdAndSettlementWeekStartAndEntryType(Long merchantId, LocalDate weekStart,
                                                                  com.eshoppingzone.settlement.entity.SettlementEntryType entryType);

    @Query("SELECT e FROM SettlementEntry e WHERE e.merchantId = :merchantId AND e.settlementWeekStart = :weekStart AND e.entryType = :entryType")
    List<SettlementEntry> findByMerchantAndWeekAndEntryType(@Param("merchantId") Long merchantId,
                                                            @Param("weekStart") LocalDate weekStart,
                                                            @Param("entryType") com.eshoppingzone.settlement.entity.SettlementEntryType entryType);

    @Query("SELECT e FROM SettlementEntry e WHERE e.settlementWeekStart = :weekStart AND e.entryType = :entryType")
    List<SettlementEntry> findByWeekAndEntryType(@Param("weekStart") LocalDate weekStart,
                                                 @Param("entryType") com.eshoppingzone.settlement.entity.SettlementEntryType entryType);

    @Query("SELECT e FROM SettlementEntry e WHERE e.merchantId = :merchantId AND e.entryType = :entryType AND e.status = :status ORDER BY e.settlementWeekStart")
    List<SettlementEntry> findByMerchantAndEntryTypeAndStatus(@Param("merchantId") Long merchantId,
                                                              @Param("entryType") com.eshoppingzone.settlement.entity.SettlementEntryType entryType,
                                                              @Param("status") SettlementStatus status);

    @Query("SELECT e FROM SettlementEntry e WHERE e.merchantId = :merchantId AND e.settlementWeekStart = :weekStart AND e.status = :status")
    List<SettlementEntry> findByMerchantAndWeekAndStatus(@Param("merchantId") Long merchantId,
                                                        @Param("weekStart") LocalDate weekStart,
                                                        @Param("status") SettlementStatus status);

    @Query("SELECT e FROM SettlementEntry e WHERE e.settlementWeekStart = :weekStart AND e.status = :status")
    List<SettlementEntry> findByWeekAndStatus(@Param("weekStart") LocalDate weekStart,
                                              @Param("status") SettlementStatus status);

    @Query("SELECT e FROM SettlementEntry e WHERE e.merchantId = :merchantId AND e.deliveryPartnerId = :partnerId AND e.entryType IN :types")
    List<SettlementEntry> findPartnerEntries(@Param("merchantId") Long merchantId, @Param("partnerId") Long partnerId,
                                             @Param("types") List<com.eshoppingzone.settlement.entity.SettlementEntryType> types);
}
