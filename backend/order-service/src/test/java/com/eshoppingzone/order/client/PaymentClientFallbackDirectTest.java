package com.eshoppingzone.order.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.eshoppingzone.order.dto.ApiResponse;
import com.eshoppingzone.order.dto.PaymentResponseDto;

@SpringBootTest(properties = {
    "spring.cloud.openfeign.circuitbreaker.enabled=true",
    "spring.cloud.discovery.client.simple.instances.payment-service[0].uri=http://localhost:9999",
    "eureka.client.enabled=false"
})
public class PaymentClientFallbackDirectTest {
    @Autowired
    private PaymentClient paymentClient;

    @Test
    public void testFallback() {
        try {
            ApiResponse<PaymentResponseDto> res = paymentClient.getPaymentByOrderId(1L);
            System.out.println("TEST_RESULT: " + (res != null ? res.getMessage() : "null"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TEST_ERROR: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
