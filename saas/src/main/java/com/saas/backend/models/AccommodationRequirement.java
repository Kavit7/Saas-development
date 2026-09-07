package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="accommodation_requirements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccommodationRequirement extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="safari_id", nullable=false)
    private Safari safari;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="itinerary_day_id", nullable=false)
    private ItineraryDay itineraryDay;
    private String destination;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="required_category_id")
    private PropertyCategory requiredCategory;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="required_price_tier_id")
    private PriceTier requiredPriceTier;
    private Integer numberOfRooms;
    @Column(columnDefinition="TEXT") private String roomPreferences;
    @Column(columnDefinition="TEXT") private String specialRequests;
    private String status;
    @Version private Integer version;
}