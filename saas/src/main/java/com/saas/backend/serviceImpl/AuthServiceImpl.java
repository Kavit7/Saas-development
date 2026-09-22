package com.saas.backend.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saas.backend.config.JwtService;
import com.saas.backend.dto.LoginRequest;
import com.saas.backend.models.PlatformAdmin;
import com.saas.backend.models.User;
import com.saas.backend.repositories.PlatformAdminRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AuthResponse;
import com.saas.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    // private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PlatformAdminRepository platformAdminRepository;

    @Qualifier("platformAdminAuthenticationProvider")
    private final AuthenticationProvider platformAdminAuthenticationProvider;
    @Qualifier ("companyAuthenticationProvider")
    private final AuthenticationProvider companyAuthenticationProvider ;
    @Override
    public AuthResponse login(LoginRequest request) {

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
            .findByEmail(request.getEmail())
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found")
            );

        // 3. Create JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",user.getId());
        claims.put("companyId", user.getCompany().getId());
        claims.put("userType","COMPANY_USER");
        claims.put("role_name", user.getRole().getName());

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
}

