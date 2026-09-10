package com.saas.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="companies")
@Setter 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder

public class Company extends BaseEntity {
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String slug;
    private String email;
    private String phone;
    private String country;
    private String timezone;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;
    
    @Enumerated(EnumType.STRING) private SubscriptionStatus status;
}