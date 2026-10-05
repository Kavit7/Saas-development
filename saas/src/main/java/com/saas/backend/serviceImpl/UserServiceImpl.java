package com.saas.backend.serviceImpl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.dto.UserStatusRequest;
import com.saas.backend.models.Company;
import com.saas.backend.models.Role;
import com.saas.backend.models.User;
import com.saas.backend.models.UserStatus;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.RoleRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.UserResponse;
import com.saas.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;

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
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public UserResponse createAdmin(UserRequest userRequest) {
        try {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                throw new RuntimeException("User with that email " + userRequest.getEmail() + " Already exists");
            }

            String hashPassword = passwordEncoder.encode(userRequest.getPassword());

            String reqRoleName = userRequest.getRoleName() != null ? userRequest.getRoleName().trim() : "";
            Role role = roleRepository.findByNameIgnoreCase(reqRoleName)
                    .or(() -> reqRoleName.toUpperCase().startsWith("ROLE_")
                            ? roleRepository.findByNameIgnoreCase(reqRoleName.substring(5))
                            : roleRepository.findByNameIgnoreCase("ROLE_" + reqRoleName))
                    .orElseThrow(() -> new RuntimeException("Role Not found: " + reqRoleName));

            Company company = null;
            if (userRequest.getCompanyName() != null && !userRequest.getCompanyName().trim().isEmpty()) {
                company = companyRepository.findByNameIgnoreCase(userRequest.getCompanyName().trim()).orElse(null);
            }

            if (company == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                    User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
                    if (currentUser != null && currentUser.getCompany() != null) {
                        company = currentUser.getCompany();
                    }
                }
            }

            if (company == null) {
                throw new RuntimeException("Company Not found. Please provide a valid company name.");
            }

            Long totalUsers = userRepository.countByCompany(company);

            if (company.getSubscriptionPlan() != null && totalUsers >= company.getSubscriptionPlan().getMaxUsers()) {
                throw new RuntimeException("You can't exceed maximum user plan");
            }

            User user = new User();
            user.setCompany(company);
            user.setEmail(userRequest.getEmail());
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setGender(userRequest.getGender());
            user.setPhone(userRequest.getPhone());
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(role);
            user.setPasswordHash(hashPassword);
            userRepository.save(user);

            return mapToResponse(user);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction) {
        try {
            Sort sort = direction.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
                if (currentUser != null && currentUser.getRole() != null) {
                    String roleName = currentUser.getRole().getName().toUpperCase();
                    if (!roleName.contains("SUPER_ADMIN") && currentUser.getCompany() != null) {
                        return userRepository.findByCompany(currentUser.getCompany(), pageable).map(this::mapToResponse);
                    }
                }
            }

            return userRepository.findAll(pageable).map(this::mapToResponse);
        } catch (Exception e) {
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
            boolean isSuperAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            User currentUser = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (currentUser.getId().equals(targetUser.getId())) {
                throw new AccessDeniedException("You are forbidden to change your own status");
            }

            if (!isSuperAdmin) {
                if (currentUser.getCompany() == null || targetUser.getCompany() == null ||
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
            boolean isSuperAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            User currentUser = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (!isSuperAdmin) {
                if (currentUser.getCompany() == null || targetUser.getCompany() == null ||
                        !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                    throw new AccessDeniedException("You cannot update a user from another company");
                }
            }

            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                targetUser.setFirstName(request.getFirstName().trim());
            }
            if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                targetUser.setLastName(request.getLastName().trim());
            }
            if (request.getPhone() != null) {
                targetUser.setPhone(request.getPhone().trim());
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
                    targetUser.setRole(role);
                }
            }
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                targetUser.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
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
            boolean isSuperAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            User currentUser = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            if (!isSuperAdmin) {
                if (currentUser.getCompany() == null || targetUser.getCompany() == null ||
                        !currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
                    throw new AccessDeniedException("You cannot trigger password reset for a user in another company");
                }
            }

            String tempPassword = "Temp@" + UUID.randomUUID().toString().substring(0, 8) + "!";
            String token = UUID.randomUUID().toString();

            targetUser.setPasswordResetToken(token);
            targetUser.setPasswordExpire(java.time.OffsetDateTime.now().plusHours(1));
            targetUser.setPasswordHash(passwordEncoder.encode(tempPassword));

            if (targetUser.getStatus() != UserStatus.ACTIVE) {
                targetUser.setStatus(UserStatus.ACTIVE);
            }

            userRepository.save(targetUser);

            return java.util.Map.of(
                    "message", "Password reset successfully triggered for " + targetUser.getEmail(),
                    "email", targetUser.getEmail(),
                    "temporaryPassword", tempPassword,
                    "resetToken", token,
                    "expiresAt", targetUser.getPasswordExpire().toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
