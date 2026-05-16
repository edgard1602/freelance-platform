package com.freelance.freelance_platform.notification.event;

import com.freelance.freelance_platform.shared.event.DomainEvent;

import java.util.UUID;

public class ApplicationAcceptedEvent extends DomainEvent {

    private final UUID freelancerId;
    private final UUID projectId;
    private final String projectTitle;

    public ApplicationAcceptedEvent(UUID freelancerId, UUID projectId,
                                    String projectTitle) {
        this.freelancerId = freelancerId;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
    }

    public UUID getFreelancerId() { return freelancerId; }
    public UUID getProjectId() { return projectId; }
    public String getProjectTitle() { return projectTitle; }
}