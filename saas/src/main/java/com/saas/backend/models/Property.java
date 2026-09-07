package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;


@Entity @Table(name="properties")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Property extends BaseEntity {
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String slug;
    private String location;
    private String region;
    private String country;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id")
    private PropertyCategory category;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="price_tier_id")
    private PriceTier priceTier;

    @Column(columnDefinition="TEXT") private String description;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String website;
    @Enumerated(EnumType.STRING) private VerificationStatus verificationStatus;

    @Version private Integer version;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
    private java.time.OffsetDateTime verifiedAt;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="verified_by")
    private User verifiedBy;
}