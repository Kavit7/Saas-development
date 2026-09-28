package com.saas.backend.models;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="booking_follow_ups")
@Data 
@AllArgsConstructor 
@NoArgsConstructor
@Builder  
public class BookingFollowUp extends BaseEntity {

    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="book_id", nullable = false)
    private AccommodationBooking booking;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime sentAt;
    @Enumerated(EnumType.STRING)
    private FollowUpType type;
     @Enumerated(EnumType.STRING)
    private FollowUpStatus status;

    @Column(columnDefinition = "TEXT" )
    private String message;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="created_by")
    private User createdBy;

   

}
