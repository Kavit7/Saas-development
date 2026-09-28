package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="accommodation_bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccommodationBooking extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="accommodation_requirement_id", nullable=false)
    AccommodationRequirement accommodationRequirement;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="property_id", nullable=false)
    private Property property;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reservation_manager_id")
    private User reservationManager;
    private LocalDate checkIn;
    private LocalDate checkOut;
    @Enumerated(EnumType.STRING) private BookingStatus status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime respondedAt;
    private OffsetDateTime confirmedAt;
    @Column(nullable=false,unique = true)
    private String referenceNumber;
    private String confirmationNumber;
    @Column(columnDefinition="TEXT") private String notes;
    @Version private Integer version;
}