package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="flight_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlightDetail extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="client_id", nullable=false)
    private Client client;
    @Enumerated(EnumType.STRING) private FlightType flightType;
    private String airline;
    private String flightNumber;
    private String airport;
    private OffsetDateTime arrivalDatetime;
    private OffsetDateTime departureDatetime;
    @Column(columnDefinition="TEXT") private String notes;
}