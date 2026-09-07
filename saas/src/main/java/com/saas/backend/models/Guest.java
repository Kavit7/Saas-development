package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="guests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guest extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="client_id", nullable=false)
    private Client client;
    @Column(nullable=false) private String firstName;
    @Column(nullable=false) private String lastName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String passportNumber;
    private LocalDate passportExpiry;
    private String gender;
}