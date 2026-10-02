package com.eshoppingzone.settlement.service;

import com.eshoppingzone.settlement.dto.SettlementRequest;
import com.eshoppingzone.settlement.dto.SettlementSummaryDto;
import com.eshoppingzone.settlement.client.WalletClient;
import com.eshoppingzone.settlement.entity.*;
import com.eshoppingzone.settlement.repository.CodRepository;
import com.eshoppingzone.settlement.repository.SettlementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class SettlementServiceImpl implements SettlementService {
    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("50.00");
    private final SettlementRepository entries;
    private final CodRepository cods;
    private WalletClient walletClient;
    @Value("${settlement.wallet.authorization:}")
    private String walletAuthorization;
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    public SettlementServiceImpl(SettlementRepository entries, CodRepository cods) {
        this.entries = entries; this.cods = cods;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setWalletClient(WalletClient walletClient) { this.walletClient = walletClient; }

    @Override @Transactional
    public SettlementEntry recordOrderSettlement(SettlementRequest r) {
        requirePositive(r.getGrossAmount(), "Merchant earning");
        return saveOnce(r.getMerchantId(), r.getOrderId(), null, r.getEventId(), r.getGrossAmount(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                SettlementEntryType.ORDER_SETTLEMENT, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public SettlementEntry recordCodCollection(Long merchantId, Long orderId, String eventId, BigDecimal amount) {
        collectCod(orderId, amount, eventId);
        return saveOnce(merchantId, orderId, null, eventId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, amount, SettlementEntryType.COD_COLLECTION, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public SettlementEntry recordDeliveryPayout(Long merchantId, Long orderId, String eventId, BigDecimal amount) {
        return saveOnce(merchantId, orderId, null, eventId, BigDecimal.ZERO, BigDecimal.ZERO, amount,
                BigDecimal.ZERO, BigDecimal.ZERO, SettlementEntryType.DELIVERY_PAYOUT, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public SettlementEntry recordDeliveryPayout(Long merchantId, Long orderId, Long partnerId, String eventId) {
        return saveOnce(merchantId, orderId, partnerId, eventId, BigDecimal.ZERO, BigDecimal.ZERO, DELIVERY_FEE,
                BigDecimal.ZERO, BigDecimal.ZERO, SettlementEntryType.DELIVERY_PAYOUT, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public SettlementEntry recordReturnDeliveryPayout(Long merchantId, Long orderId, String eventId, BigDecimal amount) {
        return saveOnce(merchantId, orderId, null, eventId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                amount, BigDecimal.ZERO, SettlementEntryType.RETURN_DELIVERY_PAYOUT, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public SettlementEntry recordReturnDeliveryPayout(Long merchantId, Long orderId, Long partnerId, String eventId) {
        return saveOnce(merchantId, orderId, partnerId, eventId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                DELIVERY_FEE, BigDecimal.ZERO, SettlementEntryType.RETURN_DELIVERY_PAYOUT, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public SettlementEntry recordRefundDeduction(Long merchantId, Long orderId, String eventId, BigDecimal amount) {
        requirePositive(amount, "Refund deduction");
        return saveOnce(merchantId, orderId, null, eventId, BigDecimal.ZERO, amount, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, SettlementEntryType.REFUND_DEDUCTION, SettlementStatus.PROCESSED);
    }

    @Override @Transactional
    public CodRecord expectCod(Long merchantId, Long orderId, Long partnerId, BigDecimal amount, String eventId) {
        requirePositive(amount, "Expected COD amount");
        Optional<CodRecord> existing = cods.findByOrderIdForUpdate(orderId);
        if (existing.isPresent()) return existing.get();
        CodRecord record = new CodRecord();
        record.setMerchantId(merchantId); record.setOrderId(orderId); record.setDeliveryPartnerId(partnerId);
        record.setExpectedAmount(amount); record.setLastEventId(eventId); record.setStatus(CodStatus.EXPECTED);
        record.setOutstandingAmount(amount);
        return cods.save(record);
    }

    @Override @Transactional
    public CodRecord collectCod(Long orderId, BigDecimal amount, String eventId) {
        requirePositive(amount, "Collected COD amount");
        CodRecord c = lockedCod(orderId, eventId);
        c.setCollectedAmount(amount); c.setOutstandingAmount(c.getExpectedAmount().subtract(amount).max(BigDecimal.ZERO));
        c.setStatus(CodStatus.COLLECTED); c.setLastEventId(eventId);
        return cods.save(c);
    }

    @Override @Transactional
    public CodRecord remitCod(Long orderId, BigDecimal amount, String eventId) {
        requirePositive(amount, "Remitted COD amount");
        CodRecord c = lockedCod(orderId, eventId);
        c.setRemittedAmount(amount); c.setStatus(CodStatus.REMITTED); c.setLastEventId(eventId);
        return cods.save(c);
    }

    @Override @Transactional
    public CodRecord confirmCodReceipt(Long orderId, BigDecimal amount, String eventId) {
        requirePositive(amount, "Received COD amount");
        CodRecord c = lockedCod(orderId, eventId);
        c.setReceivedAmount(amount); c.setOutstandingAmount(c.getExpectedAmount().subtract(amount).max(BigDecimal.ZERO));
        c.setStatus(CodStatus.RECEIVED); c.setLastEventId(eventId);
        if (amount.signum() > 0) saveOnce(c.getMerchantId(), orderId, null, "COD-RECEIVED-" + eventId,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, amount,
                SettlementEntryType.COD_RECEIVED, SettlementStatus.PROCESSED);
        if (c.getOutstandingAmount().signum() == 0) c.setStatus(CodStatus.RECONCILED);
        return cods.save(c);
    }

    @Override @Transactional(readOnly = true)
    public SettlementSummaryDto getMerchantSettlementSummary(Long merchantId, LocalDate weekStart) {
        List<SettlementEntry> list = entries.findByMerchantAndWeekAndStatus(merchantId, monday(weekStart), SettlementStatus.PROCESSED);
        SettlementSummaryDto s = new SettlementSummaryDto(); s.setMerchantId(merchantId); s.setWeekStart(monday(weekStart));
        for (SettlementEntry e : list) {
            s.setGrossAmount(s.getGrossAmount().add(z(e.getGrossAmount())));
            s.setRefundDeduction(s.getRefundDeduction().add(z(e.getRefundDeduction())));
            s.setDeliveryPayout(s.getDeliveryPayout().add(z(e.getDeliveryPayout())));
            s.setReturnDeliveryPayout(s.getReturnDeliveryPayout().add(z(e.getReturnDeliveryPayout())));
            s.setCodCollection(s.getCodCollection().add(z(e.getCodCollection())));
            s.setOrdersCount(s.getOrdersCount() + 1);
        }
        s.setNetAmount(s.getGrossAmount().subtract(s.getRefundDeduction()).subtract(s.getDeliveryPayout())
                .subtract(s.getReturnDeliveryPayout()).add(s.getCodCollection()));
        s.setOutstandingAmount(entries.findByMerchantAndEntryTypeAndStatus(merchantId,
                        SettlementEntryType.MERCHANT_OUTSTANDING, SettlementStatus.OUTSTANDING).stream()
                .map(e -> z(e.getNetAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
        return s;
    }

    @Override @Transactional
    public List<SettlementSummaryDto> runWeeklySettlement() { return settlePreviousWeek(); }

    @Override @Transactional
    public List<SettlementSummaryDto> settlePreviousWeek() {
        LocalDate previous = monday(LocalDate.now()).minusWeeks(1);
        List<SettlementEntry> candidates = entries.findByWeekAndStatus(previous, SettlementStatus.PROCESSED);
        Set<Long> merchantIds = new LinkedHashSet<>();
        candidates.forEach(e -> merchantIds.add(e.getMerchantId()));
        entries.findByWeekAndEntryType(previous, SettlementEntryType.WEEKLY_SETTLEMENT)
                .forEach(e -> merchantIds.add(e.getMerchantId()));
        entries.findAll().stream()
                .filter(e -> e.getEntryType() == SettlementEntryType.WEEKLY_SETTLEMENT
                        && e.getEventId().equals("WEEKLY-" + e.getMerchantId() + "-" + previous))
                .forEach(e -> merchantIds.add(e.getMerchantId()));
        List<SettlementSummaryDto> result = new ArrayList<>();
        for (Long merchantId : merchantIds) {
            String weeklyEventId = "WEEKLY-" + merchantId + "-" + previous;
            List<SettlementEntry> existingWeekly = entries.findByEventIdForUpdate(weeklyEventId)
                    .map(List::of).orElseGet(List::of);
            if (!existingWeekly.isEmpty()
                    && existingWeekly.get(0).getStatus() == SettlementStatus.SETTLED
                    && previous.equals(existingWeekly.get(0).getSettlementWeekStart())) {
                continue;
            }
            SettlementSummaryDto s = getMerchantSettlementSummary(merchantId, previous);
            List<SettlementEntry> carryForward = entries.findByMerchantAndEntryTypeAndStatus(
                    merchantId, SettlementEntryType.MERCHANT_OUTSTANDING, SettlementStatus.OUTSTANDING);
            BigDecimal priorOutstanding = carryForward.stream().map(e -> z(e.getNetAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal payable = s.getNetAmount().max(BigDecimal.ZERO);
            if (candidates.stream().noneMatch(e -> e.getMerchantId().equals(merchantId)) && !existingWeekly.isEmpty()) {
                payable = z(existingWeekly.get(0).getNetAmount()).max(BigDecimal.ZERO);
            }
            BigDecimal appliedOutstanding = payable.min(priorOutstanding);
            BigDecimal walletAmount = payable.subtract(appliedOutstanding);
            BigDecimal newOutstanding = priorOutstanding.subtract(appliedOutstanding)
                    .add(s.getNetAmount().min(BigDecimal.ZERO).abs());

            SettlementEntry weekly = existingWeekly.isEmpty() ? saveOnce(merchantId, -1L, null,
                    "WEEKLY-" + merchantId + "-" + previous, walletAmount, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, SettlementEntryType.WEEKLY_SETTLEMENT, SettlementStatus.PENDING)
                    : existingWeekly.get(0);
            if (walletAmount.signum() > 0) {
                if (walletClient == null) throw new IllegalStateException("Wallet service is required for merchant settlement");
                walletClient.credit(walletAuthorization(),
                        "SETTLEMENT-" + merchantId + "-" + previous,
                        new WalletClient.WalletCreditRequest(merchantId, walletAmount,
                                weekly.getEventId(), "Weekly merchant settlement"));
            }
            weekly.setNetAmount(walletAmount);
            weekly.setSettlementWeekStart(previous);
            weekly.setStatus(SettlementStatus.SETTLED);
            entries.save(weekly);
            for (SettlementEntry outstanding : carryForward) outstanding.setStatus(SettlementStatus.SETTLED);
            if (newOutstanding.signum() > 0) {
                SettlementEntry outstanding = saveOnce(merchantId, -1L, null, "OUTSTANDING-" + merchantId + "-" + previous,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, SettlementEntryType.MERCHANT_OUTSTANDING, SettlementStatus.OUTSTANDING);
                outstanding.setNetAmount(newOutstanding);
                entries.save(outstanding);
            }
            result.add(s);
            s.setOutstandingAmount(newOutstanding);
            log.info("Settled merchant {} for {} with wallet amount {} and outstanding {}",
                    merchantId, previous, walletAmount, newOutstanding);
            candidates.stream().filter(e -> e.getMerchantId().equals(merchantId)).forEach(e -> e.setStatus(SettlementStatus.SETTLED));
            entries.saveAll(candidates.stream().filter(e -> e.getMerchantId().equals(merchantId)).toList());
        }
        return result;
    }

    @Override @Transactional
    public void processSettlementEvent(String type, Long merchantId, Long orderId, BigDecimal amount, String eventId) {
        switch (type) {
            case "ORDER_SETTLEMENT", "ONLINE_PAYMENT_SUCCESS" -> recordOrderSettlement(req(merchantId, orderId, amount, eventId));
            case "COD_COLLECTION" -> recordCodCollection(merchantId, orderId, eventId, amount);
            case "DELIVERY_PAYOUT" -> recordDeliveryPayout(merchantId, orderId, eventId, amount);
            case "RETURN_DELIVERY_PAYOUT" -> recordReturnDeliveryPayout(merchantId, orderId, eventId, amount);
            case "REFUND_DEDUCTION", "REFUND_COMPLETED" -> recordRefundDeduction(merchantId, orderId, eventId, amount);
            default -> throw new IllegalArgumentException("Unsupported settlement event type: " + type);
        }
    }

    @Scheduled(cron = "${settlement.weekly-cron:0 0 3 * * MON}")
    public void automaticWeeklySettlement() { settlePreviousWeek(); }

    private CodRecord lockedCod(Long orderId, String eventId) {
        return cods.findByOrderIdForUpdate(orderId).orElseThrow(() -> new IllegalArgumentException("COD record not found for order " + orderId));
    }
    private SettlementEntry saveOnce(Long merchantId, Long orderId, Long partnerId, String eventId, BigDecimal gross,
                                     BigDecimal refund, BigDecimal delivery, BigDecimal returned, BigDecimal cod,
                                     SettlementEntryType type, SettlementStatus status) {
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("Event ID is required");
        Optional<SettlementEntry> old = entries.findByEventIdForUpdate(eventId);
        if (old.isPresent()) return old.get();
        SettlementEntry e = new SettlementEntry(); e.setMerchantId(merchantId); e.setOrderId(orderId); e.setDeliveryPartnerId(partnerId);
        e.setEventId(eventId); e.setSettlementWeekStart(monday(LocalDate.now())); e.setGrossAmount(z(gross));
        e.setRefundDeduction(z(refund)); e.setDeliveryPayout(z(delivery)); e.setReturnDeliveryPayout(z(returned)); e.setCodCollection(z(cod));
        e.setEntryType(type); e.setStatus(status); e.setNetAmount(e.getGrossAmount().subtract(e.getRefundDeduction())
                .subtract(e.getDeliveryPayout()).subtract(e.getReturnDeliveryPayout()).add(e.getCodCollection()));
        return entries.save(e);
    }
    private static SettlementRequest req(Long m, Long o, BigDecimal a, String id) { SettlementRequest r = new SettlementRequest(); r.setMerchantId(m); r.setOrderId(o); r.setGrossAmount(a); r.setEventId(id); return r; }
    private static BigDecimal z(BigDecimal n) { return n == null ? BigDecimal.ZERO : n; }
    private String walletAuthorization() {
        if (walletAuthorization != null && !walletAuthorization.isBlank()) return walletAuthorization;
        String secret = (jwtSecret == null || jwtSecret.isBlank())
                ? "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" : jwtSecret;
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("settlement-service").claim("userId", 0L)
                .claim("role", "INTERNAL").issuedAt(java.util.Date.from(Instant.now()))
                .expiration(java.util.Date.from(Instant.now().plusSeconds(300))).signWith(key).compact();
        return "Bearer " + token;
    }
    private static void requirePositive(BigDecimal n, String label) { if (n == null || n.signum() <= 0) throw new IllegalArgumentException(label + " must be greater than zero"); }
    private static LocalDate monday(LocalDate d) { return d.with(DayOfWeek.MONDAY); }
}
