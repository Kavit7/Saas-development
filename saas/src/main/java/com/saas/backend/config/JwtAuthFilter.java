package com.saas.backend.config;

import java.io.IOException;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;




@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserDetailsService platformAdminDetailsService;

    public JwtAuthFilter(
            JwtService jwtService,
            @Qualifier("userDetailsService")
            UserDetailsService userDetailsService,
            @Qualifier("platformAdminDetailsService")
            UserDetailsService platformAdminUserDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.platformAdminDetailsService = platformAdminUserDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        UserDetails userDetails;

        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        userEmail = jwtService.extractUsername(jwt);

        String userType = jwtService.extractClaim(
                jwt,
                claims -> claims.get("userType", String.class)
        );

        if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            if ("PLATFORM_ADMIN".equals(userType)) {

                userDetails =
                        platformAdminDetailsService
                                .loadUserByUsername(userEmail);

            } else {

                userDetails =
                        userDetailsService
                                .loadUserByUsername(userEmail);
            }

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                auth.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}