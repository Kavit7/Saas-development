package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="guest_requirements")
@Getter @Setter @NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class GuestRequirement extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="guest_id", nullable=false)
    private Guest guest;
    @Enumerated(EnumType.STRING) private RequirementType requirementType;
    @Column(columnDefinition="TEXT") private String requirementValue;
    @Enumerated(EnumType.STRING) private Severity severity;
    @Column(columnDefinition="TEXT") private String notes;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
}