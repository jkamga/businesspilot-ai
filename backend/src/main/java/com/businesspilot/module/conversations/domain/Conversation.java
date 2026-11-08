package com.businesspilot.module.conversations.domain;

import com.businesspilot.common.entity.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "conversations")
public class Conversation extends TenantAwareEntity {

    @Column(name = "prospect_name")
    private String prospectName;

    @Column(name = "prospect_email")
    private String prospectEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.OPEN;

    public String getProspectName() { return prospectName; }
    public void setProspectName(String prospectName) { this.prospectName = prospectName; }

    public String getProspectEmail() { return prospectEmail; }
    public void setProspectEmail(String prospectEmail) { this.prospectEmail = prospectEmail; }

    public ConversationStatus getStatus() { return status; }
    public void setStatus(ConversationStatus status) { this.status = status; }
}
