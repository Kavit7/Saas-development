package com.saas.backend.specification;

import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import com.saas.backend.models.SupportCategory;
import com.saas.backend.models.SupportTicket;
import com.saas.backend.models.TicketStatus;
import jakarta.persistence.criteria.Predicate;

public class SupportTicketSpecification {

    public static Specification<SupportTicket> hasCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<SupportTicket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<SupportTicket> isPlatformTicket() {
        return (root, query, cb) -> cb.or(
            cb.equal(cb.upper(root.get("targetRole")), "PLATFORM_ADMIN"),
            cb.equal(root.get("category"), SupportCategory.TECHNICAL)
        );
    }

    public static Specification<SupportTicket> isVisibleForCompanyAdmin(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<SupportTicket> isVisibleForStaff(UUID companyId, UUID currentUserId, String userRole) {
        return (root, query, cb) -> {
            Predicate companyPred = cb.equal(root.get("company").get("id"), companyId);
            Predicate raisedByPred = cb.equal(root.get("raisedBy").get("id"), currentUserId);
            Predicate assignedToPred = cb.equal(root.get("assignedTo").get("id"), currentUserId);

            String roleNorm = userRole != null ? userRole.toUpperCase().replace("ROLE_", "").trim() : "";
            Predicate targetRolePred = cb.equal(cb.upper(root.get("targetRole")), roleNorm);

            // Legacy fallback for records where targetRole was null:
            Predicate legacyFallback;
            if ("RESERVATION_MANAGER".equalsIgnoreCase(roleNorm)) {
                legacyFallback = cb.and(
                    cb.isNull(root.get("targetRole")),
                    cb.equal(root.get("category"), SupportCategory.BOOKING_ISSUE)
                );
            } else if ("ADMIN".equalsIgnoreCase(roleNorm)) {
                legacyFallback = cb.and(
                    cb.isNull(root.get("targetRole")),
                    cb.or(
                        cb.equal(root.get("category"), SupportCategory.ACCOUNT_ISSUE),
                        cb.equal(root.get("category"), SupportCategory.PAYMENT_ISSUE),
                        cb.equal(root.get("category"), SupportCategory.LOGIN_ISSUE)
                    )
                );
            } else {
                legacyFallback = cb.disjunction();
            }

            Predicate accessPred = cb.or(raisedByPred, assignedToPred, targetRolePred, legacyFallback);
            return cb.and(companyPred, accessPred);
        };
    }
}
