package com.eshoppingzone.inventory.listener;

import com.eshoppingzone.inventory.dto.ProductEvent;
import com.eshoppingzone.inventory.entity.Inventory;
import com.eshoppingzone.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductEventListener productEventListener;

    @Test
    void testHandleProductCreatedNewProduct() {
        ProductEvent event = new ProductEvent("PRODUCT_CREATED", 5L, "New Product", 2L);

        when(inventoryRepository.existsByProductId(5L)).thenReturn(false);

        productEventListener.handleProductCreated(event);

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository, times(1)).save(captor.capture());

        Inventory saved = captor.getValue();
        assertEquals(5L, saved.getProductId());
        assertEquals(50, saved.getAvailableStock());
        assertEquals(0, saved.getReservedStock());
    }

    @Test
    void testHandleProductCreatedExistingProductSkipped() {
        ProductEvent event = new ProductEvent("PRODUCT_CREATED", 5L, "New Product", 2L);

        when(inventoryRepository.existsByProductId(5L)).thenReturn(true);

        productEventListener.handleProductCreated(event);

        verify(inventoryRepository, never()).save(any());
    }
}
