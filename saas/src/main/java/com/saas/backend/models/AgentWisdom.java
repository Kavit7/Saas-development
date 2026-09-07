package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="agent_wisdom")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentWisdom extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by", nullable=false)
    private User createdBy;
    private String title;
    @Column(nullable=false, columnDefinition="TEXT") private String content;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id")
    private Property property;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="tag_id")
    private Tag tag;
    @Column(columnDefinition="jsonb") private String context;
    private BigDecimal confidenceScore;
    @Enumerated(EnumType.STRING) private WisdomStatus status;
    @Version private Integer version;
}