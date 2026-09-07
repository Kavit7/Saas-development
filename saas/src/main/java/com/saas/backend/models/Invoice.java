package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice extends BaseEntity {
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="accommodation_booking_id", nullable=false, unique=true)
    private AccommodationBooking accommodationBooking;
    @Column(nullable=false, unique=true) private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) private InvoiceStatus status;
    private String fileName;
    private String filePath;
    private java.time.OffsetDateTime issuedAt;
    @Version private Integer version;
}