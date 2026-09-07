package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="itinerary_days")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryDay extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="safari_id", nullable=false)
    private Safari safari;
    private Integer dayNumber;
    private LocalDate date;
    private String destination;
    @Column(columnDefinition="TEXT") private String notes;
}