package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saas.backend.dto.RoleRequest;
import com.saas.backend.models.Role;
import com.saas.backend.repositories.RoleRepository;
import com.saas.backend.response.RoleResponse;
import com.saas.backend.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private RoleResponse mapToResponse(Role role) {
        if (role == null) return null;
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    @Override
    public void createRole(RoleRequest roleRequest) {
        try {
            if (roleRepository.findByNameIgnoreCase(roleRequest.getName()).isPresent()) {
                throw new RuntimeException("Role with name " + roleRequest.getName() + " already exists");
            }   

            Role role = new Role();
            role.setName(roleRequest.getName());
            role.setDescription(roleRequest.getDescription()); 
            roleRepository.save(role);
        } catch (Exception e) {
            throw new RuntimeException("Error creating role: " + e.getMessage());
        }
    }

    @Override
    public List<RoleResponse> getRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            return roles.stream().map(this::mapToResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
