package com.saas.backend.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.backend.config.JwtService;
import com.saas.backend.dto.ChangePasswordRequest;
import com.saas.backend.dto.LoginRequest;
import com.saas.backend.models.PlatformAdmin;
import com.saas.backend.models.User;
import com.saas.backend.repositories.PlatformAdminRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AuthResponse;
import com.saas.backend.response.UserResponse;
import com.saas.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    // private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PlatformAdminRepository platformAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Qualifier("platformAdminAuthenticationProvider")
    private final AuthenticationProvider platformAdminAuthenticationProvider;
    @Qualifier ("companyAuthenticationProvider")
    private final AuthenticationProvider companyAuthenticationProvider ;
    @Override
    public AuthResponse login(LoginRequest request) {

        // Check if email belongs to a Platform Admin
        if (platformAdminRepository.findByEmail(request.getEmail()).isPresent()) {
            return platformAdminlogin(request);
        }

        // 1. Check email and password
        try {

            companyAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        }
        catch(DisabledException e){
            throw new RuntimeException("Please contact your provider to check your account. " + "Your account may be blocked or suspended");
        } catch (BadCredentialsException e) {

            throw new BadCredentialsException(
                "Invalid email or password"
            );
        }

        // 2. Get authenticated user
        User user = userRepository
            .findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found")
            );

        // Check if temporary default password has expired (3 days TTL)
        if (user.getMustChangePassword() != null && user.getMustChangePassword()) {
            if (user.getPasswordExpire() != null && user.getPasswordExpire().isBefore(java.time.OffsetDateTime.now())) {
                throw new DisabledException("Your temporary default password has expired (validity was 3 days). Please request a password reset from your administrator or support.");
            }
        }

        // 3. Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",user.getId());
        claims.put("companyId", user.getCompany().getId());
        claims.put("userType","COMPANY_USER");
        claims.put("role_name", user.getRole().getName().toUpperCase().replace(" ", "_"));
        claims.put("mustChangePassword", user.getMustChangePassword() != null ? user.getMustChangePassword() : false);

        // 4. Generate JWT
        String token = jwtService.generateToken(claims, user);
        // 5. Return response
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse platformAdminlogin(LoginRequest request) {
        // 1. Check email and password
        try {

            platformAdminAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        }
        catch(DisabledException e){
            throw new RuntimeException("Please contact your provider to check your account. " + "Your account may be blocked or suspended");
        } catch (BadCredentialsException e) {

            throw new BadCredentialsException(
                "Invalid email or password"
            );
        }

        // 2. Get authenticated user
        PlatformAdmin platformAdmin = platformAdminRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found")
            );

        // 3. Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",platformAdmin.getId());
        claims.put("role_name", platformAdmin.getPlatformRole());
        claims.put("userType","PLATFORM_ADMIN");

        // 4. Generate JWT
        String token = jwtService.generateToken(claims, platformAdmin);
        // 5. Return response
        return new AuthResponse(token);
    }

    @Override
    public UserResponse getCurrentUserProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
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

        PlatformAdmin pa = platformAdminRepository.findByEmail(auth.getName()).orElse(null);
        if (pa != null) {
            return UserResponse.builder()
                    .id(pa.getId())
                    .email(pa.getEmail())
                    .firstName(pa.getFirstName())
                    .lastName(pa.getLastName())
                    .phone(pa.getPhone())
                    .role(pa.getPlatformRole() != null ? pa.getPlatformRole().name() : "SUPER_ADMIN")
                    .status(com.saas.backend.models.UserStatus.ACTIVE)
                    .companyId(null)
                    .companyName("PLATFORM_ADMIN")
                    .mustChangePassword(false)
                    .passwordExpire(pa.getPasswordExpire())
                    .createdAt(pa.getCreatedAt())
                    .updatedAt(pa.getUpdatedAt())
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + auth.getName());
    }

    @Override
    public Map<String, Object> changePassword(ChangePasswordRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters long.");
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
            // Once changed by user, it's permanent and never expires after 3 days!
            user.setPasswordExpire(null);
            user.setMustChangePassword(false);
            userRepository.save(user);
            return Map.of("message", "Password successfully changed. You may now continue using the system.");
        }

        PlatformAdmin pa = platformAdminRepository.findByEmail(auth.getName()).orElse(null);
        if (pa != null) {
            pa.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
            pa.setPasswordExpire(null);
            platformAdminRepository.save(pa);
            return Map.of("message", "Password successfully changed. You may now continue using the system.");
        }

        throw new UsernameNotFoundException("User not found: " + auth.getName());
    }
}

