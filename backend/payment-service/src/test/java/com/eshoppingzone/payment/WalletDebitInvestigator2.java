package com.eshoppingzone.payment;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class WalletDebitInvestigator2 {
    @Test
    public void investigate() throws Exception {
        System.out.println("=== FAILED PAYMENTS ===");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eshoppingzone_payment?user=root&password=sree@3008");
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, order_id, amount, status, transaction_reference FROM payments WHERE status = 'FAILED'");
            while (rs.next()) {
                System.out.printf("Payment: ID=%d, Order=%d, Amount=%s, Ref=%s\n", rs.getLong(1), rs.getLong(2), rs.getBigDecimal(3), rs.getString(5));
            }
        }
        
        System.out.println("=== WALLET TRANSACTIONS ===");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eshoppingzone_wallet?user=root&password=sree@3008");
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, wallet_id, amount, type, reference FROM wallet_transactions WHERE type IN ('DEBIT', 'CREDIT', 'REFUND')");
            while (rs.next()) {
                System.out.printf("Txn: ID=%d, Wallet=%d, Amount=%s, Type=%s, Ref=%s\n", rs.getLong(1), rs.getLong(2), rs.getBigDecimal(3), rs.getString(4), rs.getString(5));
            }
        }
    }
}
