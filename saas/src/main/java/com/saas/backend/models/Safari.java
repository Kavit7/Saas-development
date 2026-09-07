package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="safaris")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Safari extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="client_id", nullable=false)
    private Client client;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sales_person_id")
    private User salesPerson;
    @Column(nullable=false, unique=true) private String referenceNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfPassengers;
    @Enumerated(EnumType.STRING) private SafariStatus status;
    @Column(columnDefinition="TEXT") private String notes;
}