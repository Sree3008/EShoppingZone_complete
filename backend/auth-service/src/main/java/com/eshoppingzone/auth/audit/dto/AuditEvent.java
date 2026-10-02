package com.eshoppingzone.auth.audit.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuditEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private LocalDateTime timestamp;
    private String actorUserId;
    private String actorUsername;
    private String actorRole;
    private String serviceName;
    private String action;
    private String resourceType;
    private String resourceId;
    private String outcome;
    private String correlationId;
    private String metadata;

    public AuditEvent() {
    }

    public AuditEvent(String eventId, LocalDateTime timestamp, String actorUserId, String actorUsername,
                      String actorRole, String serviceName, String action, String resourceType,
                      String resourceId, String outcome, String correlationId, String metadata) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.actorUserId = actorUserId;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.serviceName = serviceName;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.outcome = outcome;
        this.correlationId = correlationId;
        this.metadata = metadata;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}

