package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity 
@Table(name="itinerary_days",uniqueConstraints={
    @UniqueConstraint(name="uk_itinerary_day_safari_day", columnNames = {"safari_id","day_number"})
})

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryDay extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="safari_id", nullable=false)
    private Safari safari;
    private Integer dayNumber;
    private LocalDate date;
    private String destination;
    @Column(columnDefinition="TEXT") private String notes;
}