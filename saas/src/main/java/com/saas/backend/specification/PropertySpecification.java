package com.saas.backend.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.saas.backend.models.Property;
import com.saas.backend.models.PropertyTag;
import com.saas.backend.models.Tag;
import com.saas.backend.models.VerificationStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class PropertySpecification {

    public static Specification<Property> hasSearch(String search) {
        return (root, query, cb) -> {
            String keyword = "%" + search + "%";

            return cb.or(
                cb.like(cb.lower(root.get("name")), keyword),
                cb.like(cb.lower(root.get("website")), keyword),
                cb.like(cb.lower(root.get("location")), keyword),
                cb.like(cb.lower(root.get("region")), keyword),
                cb.like(cb.lower(root.get("country")), keyword)
            );
        };
    }

    public static Specification<Property> hasStatus(VerificationStatus status) {
        if (status == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("verificationStatus"), status);
    }

    public static Specification<Property> hasTag(String tag, UUID tagId) {
        if ((tag == null || tag.isBlank()) && tagId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Property, PropertyTag> propertyTagJoin = root.join("tags", JoinType.INNER);
            Join<PropertyTag, Tag> tagJoin = propertyTagJoin.join("tag", JoinType.INNER);

            if (tagId != null) {
                return cb.equal(tagJoin.get("id"), tagId);
            }

            try {
                UUID parsedUuid = UUID.fromString(tag.trim());
                return cb.or(
                    cb.equal(tagJoin.get("id"), parsedUuid),
                    cb.equal(cb.lower(tagJoin.get("name")), tag.trim().toLowerCase())
                );
            } catch (IllegalArgumentException e) {
                return cb.equal(cb.lower(tagJoin.get("name")), tag.trim().toLowerCase());
            }
        };
    }
}
