package com.saas.backend.service;

import java.util.List;

import com.saas.backend.dto.RoleRequest;
import com.saas.backend.response.RoleResponse;

public interface RoleService {

    public void createRole(RoleRequest roleRequest);

    public List<RoleResponse> getRoles();
    
}
