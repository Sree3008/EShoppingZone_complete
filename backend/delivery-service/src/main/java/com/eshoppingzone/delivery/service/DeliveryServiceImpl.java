package com.eshoppingzone.delivery.service;

import com.eshoppingzone.delivery.client.OrderClient;
import com.eshoppingzone.delivery.client.PaymentClient;
import com.eshoppingzone.delivery.config.RabbitMQConfig;
import com.eshoppingzone.delivery.dto.ApiResponse;
import com.eshoppingzone.delivery.dto.AssignDeliveryRequest;
import com.eshoppingzone.delivery.dto.DeliveryDto;
import com.eshoppingzone.delivery.dto.DeliveryEvent;
import com.eshoppingzone.delivery.dto.OrderDto;
import com.eshoppingzone.delivery.dto.UpdateDeliveryStatusRequest;
import com.eshoppingzone.delivery.entity.Delivery;
import com.eshoppingzone.delivery.entity.DeliveryStatus;
import com.eshoppingzone.delivery.exception.DeliveryException;
import com.eshoppingzone.delivery.exception.ResourceNotFoundException;
import com.eshoppingzone.delivery.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final RabbitTemplate rabbitTemplate;
    private final com.eshoppingzone.delivery.audit.service.AuditLogService auditLogService;

    @org.springframework.beans.factory.annotation.Autowired
    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
            OrderClient orderClient,
            PaymentClient paymentClient,
            RabbitTemplate rabbitTemplate,
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.eshoppingzone.delivery.audit.service.AuditLogService auditLogService) {
        this.deliveryRepository = deliveryRepository;
        this.orderClient = orderClient;
        this.paymentClient = paymentClient;
        this.rabbitTemplate = rabbitTemplate;
        this.auditLogService = auditLogService;
    }

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
            OrderClient orderClient,
            PaymentClient paymentClient,
            RabbitTemplate rabbitTemplate) {
        this(deliveryRepository, orderClient, paymentClient, rabbitTemplate, null);
    }

    private java.util.Map<String, Object> safeMeta(Object... keyValues) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length && keyValues[i] != null && keyValues[i + 1] != null) {
                map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
            }
        }
        return map;
    }

    private void audit(String action, String resourceType, String resourceId, String outcome, String failureReason,
            java.util.Map<String, Object> metadata) {
        if (auditLogService != null) {
            try {
                auditLogService.logAction(action, resourceType, resourceId, outcome, failureReason, metadata);
            } catch (Exception e) {
                log.warn("Failed to write audit log: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public DeliveryDto assignDelivery(AssignDeliveryRequest request) {
        log.info("Assigning delivery for orderId: {} to agentId: {}", request.getOrderId(),
                request.getDeliveryAgentId());

        // Verify order status before assigning delivery
        try {
            ApiResponse<OrderDto> orderResponse = orderClient.getOrderById(request.getOrderId());
            if (orderResponse != null && orderResponse.getData() != null) {
                String status = orderResponse.getData().getStatus();
                if ("CANCELLED".equals(status) || "FAILED".equals(status) || "FAILED_DELIVERY".equals(status)) {
                    throw new DeliveryException("Cannot assign delivery for an order in status: " + status);
                }
            }
        } catch (DeliveryException de) {
            throw de;
        } catch (Exception ex) {
            log.warn("Could not verify order status before assigning delivery for orderId={}: {}", request.getOrderId(),
                    ex.getMessage());
        }

        Long adminId = 1L;
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.eshoppingzone.delivery.security.UserPrincipal up
                    && up.getUserId() != null) {
                adminId = up.getUserId();
            }
        } catch (Exception ignored) {
        }

        Optional<Delivery> existingOpt = deliveryRepository.findByOrderId(request.getOrderId());
        Delivery delivery;
        if (existingOpt.isPresent()) {
            delivery = existingOpt.get();
            if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
                throw new DeliveryException("Order has already been delivered");
            }
            delivery.setDeliveryAgentId(request.getDeliveryAgentId());
            delivery.setAssignedByAdminId(adminId);
            delivery.setPickupAddress(request.getPickupAddress());
            delivery.setDeliveryAddress(request.getDeliveryAddress());
            delivery.setCustomerPhone(request.getCustomerPhone());
            delivery.setStatus(DeliveryStatus.ASSIGNED);
            delivery.setAssignedAt(LocalDateTime.now());
        } else {
            delivery = new Delivery(request.getOrderId(), request.getCustomerId(), request.getDeliveryAddress(),
                    request.getCustomerPhone());
            delivery.setDeliveryAgentId(request.getDeliveryAgentId());
            delivery.setAssignedByAdminId(adminId);
            delivery.setPickupAddress(request.getPickupAddress());
            delivery.setStatus(DeliveryStatus.ASSIGNED);
            delivery.setAssignedAt(LocalDateTime.now());
        }

        Delivery saved = deliveryRepository.save(delivery);
        audit("DELIVERY_ASSIGNED", "DELIVERY", String.valueOf(saved.getId()), "SUCCESS", null,
                safeMeta("orderId", saved.getOrderId(), "customerId", saved.getCustomerId(), "deliveryAgentId",
                        saved.getDeliveryAgentId(), "status", saved.getStatus()));

        // Notify order-service of ASSIGNED status
        try {
            log.info("[DELIVERY_ASSIGNED] Notifying order-service for orderId={} to status ASSIGNED",
                    saved.getOrderId());
            orderClient.updateOrderStatus(saved.getOrderId(), Collections.singletonMap("status", "ASSIGNED"));
        } catch (Exception ex) {
            log.warn("[DELIVERY_ASSIGNED] Failed to notify order-service of ASSIGNED status for orderId={}: {}",
                    saved.getOrderId(), ex.getMessage());
        }

        // Publish DELIVERY_ASSIGNED event
        try {
            DeliveryEvent event = new DeliveryEvent(
                    "DELIVERY_ASSIGNED",
                    saved.getId(),
                    saved.getOrderId(),
                    saved.getCustomerId(),
                    saved.getDeliveryAgentId(),
                    saved.getAssignedByAdminId(),
                    saved.getStatus().name(),
                    null);
            CorrelationData correlationData = new CorrelationData("delivery-" + saved.getId() + "-ASSIGNED");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_DELIVERY_ASSIGNED, event,
                    correlationData);
        } catch (Exception ex) {
            log.error("Failed to publish DELIVERY_ASSIGNED event: {}", ex.getMessage());
        }

        return DeliveryDto.fromEntity(saved);
    }

    @Override
    public List<DeliveryDto> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryDto> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status).stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryDto> getAgentDeliveries(Long deliveryAgentId) {
        return deliveryRepository.findByDeliveryAgentId(deliveryAgentId).stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryDto> getAgentDeliveriesByStatus(Long deliveryAgentId, DeliveryStatus status) {
        return deliveryRepository.findByDeliveryAgentIdAndStatus(deliveryAgentId, status).stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryDto updateDeliveryStatusByAgent(Long deliveryAgentId, Long deliveryId,
            UpdateDeliveryStatusRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        if (!delivery.getDeliveryAgentId().equals(deliveryAgentId)) {
            throw new DeliveryException("You are not authorized to update this delivery task");
        }

        DeliveryStatus newStatus;
        try {
            newStatus = DeliveryStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DeliveryException("Invalid delivery status: " + request.getStatus());
        }

        DeliveryStatus oldStatus = delivery.getStatus();
        validateStatusTransition(delivery.getStatus(), newStatus);
        delivery.setStatus(newStatus);

        if (newStatus == DeliveryStatus.PICKED_UP) {
            if (delivery.getPickedUpAt() == null) {
                delivery.setPickedUpAt(LocalDateTime.now());
            }
        } else if (newStatus == DeliveryStatus.OUT_FOR_DELIVERY) {
            if (delivery.getPickedUpAt() == null) {
                throw new DeliveryException(
                        "Cannot set status to OUT_FOR_DELIVERY because package was not picked up (pickedUpAt is null)");
            }
            try {
                log.info("[OUT_FOR_DELIVERY] Updating orderId={} to OUT_FOR_DELIVERY via OrderClient for agentId={}",
                        delivery.getOrderId(), deliveryAgentId);
                orderClient.updateOrderStatus(delivery.getOrderId(),
                        Collections.singletonMap("status", "OUT_FOR_DELIVERY"));
            } catch (Exception ex) {
                log.error(
                        "[OUT_FOR_DELIVERY] Failed to notify order-service of OUT_FOR_DELIVERY status for orderId={}: {}",
                        delivery.getOrderId(), ex.getMessage(), ex);
                throw new DeliveryException("Failed to notify order-service of OUT_FOR_DELIVERY status", ex);
            }
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            if (delivery.getPickedUpAt() == null) {
                throw new DeliveryException(
                        "Cannot set status to DELIVERED because package was not picked up (pickedUpAt is null)");
            }
            // Check if order is eligible for delivery completion
            try {
                ApiResponse<OrderDto> orderResponse = orderClient.getOrderById(delivery.getOrderId());
                if (orderResponse != null && orderResponse.getData() != null) {
                    String status = orderResponse.getData().getStatus();
                    if ("CANCELLED".equals(status) || "RETURN_REQUESTED".equals(status) || "RETURNED".equals(status)
                            || "FAILED_DELIVERY".equals(status) || "FAILED".equals(status)) {
                        throw new DeliveryException("Cannot complete delivery for an order in status: " + status);
                    }
                }
            } catch (DeliveryException ex) {
                throw ex;
            } catch (Exception ex) {
                log.warn("Could not verify order status before completing delivery for orderId={}: {}",
                        delivery.getOrderId(), ex.getMessage());
            }

            delivery.setDeliveredAt(LocalDateTime.now());
            try {
                log.info("[DELIVERED] Updating orderId={} to DELIVERED via OrderClient for agentId={}",
                        delivery.getOrderId(), deliveryAgentId);
                orderClient.updateOrderStatus(delivery.getOrderId(), Collections.singletonMap("status", "DELIVERED"));
            } catch (Exception ex) {
                log.error("[DELIVERED] Failed to notify order-service of DELIVERED status for orderId={}: {}",
                        delivery.getOrderId(), ex.getMessage(), ex);
                throw new DeliveryException("Failed to notify order-service of DELIVERED status", ex);
            }

            try {
                paymentClient.completeCodPayment(delivery.getOrderId());
            } catch (Exception ex) {
                throw new DeliveryException("Failed to complete COD payment", ex);
            }
        } else if (newStatus == DeliveryStatus.FAILED) {
            delivery.setFailureReason(
                    request.getFailureReason() != null ? request.getFailureReason() : "Delivery failed");
            try {
                orderClient.updateOrderStatus(delivery.getOrderId(), Collections.singletonMap("status", "FAILED"));
            } catch (Exception ex) {
                throw new DeliveryException("Failed to notify order-service of FAILED status", ex);
            }
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            try {
                orderClient.updateOrderStatus(delivery.getOrderId(), Collections.singletonMap("status", "CANCELLED"));
            } catch (Exception ex) {
                throw new DeliveryException("Failed to notify order-service of CANCELLED status", ex);
            }
        }

        Delivery saved = deliveryRepository.save(delivery);
        audit("DELIVERY_STATUS_CHANGED", "DELIVERY", String.valueOf(saved.getId()), "SUCCESS", saved.getFailureReason(),
                safeMeta("orderId", saved.getOrderId(), "deliveryAgentId", deliveryAgentId, "oldStatus", oldStatus,
                        "newStatus", saved.getStatus()));

        // Publish event
        try {
            DeliveryEvent event = new DeliveryEvent(
                    "DELIVERY_STATUS_CHANGED",
                    saved.getId(),
                    saved.getOrderId(),
                    saved.getCustomerId(),
                    saved.getDeliveryAgentId(),
                    saved.getAssignedByAdminId(),
                    saved.getStatus().name(),
                    saved.getFailureReason());
            CorrelationData correlationData = new CorrelationData("delivery-" + saved.getId() + "-STATUS_CHANGED");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_DELIVERY_STATUS, event,
                    correlationData);
        } catch (Exception ex) {
            log.error("Failed to publish DELIVERY_STATUS_CHANGED event: {}", ex.getMessage());
        }

        return DeliveryDto.fromEntity(saved);
    }

    private void validateStatusTransition(DeliveryStatus current, DeliveryStatus next) {
        if (current == null) {
            if (next == DeliveryStatus.PENDING || next == DeliveryStatus.ASSIGNED) {
                return;
            }
            throw new DeliveryException("Delivery cannot transition from an unknown state to " + next);
        }

        if (current == next) {
            return; // Idempotent same-state transition
        }

        if (current == DeliveryStatus.DELIVERED || current == DeliveryStatus.CANCELLED) {
            throw new DeliveryException(
                    "Cannot change status of an already " + current.name().toLowerCase() + " delivery");
        }

        if (current == DeliveryStatus.FAILED && next != DeliveryStatus.CANCELLED) {
            throw new DeliveryException("A failed delivery cannot be resumed; it must be cancelled or left as failed");
        }

        boolean allowed = switch (current) {
            case PENDING -> next == DeliveryStatus.ASSIGNED || next == DeliveryStatus.CANCELLED;
            case ASSIGNED ->
                next == DeliveryStatus.ACCEPTED || next == DeliveryStatus.REJECTED || next == DeliveryStatus.PICKED_UP
                        || next == DeliveryStatus.FAILED || next == DeliveryStatus.CANCELLED;
            case ACCEPTED ->
                next == DeliveryStatus.PICKED_UP || next == DeliveryStatus.FAILED || next == DeliveryStatus.CANCELLED;
            case PICKED_UP -> next == DeliveryStatus.OUT_FOR_DELIVERY || next == DeliveryStatus.FAILED
                    || next == DeliveryStatus.CANCELLED;
            case OUT_FOR_DELIVERY ->
                next == DeliveryStatus.DELIVERED || next == DeliveryStatus.FAILED || next == DeliveryStatus.CANCELLED;
            case REJECTED, FAILED -> next == DeliveryStatus.CANCELLED;
            default -> false;
        };

        if (!allowed) {
            throw new DeliveryException("Invalid delivery status transition from " + current + " to " + next);
        }
    }

    @Override
    public DeliveryDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for orderId: " + orderId));
        return DeliveryDto.fromEntity(delivery);
    }

    @Override
    public List<DeliveryDto> getMyDeliveries(Long customerId) {
        return deliveryRepository.findByCustomerId(customerId).stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryDto getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
        return DeliveryDto.fromEntity(delivery);
    }
}
