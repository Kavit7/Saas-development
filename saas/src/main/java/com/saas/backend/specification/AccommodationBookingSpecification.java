package com.saas.backend.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.BookingStatus;

public class AccommodationBookingSpecification {

    public static Specification<AccommodationBooking> hasCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(
            root.get("accommodationRequirement")
                .get("safari")
                .get("client")
                .get("company")
                .get("id"),
            companyId
        );
    }

    public static Specification<AccommodationBooking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<AccommodationBooking> hasSearch(String keyword) {
        return (root, query, cb) -> {
            String searchValue = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("referenceNumber")), searchValue),
                cb.like(cb.lower(root.get("confirmationNumber")), searchValue),
                cb.like(cb.lower(root.get("property").get("name")), searchValue),
                cb.like(cb.lower(root.get("accommodationRequirement").get("destination")), searchValue),
                cb.like(cb.lower(root.get("accommodationRequirement").get("safari").get("referenceNumber")), searchValue)
            );
        };
    }
}
