package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="accommodation_bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccommodationBooking extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="safari_id", nullable=false)
    private Safari safari;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="itinerary_day_id", nullable=false)
    private ItineraryDay itineraryDay;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id", nullable=false)
    private Property property;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reservation_manager_id")
    private User reservationManager;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sales_person_id")
    private User salesPerson;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer rooms;
    private String roomType;
    @Enumerated(EnumType.STRING) private BookingStatus status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime respondedAt;
    private OffsetDateTime confirmedAt;
    private String confirmationNumber;
    @Column(columnDefinition="TEXT") private String specialRequests;
    @Column(columnDefinition="TEXT") private String notes;
    @Version private Integer version;
}