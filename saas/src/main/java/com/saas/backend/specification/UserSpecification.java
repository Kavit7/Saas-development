package com.saas.backend.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.saas.backend.models.User;
import com.saas.backend.models.UserStatus;

public class UserSpecification {

    public static Specification<User> hasCompanyId(UUID companyId) {
        if (companyId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<User> hasCompany(String companyOrId) {
        if (companyOrId == null || companyOrId.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            try {
                UUID id = UUID.fromString(companyOrId.trim());
                return cb.or(
                    cb.equal(root.get("company").get("id"), id),
                    cb.equal(cb.lower(root.get("company").get("name")), companyOrId.trim().toLowerCase())
                );
            } catch (IllegalArgumentException e) {
                return cb.equal(cb.lower(root.get("company").get("name")), companyOrId.trim().toLowerCase());
            }
        };
    }

    public static Specification<User> hasRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            String cleanRole = roleName.trim().toUpperCase().replace("ROLE_", "");
            return cb.or(
                cb.equal(cb.upper(root.get("role").get("name")), cleanRole),
                cb.like(cb.upper(root.get("role").get("name")), "%" + cleanRole + "%")
            );
        };
    }

    public static Specification<User> hasStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        try {
            UserStatus status = UserStatus.valueOf(statusStr.trim().toUpperCase());
            return (root, query, cb) -> cb.equal(root.get("status"), status);
        } catch (IllegalArgumentException e) {
            return (root, query, cb) -> cb.conjunction();
        }
    }

    public static Specification<User> hasSearch(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern)
            );
        };
    }
}
