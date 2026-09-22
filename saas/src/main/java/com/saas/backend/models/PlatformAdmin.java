package com.saas.backend.models;

/**
 * PlatformAdmin
 */

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity @Table(name="platform_admins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatformAdmin extends BaseEntity implements UserDetails {
    @Column(nullable=false) private String firstName;
    @Column(nullable=false) private String lastName;
    @Column(nullable=false, unique=true) private String email;
    private String phone;
    @Column(nullable=false) private String passwordHash;
    private String passwordResetToken;
    private OffsetDateTime passwordExpire;
    @Enumerated(EnumType.STRING) private PlatformRole platformRole;
    @Enumerated(EnumType.STRING) private PlatformAdminStatus status;
    private OffsetDateTime lastLoginAt;
   
    @Override
    public @Nullable String getPassword() {
        return this.passwordHash;
        
    }
    @Override
    public String getUsername() {
        return this.email;
    }
        @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+this.platformRole));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == PlatformAdminStatus.ACTIVE;
    }
}
