package com.saas.backend.models;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="clients")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Client extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id", nullable=false)
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sales_person_id")
    private User salesPerson;

    @Column(nullable=false) private String firstName;
    @Column(nullable=false) private String lastName;
    private String email;
    private String phone;
    private String nationality;
    private String preferredLanguage;
    private String countryOfResidence;
    @Column(columnDefinition="TEXT") private String notes;
    @Enumerated(EnumType.STRING) private ClientStatus status;

    @Version private Integer version;
}