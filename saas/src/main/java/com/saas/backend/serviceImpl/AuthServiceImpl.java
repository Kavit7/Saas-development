package com.saas.backend.serviceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.saas.backend.config.JwtService;
import com.saas.backend.dto.LoginRequest;
import com.saas.backend.models.User;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AuthResponse;
import com.saas.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {

        // 1. Check email and password
        try {

            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

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

        claims.put("companyId", user.getCompany().getId());
        claims.put("role_id", user.getRole().getId());
        claims.put("role_name", user.getRole().getName());

        // 4. Generate JWT
        String token = jwtService.generateToken(claims, user);

        // 5. Return response
        return new AuthResponse(token);
    }
}

