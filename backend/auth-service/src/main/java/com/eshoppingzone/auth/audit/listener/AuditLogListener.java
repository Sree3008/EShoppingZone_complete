package com.eshoppingzone.auth.audit.listener;

import com.eshoppingzone.auth.audit.dto.AuditEvent;
import com.eshoppingzone.auth.audit.entity.AuditLog;
import com.eshoppingzone.auth.audit.repository.AuditLogRepository;
import com.eshoppingzone.auth.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLogListener {

    private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleAuditLog(AuditEvent event) {
        try {
            log.debug("Received audit event: {}", event.getEventId());
            AuditLog auditLog = new AuditLog();
            auditLog.setEventId(event.getEventId());
            auditLog.setTimestamp(event.getTimestamp());
            auditLog.setActorUserId(event.getActorUserId());
            auditLog.setActorUsername(event.getActorUsername());
            auditLog.setActorRole(event.getActorRole());
            auditLog.setServiceName(event.getServiceName());
            auditLog.setAction(event.getAction());
            auditLog.setResourceType(event.getResourceType());
            auditLog.setResourceId(event.getResourceId());
            auditLog.setOutcome(event.getOutcome());
            auditLog.setCorrelationId(event.getCorrelationId());
            auditLog.setMetadata(event.getMetadata());
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to process audit event {}: {}", event.getEventId(), e.getMessage());
        }
    }
}
