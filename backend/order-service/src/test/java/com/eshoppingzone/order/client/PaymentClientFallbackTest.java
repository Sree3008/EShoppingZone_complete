package com.eshoppingzone.order.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.eshoppingzone.order.dto.ApiResponse;
import com.eshoppingzone.order.dto.PaymentResponseDto;

@SpringBootTest
public class PaymentClientFallbackTest {
    @Autowired
    private PaymentClient paymentClient;

    @Test
    public void testFallback() {
        try {
            System.out.println("Calling paymentClient...");
            ApiResponse<PaymentResponseDto> res = paymentClient.getPaymentByOrderId(1L);
            System.out.println("Response: " + (res != null ? res.getMessage() : "null"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
