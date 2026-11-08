package com.businesspilot.module.conversations.domain;

import com.businesspilot.common.entity.TenantAwareEntity;
import jakarta.persistence.*;
import com.businesspilot.module.conversations.domain.MessageSource;

@Entity
@Table(name = "conversations")
public class Conversation extends TenantAwareEntity {

    @Column(name = "prospect_name")
    private String prospectName;

    @Column(name = "prospect_email")
    private String prospectEmail;

    @Column(name = "whatsapp_phone", length = 50)
    private String whatsappPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageSource source = MessageSource.WEB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.OPEN;

    public String getProspectName() { return prospectName; }
    public void setProspectName(String prospectName) { this.prospectName = prospectName; }

    public String getProspectEmail() { return prospectEmail; }
    public void setProspectEmail(String prospectEmail) { this.prospectEmail = prospectEmail; }

    public String getWhatsappPhone() { return whatsappPhone; }
    public void setWhatsappPhone(String whatsappPhone) { this.whatsappPhone = whatsappPhone; }

    public MessageSource getSource() { return source; }
    public void setSource(MessageSource source) { this.source = source; }

    public ConversationStatus getStatus() { return status; }
    public void setStatus(ConversationStatus status) { this.status = status; }
}
