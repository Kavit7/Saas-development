package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="special_occasions")
@Getter 
@Setter @NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class SpecialOccasion extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="client_id", nullable=false)
    private Client client;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="guest_id")
    private Guest guest;
    @Enumerated(EnumType.STRING) private OccasionType occasionType;
    private LocalDate occasionDate;
    @Column(columnDefinition="TEXT") private String notes;
}