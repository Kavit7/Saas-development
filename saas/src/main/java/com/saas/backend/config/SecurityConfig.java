package com.saas.backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration 
@EnableMethodSecurity 
@EnableWebSecurity 
@RequiredArgsConstructor 
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;



  @Qualifier("companyAuthenticationProvider")
  private final AuthenticationProvider companyAuthenticationProvider;
  @Qualifier("platformAdminAuthenticationProvider")
  private final AuthenticationProvider platformAdminAuthenticationProvider;





  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {})
            .authorizeHttpRequests(auth->auth
            .requestMatchers("/api/v1/auth/**","/docs/**","/swagger-ui/**","/v3/api-docs/**")
            .permitAll()
            .anyRequest()
            .authenticated())
            .sessionManagement(session->session
            .sessionCreationPolicy( SessionCreationPolicy.STATELESS))
            .authenticationProvider(companyAuthenticationProvider)
            .authenticationProvider(platformAdminAuthenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();   
}
}
