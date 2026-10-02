package com.eshoppingzone.inventory.listener;

import com.eshoppingzone.inventory.config.RabbitMQConfig;
import com.eshoppingzone.inventory.dto.ProductEvent;
import com.eshoppingzone.inventory.entity.Inventory;
import com.eshoppingzone.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductEventListener.class);

    private final InventoryRepository inventoryRepository;

    public ProductEventListener(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_CREATED_QUEUE)
    public void handleProductCreated(ProductEvent event) {
        if (event == null || event.getProductId() == null) {
            return;
        }
        log.info("Received PRODUCT_CREATED event for product ID: {}", event.getProductId());
        if (!inventoryRepository.existsByProductId(event.getProductId())) {
            Inventory inv = new Inventory();
            inv.setProductId(event.getProductId());
            inv.setAvailableStock(50);
            inv.setReservedStock(0);
            inventoryRepository.save(inv);
            log.info("Auto-initialized stock for newly created product ID {}: 50 units", event.getProductId());
        }
    }
}
