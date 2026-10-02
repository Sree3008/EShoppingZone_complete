package com.eshoppingzone.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
public class WalletDebitInvestigator {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void investigate() {
        System.out.println("=== FAILED PAYMENTS ===");
        List<Map<String, Object>> payments = jdbcTemplate.queryForList("SELECT id, order_id, amount, status, transaction_reference FROM payments WHERE status = 'FAILED'");
        for (Map<String, Object> p : payments) {
            System.out.println(p);
        }
    }
}
