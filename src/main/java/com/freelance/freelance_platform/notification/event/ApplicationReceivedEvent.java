package com.freelance.freelance_platform.notification.event;

import com.freelance.freelance_platform.shared.event.DomainEvent;

import java.util.UUID;

public class ApplicationReceivedEvent extends DomainEvent {

    private final UUID projectId;
    private final UUID clientId;
    private final UUID freelancerId;
    private final String projectTitle;
    private final String freelancerName;

    public ApplicationReceivedEvent(UUID projectId, UUID clientId,
                                    UUID freelancerId, String projectTitle,
                                    String freelancerName) {
        this.projectId = projectId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.projectTitle = projectTitle;
        this.freelancerName = freelancerName;
    }

    public UUID getProjectId() { return projectId; }
    public UUID getClientId() { return clientId; }
    public UUID getFreelancerId() { return freelancerId; }
    public String getProjectTitle() { return projectTitle; }
    public String getFreelancerName() { return freelancerName; }
}