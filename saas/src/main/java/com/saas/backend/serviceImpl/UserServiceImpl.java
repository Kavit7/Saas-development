package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.dto.UserStatusRequest;
import com.saas.backend.models.Company;
import com.saas.backend.models.Priority;
import com.saas.backend.models.Role;
import com.saas.backend.models.SupportCategory;
import com.saas.backend.models.SupportTicket;
import com.saas.backend.models.TicketStatus;
import com.saas.backend.models.User;
import com.saas.backend.models.UserStatus;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.RoleRepository;
import com.saas.backend.repositories.SupportTicketRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.UserResponse;
import com.saas.backend.service.UserService;
import com.saas.backend.specification.UserSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service 
@RequiredArgsConstructor 
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final com.saas.backend.repositories.PlatformAdminRepository platformAdminRepository;
    private final com.saas.backend.service.AuditLogService auditLogService;
    private final com.saas.backend.config.JwtService jwtService;

    public UserResponse mapToResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .status(user.getStatus())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .mustChangePassword(user.getMustChangePassword())
                .passwordExpire(user.getPasswordExpire())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String sanitizeName(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String trimmed = name.trim().replaceAll("\\s+", " ");
        String[] words = trimmed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
                if (i < words.length - 1) sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String sanitizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            cleaned = "+255" + cleaned.substring(1);
        } else if (cleaned.startsWith("255") && cleaned.length() == 12) {
            cleaned = "+" + cleaned;
        } else if (!cleaned.startsWith("+") && cleaned.length() >= 9) {
            cleaned = "+" + cleaned;
        }
        return cleaned;
    }

    @Override
    public UserResponse createAdmin(UserRequest userRequest) {
        try {
            if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email is required.");
            }
            if (userRequest.getFirstName() == null || userRequest.getFirstName().trim().isEmpty()) {
                throw new RuntimeException("First name is required.");
            }
            if (userRequest.getLastName() == null || userRequest.getLastName().trim().isEmpty()) {
                throw new RuntimeException("Last name is required.");
            }
            if (userRequest.getRoleName() == null || userRequest.getRoleName().trim().isEmpty()) {
                throw new RuntimeException("Role is required.");
            }

            if (userRepository.findByEmail(userRequest.getEmail().trim().toLowerCase()).isPresent()) {
                throw new RuntimeException("User with email '" + userRequest.getEmail().trim().toLowerCase() + "' already exists");
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            User currentUser = null;
            if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            }

            String reqRoleName = userRequest.getRoleName().trim();
            String normalizedRole = reqRoleName.toUpperCase().replace("ROLE_", "").trim();

            Company company = null;
            boolean isCreatingPlatformAdmin = normalizedRole.equals("SUPER_ADMIN") || normalizedRole.equals("PLATFORM_ADMIN");

            if (!isSuperAdmin) {
                // Company Admin is creating a user:
                if (currentUser == null || currentUser.getCompany() == null) {
                    throw new AccessDeniedException("Only authenticated staff can register users for their company.");
                }
                company = currentUser.getCompany();

                // Strict restriction: Company Admin CANNOT create ADMIN or SUPER_ADMIN
                if (normalizedRole.equals("ADMIN") || isCreatingPlatformAdmin) {
                    throw new AccessDeniedException("Company Admins are strictly not permitted to register another Admin. Only Super Admin can register Company Admins.");
                }

                // Company Admin can only register SALES_PERSON, RESERVATION_MANAGER, GUIDE
                if (!normalizedRole.equals("SALES_PERSON") && !normalizedRole.equals("SALES") &&
                    !normalizedRole.equals("RESERVATION_MANAGER") && !normalizedRole.equals("GUIDE")) {
                    throw new AccessDeniedException("Invalid role. Company Admins can only register Sales Persons, Reservation Managers, or Guides.");
                }
            } else {
                // Super Admin can create users for specified company or platform
                if (userRequest.getCompanyId() != null) {
                    company = companyRepository.findById(userRequest.getCompanyId()).orElse(null);
                }
                if (company == null && userRequest.getCompanyName() != null && !userRequest.getCompanyName().trim().isEmpty()) {
                    company = companyRepository.findByNameIgnoreCase(userRequest.getCompanyName().trim()).orElse(null);
                }
                if (company == null && currentUser != null && currentUser.getCompany() != null) {
                    company = currentUser.getCompany();
                }
                if (company == null && isCreatingPlatformAdmin) {
                    // Platform admin doesn't require a tenant company, attach to first company to satisfy DB not-null constraint
                    company = companyRepository.findAll().stream().findFirst().orElse(null);
                }

                if (company == null && !isCreatingPlatformAdmin) {
                    throw new RuntimeException("Company is required. Please select a company for this user.");
                }
            }

            if (company == null) {
                throw new RuntimeException("Company not found. Please provide or select a valid company for this user.");
            }

            Role role = roleRepository.findByNameIgnoreCase(reqRoleName)
                    .or(() -> reqRoleName.toUpperCase().startsWith("ROLE_")
                            ? roleRepository.findByNameIgnoreCase(reqRoleName.substring(5))
                            : roleRepository.findByNameIgnoreCase("ROLE_" + reqRoleName))
                    .or(() -> roleRepository.findByNameIgnoreCase(normalizedRole))
                    .or(() -> roleRepository.findByNameIgnoreCase("ROLE_" + normalizedRole))
                    .orElseThrow(() -> new RuntimeException("Role Not found: " + reqRoleName));

            Long totalUsers = userRepository.countByCompany(company);
            if (!isCreatingPlatformAdmin && company.getSubscriptionPlan() != null && totalUsers >= company.getSubscriptionPlan().getMaxUsers()) {
                throw new RuntimeException("You can't exceed maximum user plan (" + company.getSubscriptionPlan().getMaxUsers() + " users)");
            }

            String firstName = sanitizeName(userRequest.getFirstName());
            String lastName = sanitizeName(userRequest.getLastName());
            String phone = sanitizePhone(userRequest.getPhone());

            // Auto-generate secure temporary password (creator does not manually type user password)
            String defaultPassword = lastName.toLowerCase().trim() + "123!";
            String rawPassword = (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty())
                    ? userRequest.getPassword().trim()
                    : defaultPassword;
            boolean isDefaultPassword = true;

            String hashPassword = passwordEncoder.encode(rawPassword);

            User user = new User();
            user.setCompany(company);
            user.setEmail(userRequest.getEmail().trim().toLowerCase());
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setGender(userRequest.getGender());
            user.setPhone(phone);
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(role);
            user.setPasswordHash(hashPassword);
            user.setPasswordExpire(OffsetDateTime.now().plusDays(3));
            user.setMustChangePassword(true);

            User saved = userRepository.save(user);

            // If creating Super Admin, also synchronize to platformAdminRepository so platform admin login works
            if (isCreatingPlatformAdmin) {
                try {
                    if (platformAdminRepository.findByEmail(saved.getEmail()).isEmpty()) {
                        com.saas.backend.models.PlatformAdmin pa = com.saas.backend.models.PlatformAdmin.builder()
                                .email(saved.getEmail())
                                .passwordHash(hashPassword)
                                .firstName(saved.getFirstName())
                                .lastName(saved.getLastName())
                                .phone(saved.getPhone())
                                .platformRole(com.saas.backend.models.PlatformRole.SUPER_ADMIN)
                                .status(com.saas.backend.models.PlatformAdminStatus.ACTIVE)
                                .build();
                        platformAdminRepository.save(pa);
                    }
                } catch (Exception ex) {
                    log.warn("Could not mirror new super admin to platform_admins table: {}", ex.getMessage());
                }
            }

            return mapToResponse(saved);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction, String role, String company, String status, String search) {
        try {
            String safeSortBy;
            if (sortBy == null || sortBy.isBlank()) {
                safeSortBy = "createdAt";
            } else {
                String lower = sortBy.toLowerCase().trim();
                if (lower.equals("role") || lower.equals("rolename")) {
                    safeSortBy = "role.name";
                } else if (lower.equals("company") || lower.equals("companyname")) {
                    safeSortBy = "company.name";
                } else if (lower.equals("name") || lower.equals("firstname")) {
                    safeSortBy = "firstName";
                } else if (lower.equals("lastname")) {
                    safeSortBy = "lastName";
                } else if (lower.equals("status")) {
                    safeSortBy = "status";
                } else if (lower.equals("email")) {
                    safeSortBy = "email";
                } else if (lower.equals("phone")) {
                    safeSortBy = "phone";
                } else if (lower.equals("createdat") || lower.equals("created_at")) {
                    safeSortBy = "createdAt";
                } else {
                    safeSortBy = "createdAt";
                }
            }

            Sort sort = direction != null && direction.equalsIgnoreCase("desc")
                    ? Sort.by(safeSortBy).descending() : Sort.by(safeSortBy).ascending();

            int safePage = Math.max(page, 0);
            int safeSize = (size <= 0) ? 20 : size;
            Pageable pageable = PageRequest.of(safePage, safeSize, sort);

            Specification<User> spec = (root, query, cb) -> cb.conjunction();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            boolean isSuperAdmin = false;
            if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
                isSuperAdmin = auth.getAuthorities().stream().anyMatch(a ->
                        a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            }

            if (!isSuperAdmin) {
                if (currentUser != null && currentUser.getCompany() != null) {
                    spec = spec.and(UserSpecification.hasCompanyId(currentUser.getCompany().getId()));
                } else {
                    return Page.empty(pageable);
                }
            } else {
                if (company != null && !company.isBlank()) {
                    spec = spec.and(UserSpecification.hasCompany(company));
                }
            }

            if (role != null && !role.isBlank()) {
                spec = spec.and(UserSpecification.hasRole(role));
            }

            if (status != null && !status.isBlank()) {
                spec = spec.and(UserSpecification.hasStatus(status));
            }

            if (search != null && !search.isBlank()) {
                spec = spec.and(UserSpecification.hasSearch(search));
            }

            return userRepository.findAll(spec, pageable).map(this::mapToResponse);
        } catch (Exception e) {
            log.error("Failed to load users: {}", e.getMessage(), e);
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public UserResponse getUserById(UUID id, Authentication auth) {
        try {
            boolean isSuperAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            boolean isMe = currentUser != null && currentUser.getId().equals(id);
            boolean isSameCompanyStaff = false;
            if (currentUser != null && currentUser.getRole() != null) {
                String role = currentUser.getRole().getName().toUpperCase();
                if ((role.contains("ADMIN") || role.contains("RESERVATION") || role.contains("SALE") || role.contains("GUIDE"))
                        && currentUser.getCompany() != null && targetUser.getCompany() != null) {
                    isSameCompanyStaff = currentUser.getCompany().getId().equals(targetUser.getCompany().getId());
                }
            }

            if (!isSuperAdmin && !isMe && !isSameCompanyStaff) {
                throw new AccessDeniedException("You have no permission to view this resource");
            }
            return mapToResponse(targetUser);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public UserResponse updateUserStatus(UUID id, UserStatusRequest status, Authentication auth) {
        try {
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                    ? userRepository.findByEmail(auth.getName()).orElse(null)
                    : null;
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
                throw new AccessDeniedException("You are forbidden to change your own status");
            }

            if (!isSuperAdmin) {
                if (currentUser == null || currentUser.getCompany() == null || targetUser.getCompany() == null ||
                        !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                    throw new AccessDeniedException("You cannot change the status of a user from another company");
                }
            }

            targetUser.setStatus(status.getStatus());
            userRepository.save(targetUser);
            return mapToResponse(targetUser);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public UserResponse updateUser(UUID id, UserRequest request, Authentication auth) {
        try {
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                    ? userRepository.findByEmail(auth.getName()).orElse(null)
                    : null;
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (!isSuperAdmin) {
                if (currentUser == null || currentUser.getCompany() == null || targetUser.getCompany() == null ||
                        !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                    throw new AccessDeniedException("You cannot update a user from another company");
                }
            }

            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                targetUser.setFirstName(sanitizeName(request.getFirstName()));
            }
            if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                targetUser.setLastName(sanitizeName(request.getLastName()));
            }
            if (request.getPhone() != null) {
                targetUser.setPhone(sanitizePhone(request.getPhone()));
            }
            if (request.getGender() != null) {
                targetUser.setGender(request.getGender());
            }
            if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
                String reqRoleName = request.getRoleName().trim();
                Role role = roleRepository.findByNameIgnoreCase(reqRoleName)
                        .or(() -> reqRoleName.toUpperCase().startsWith("ROLE_")
                                ? roleRepository.findByNameIgnoreCase(reqRoleName.substring(5))
                                : roleRepository.findByNameIgnoreCase("ROLE_" + reqRoleName))
                        .orElse(null);
                if (role != null) {
                    if (!isSuperAdmin) {
                        String roleNameUpper = role.getName().toUpperCase().replace("ROLE_", "").trim();
                        if (roleNameUpper.equals("ADMIN") || roleNameUpper.equals("SUPER_ADMIN")) {
                            throw new AccessDeniedException("Company Admins cannot promote users to Admin or Super Admin.");
                        }
                    }
                    targetUser.setRole(role);
                }
            }
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                targetUser.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
                // User has set their chosen password, which is permanent and never expires after 3 days!
                targetUser.setPasswordExpire(null);
                targetUser.setMustChangePassword(false);
            }

            userRepository.save(targetUser);
            return mapToResponse(targetUser);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public java.util.Map<String, Object> triggerPasswordResetForUser(UUID id, Authentication auth) {
        try {
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                    ? userRepository.findByEmail(auth.getName()).orElse(null)
                    : null;
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (!isSuperAdmin) {
                if (currentUser == null || currentUser.getCompany() == null || targetUser.getCompany() == null ||
                        !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                    throw new AccessDeniedException("You cannot trigger password reset for a user in another company");
                }
            }

            // Default password on reset is user's last name (lowercase, trimmed)
            String defaultPassword = targetUser.getLastName().toLowerCase().trim();
            targetUser.setPasswordHash(passwordEncoder.encode(defaultPassword));
            targetUser.setPasswordResetToken(UUID.randomUUID().toString());
            // Valid for 3 days only
            targetUser.setPasswordExpire(OffsetDateTime.now().plusDays(3));
            targetUser.setMustChangePassword(true);

            if (targetUser.getStatus() != UserStatus.ACTIVE) {
                targetUser.setStatus(UserStatus.ACTIVE);
            }

            userRepository.save(targetUser);

            // Create Support Ticket for audit trail and customer transparency
            try {
                User ticketRaisedBy = currentUser != null ? currentUser : targetUser;
                String performer = currentUser != null ? currentUser.getEmail() : (auth != null ? auth.getName() : "Platform Administrator");
                SupportTicket ticket = SupportTicket.builder()
                        .company(targetUser.getCompany())
                        .raisedBy(ticketRaisedBy)
                        .assignedTo(currentUser)
                        .category(SupportCategory.ACCOUNT_ISSUE)
                        .subject("Password Reset for " + targetUser.getFirstName() + " " + targetUser.getLastName())
                        .description("Administrative password reset executed by " + performer +
                                ". Temporary password reset to user's last name ('" + defaultPassword + "'), valid for 3 days.")
                        .relatedEntityType("USER")
                        .relatedEntityId(targetUser.getId())
                        .priority(Priority.MEDIUM)
                        .status(TicketStatus.RESOLVED)
                        .resolutionNotes("Password reset to default ('" + defaultPassword + "'). Must change password upon login; expires in 3 days (" +
                                targetUser.getPasswordExpire() + ").")
                        .resolvedAt(OffsetDateTime.now())
                        .build();
                supportTicketRepository.save(ticket);
            } catch (Exception ex) {
                log.warn("Could not save audit support ticket for password reset: {}", ex.getMessage());
            }

            return java.util.Map.of(
                    "message", "Password reset successfully. Default password is user's last name ('" + defaultPassword + "'), valid for 3 days.",
                    "email", targetUser.getEmail(),
                    "temporaryPassword", defaultPassword,
                    "expiresAt", targetUser.getPasswordExpire().toString(),
                    "mustChangePassword", true
            );
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public java.util.Map<String, Object> personalizeUserAccount(UUID id, com.saas.backend.dto.AccountPersonalizationRequest request, Authentication auth) {
        try {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Reason for account personalization is required for transparent support tracking.");
            }

            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                    ? userRepository.findByEmail(auth.getName()).orElse(null)
                    : null;

            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Target user not found with id: " + id));

            if (!isSuperAdmin) {
                if (currentUser == null || currentUser.getCompany() == null || targetUser.getCompany() == null ||
                        !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                    throw new AccessDeniedException("You do not have permission to personalize a user from another company.");
                }
            }

            String actionType = request.getActionType() != null ? request.getActionType().toUpperCase().trim() : "CUSTOM_INTERVENTION";
            if ("IMPERSONATE".equals(actionType)) {
                return impersonateUser(id, request.getReason(), auth);
            }
            StringBuilder resolutionDetails = new StringBuilder();
            String performer = currentUser != null ? currentUser.getEmail() : (auth != null ? auth.getName() : "Platform Administrator");

            if ("PASSWORD_RESET".equals(actionType)) {
                String defaultPassword = targetUser.getLastName().toLowerCase().trim();
                targetUser.setPasswordHash(passwordEncoder.encode(defaultPassword));
                targetUser.setPasswordResetToken(UUID.randomUUID().toString());
                targetUser.setPasswordExpire(OffsetDateTime.now().plusDays(3));
                targetUser.setMustChangePassword(true);
                targetUser.setStatus(UserStatus.ACTIVE);
                resolutionDetails.append("Password reset to temporary default ('").append(defaultPassword)
                        .append("') valid for 3 days until ").append(targetUser.getPasswordExpire()).append(". ");
            } else if ("STATUS_CHANGE".equals(actionType)) {
                if (request.getStatus() != null) {
                    targetUser.setStatus(request.getStatus());
                    resolutionDetails.append("Account status updated to ").append(request.getStatus()).append(". ");
                }
            } else if ("DATA_CORRECTION".equals(actionType)) {
                if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                    targetUser.setFirstName(sanitizeName(request.getFirstName()));
                    resolutionDetails.append("First name updated to '").append(targetUser.getFirstName()).append("'. ");
                }
                if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                    targetUser.setLastName(sanitizeName(request.getLastName()));
                    resolutionDetails.append("Last name updated to '").append(targetUser.getLastName()).append("'. ");
                }
                if (request.getPhone() != null) {
                    targetUser.setPhone(sanitizePhone(request.getPhone()));
                    resolutionDetails.append("Phone updated to '").append(targetUser.getPhone()).append("'. ");
                }
                if (request.getGender() != null) {
                    targetUser.setGender(request.getGender());
                    resolutionDetails.append("Gender updated to ").append(targetUser.getGender()).append(". ");
                }
                if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
                    String reqRole = request.getRoleName().trim();
                    Role role = roleRepository.findByNameIgnoreCase(reqRole)
                            .or(() -> reqRole.toUpperCase().startsWith("ROLE_")
                                    ? roleRepository.findByNameIgnoreCase(reqRole.substring(5))
                                    : roleRepository.findByNameIgnoreCase("ROLE_" + reqRole))
                            .orElse(null);
                    if (role != null) {
                        targetUser.setRole(role);
                        resolutionDetails.append("Role updated to ").append(role.getName()).append(". ");
                    }
                }
            } else {
                // CUSTOM_INTERVENTION
                if (request.getStatus() != null) {
                    targetUser.setStatus(request.getStatus());
                    resolutionDetails.append("Status updated to ").append(request.getStatus()).append(". ");
                }
                if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
                    targetUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
                    targetUser.setPasswordExpire(null);
                    targetUser.setMustChangePassword(false);
                    resolutionDetails.append("Permanent password set. ");
                }
            }

            if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
                resolutionDetails.append("Notes: ").append(request.getNotes().trim());
            }

            userRepository.save(targetUser);

            // Generate Transparent Support Ticket for the User and Tenant
            SupportTicket ticket = SupportTicket.builder()
                    .company(targetUser.getCompany())
                    .raisedBy(targetUser) // Raised under target user for 100% customer visibility
                    .assignedTo(currentUser)
                    .category(SupportCategory.ACCOUNT_ISSUE)
                    .subject("[Account Personalization] " + actionType + " for " + targetUser.getFirstName() + " " + targetUser.getLastName())
                    .description("Administrative Account Personalization performed by " + performer + ".\n" +
                            "Reason: " + request.getReason() + "\n" +
                            (resolutionDetails.length() > 0 ? "Action Details: " + resolutionDetails.toString() + "\n" : "") +
                            "Target: " + targetUser.getEmail() + " (" + targetUser.getFirstName() + " " + targetUser.getLastName() + ")")
                    .relatedEntityType("USER")
                    .relatedEntityId(targetUser.getId())
                    .priority(Priority.HIGH)
                    .status(TicketStatus.RESOLVED)
                    .resolutionNotes(resolutionDetails.toString())
                    .resolvedAt(OffsetDateTime.now())
                    .build();

            SupportTicket savedTicket = supportTicketRepository.save(ticket);

            try {
                auditLogService.recordAuditLog(
                        targetUser.getCompany(),
                        currentUser,
                        "ACCOUNT_PERSONALIZATION",
                        "user",
                        targetUser.getId(),
                        null,
                        "{\"actionType\":\"" + actionType + "\",\"ticketId\":\"" + savedTicket.getId() + "\",\"reason\":\"" + request.getReason().replace("\"", "'") + "\"}",
                        "0.0.0.0",
                        "Admin Management Console"
                );
            } catch (Exception ex) {
                log.warn("Could not record audit log for personalization: {}", ex.getMessage());
            }

            return java.util.Map.of(
                    "message", "Account personalization executed successfully. Support Ticket #" + savedTicket.getId() + " generated.",
                    "ticketId", savedTicket.getId(),
                    "ticketSubject", savedTicket.getSubject(),
                    "actionType", actionType,
                    "targetUser", mapToResponse(targetUser)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public java.util.Map<String, Object> impersonateUser(UUID id, String reason, Authentication auth) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Sababu ya kuingia kwenye akaunti ya mtumiaji (Reason) inahitajika kwa ajili ya usalama na uwazi.");
        }

        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
        boolean isCompanyAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSuperAdmin && !isCompanyAdmin) {
            throw new AccessDeniedException("Huna mamlaka ya kuingia kwenye akaunti ya mtumiaji mwingine.");
        }

        User currentUser = (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser"))
                ? userRepository.findByEmail(auth.getName()).orElse(null)
                : null;

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mtumiaji anayelengwa hajapatikana: " + id));

        // Multi-tenant check: Company Admin can ONLY impersonate users within their own company!
        if (!isSuperAdmin) {
            if (currentUser == null || currentUser.getCompany() == null || targetUser.getCompany() == null ||
                    !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                throw new AccessDeniedException("Admin wa kampuni hawezi kuingia kwenye akaunti ya kampuni nyingine.");
            }
        }

        // Security: Prevent impersonating another Super Admin or Platform Admin
        String targetRole = targetUser.getRole() != null ? targetUser.getRole().getName().toUpperCase() : "";
        if (targetRole.contains("SUPER_ADMIN") || targetRole.contains("PLATFORM_ADMIN")) {
            throw new AccessDeniedException("Huwezi kuingia kwenye akaunti ya Super Admin mwingine.");
        }

        String adminIdentifier = currentUser != null ? currentUser.getEmail() : (auth != null ? auth.getName() : "Platform Administrator");

        // 1. Create transparent Support Ticket for target user and company
        SupportTicket ticket = SupportTicket.builder()
                .company(targetUser.getCompany())
                .raisedBy(targetUser) // Tied to target user so they see it in their support tickets
                .assignedTo(currentUser)
                .category(SupportCategory.ACCOUNT_ISSUE)
                .subject("[Support Personalization] Msimamizi ameingia kama " + targetUser.getFirstName() + " " + targetUser.getLastName())
                .description("Msimamizi (" + adminIdentifier + ") ameingia kwenye akaunti ya " + targetUser.getEmail() +
                        " kwa ajili ya msaada na utatuzi wa changamoto.\nSababu: " + reason.trim())
                .relatedEntityType("USER")
                .relatedEntityId(targetUser.getId())
                .priority(Priority.HIGH)
                .status(TicketStatus.RESOLVED)
                .resolutionNotes("Kikao cha msaada (Support Personalization Session) kimeanzishwa na " + adminIdentifier + ". Sababu: " + reason.trim())
                .resolvedAt(OffsetDateTime.now())
                .build();

        SupportTicket savedTicket = supportTicketRepository.save(ticket);

        // 2. Record PostgreSQL Audit Log
        try {
            auditLogService.recordAuditLog(
                    targetUser.getCompany(),
                    currentUser,
                    "USER_IMPERSONATION",
                    "user",
                    targetUser.getId(),
                    null,
                    "{\"action\":\"IMPERSONATE\",\"impersonatedBy\":\"" + adminIdentifier + "\",\"targetUser\":\"" + targetUser.getEmail() + "\",\"ticketId\":\"" + savedTicket.getId() + "\",\"reason\":\"" + reason.trim().replace("\"", "'") + "\"}",
                    "0.0.0.0",
                    "Admin Management Console"
            );
        } catch (Exception ex) {
            log.warn("Could not record audit log for impersonation: {}", ex.getMessage());
        }

        // 3. Generate Impersonation JWT Token for Target User
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", targetUser.getId());
        claims.put("companyId", targetUser.getCompany() != null ? targetUser.getCompany().getId() : null);
        claims.put("userType", "COMPANY_USER");
        claims.put("role_name", targetUser.getRole().getName().toUpperCase().replace(" ", "_"));
        claims.put("mustChangePassword", false); // Do not block support session with password change
        claims.put("isImpersonated", true);
        claims.put("impersonatedBy", adminIdentifier);
        claims.put("ticketId", savedTicket.getId());

        String impersonationToken = jwtService.generateToken(claims, targetUser);

        return java.util.Map.of(
                "token", impersonationToken,
                "ticketId", savedTicket.getId(),
                "ticketSubject", savedTicket.getSubject(),
                "targetUser", mapToResponse(targetUser),
                "impersonatedBy", adminIdentifier,
                "isImpersonated", true,
                "message", "Umeingia kikamilifu kama " + targetUser.getFirstName() + " " + targetUser.getLastName() + ". Tiketi ya msaada #" + savedTicket.getId() + " imerekodiwa kwa uwazi."
        );
    }
}
