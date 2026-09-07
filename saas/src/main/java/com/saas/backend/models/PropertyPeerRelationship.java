package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="property_peer_relationships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyPeerRelationship extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id", nullable=false)
    private Property property;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="similar_property_id", nullable=false)
    private Property similarProperty;
    private String relationshipType;
    private BigDecimal confidenceScore;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
    private Integer verifiedCount;
    private String status;
}