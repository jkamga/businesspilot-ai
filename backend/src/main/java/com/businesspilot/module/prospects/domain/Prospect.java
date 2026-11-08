package com.businesspilot.module.prospects.domain;

import com.businesspilot.common.entity.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "prospects")
public class Prospect extends TenantAwareEntity {

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProspectStatus status = ProspectStatus.LEAD;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public ProspectStatus getStatus() { return status; }
    public void setStatus(ProspectStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
