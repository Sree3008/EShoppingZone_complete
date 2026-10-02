package com.eshoppingzone.settlement.config;

import com.eshoppingzone.settlement.service.SettlementService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "eshoppingzone.exchange";
    @Bean TopicExchange settlementExchange() { return new TopicExchange(EXCHANGE); }
    @Bean Queue settlementQueue() { return QueueBuilder.durable("settlement-service.events").withArgument("x-dead-letter-exchange", EXCHANGE).withArgument("x-dead-letter-routing-key", "settlement.dlq").build(); }
    @Bean Binding settlementBinding(Queue settlementQueue, TopicExchange settlementExchange) { return BindingBuilder.bind(settlementQueue).to(settlementExchange).with("payment.#"); }
    @Bean Queue settlementDeliveryQueue() { return QueueBuilder.durable("settlement-service.delivery-events").build(); }
    @Bean Binding settlementDeliveryBinding(Queue settlementDeliveryQueue, TopicExchange settlementExchange) { return BindingBuilder.bind(settlementDeliveryQueue).to(settlementExchange).with("delivery.#"); }
    @Bean Queue settlementOrderQueue() { return QueueBuilder.durable("settlement-service.order-events").build(); }
    @Bean Binding settlementOrderBinding(Queue settlementOrderQueue, TopicExchange settlementExchange) { return BindingBuilder.bind(settlementOrderQueue).to(settlementExchange).with("eshoppingzone.order.#"); }
    @Bean Jackson2JsonMessageConverter settlementMessageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean SettlementEventListener settlementEventListener(SettlementService service) { return new SettlementEventListener(service); }
    static class SettlementEventListener {
        private final SettlementService service;
        SettlementEventListener(SettlementService service) { this.service = service; }
        @RabbitListener(queues = "settlement-service.events")
        public void payment(Map<String, Object> e) { handle(e); }
        @RabbitListener(queues = "settlement-service.delivery-events")
        public void delivery(Map<String, Object> e) { handle(e); }
        @RabbitListener(queues = "settlement-service.order-events")
        public void order(Map<String, Object> e) { handle(e); }
        private void handle(Map<String, Object> e) {
            String type = String.valueOf(e.getOrDefault("eventType", e.getOrDefault("type", "")));
            Long merchant = number(e.get("merchantId")); Long order = number(e.get("orderId"));
            BigDecimal amount = e.get("amount") == null ? null : new BigDecimal(String.valueOf(e.get("amount")));
            String id = String.valueOf(e.getOrDefault("eventId", e.getOrDefault("id", "")));
            if (type.isBlank() || merchant == null || order == null || id.isBlank()) throw new IllegalArgumentException("Invalid settlement message");
            service.processSettlementEvent(type, merchant, order, amount, id);
        }
        private Long number(Object value) { return value == null ? null : Long.valueOf(String.valueOf(value)); }
    }
}
