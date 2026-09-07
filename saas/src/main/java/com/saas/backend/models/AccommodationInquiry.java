package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="accommodation_inquiries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccommodationInquiry extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="accommodation_booking_id", nullable=false)
    private AccommodationBooking accommodationBooking;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sender_id", nullable=false)
    private User sender;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="receiver_id", nullable=false)
    private User receiver;
    private String subject;
    @Column(columnDefinition="TEXT") private String message;
    @Enumerated(EnumType.STRING) private Priority priority;
    @Enumerated(EnumType.STRING) private TicketStatus status;
}