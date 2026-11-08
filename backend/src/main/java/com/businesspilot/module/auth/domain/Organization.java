package com.businesspilot.module.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(length = 10, nullable = false)
    private String locale;

    @Column(length = 50, nullable = false)
    private String timezone;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "whatsapp_phone_number_id", length = 50)
    private String whatsappPhoneNumberId;

    @Column(name = "stripe_customer_id", length = 255)
    private String stripeCustomerId;

    @Column(name = "widget_token", nullable = false, updatable = false)
    private java.util.UUID widgetToken;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
