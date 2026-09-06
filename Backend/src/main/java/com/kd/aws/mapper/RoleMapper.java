package com.kd.aws.mapper;

import com.kd.aws.dto.role.CreateRoleRequest;
import com.kd.aws.dto.role.RoleResponse;
import com.kd.aws.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toEntity(CreateRoleRequest request){
        Role role = new Role();

        role.setName(request.getName());
        return role;
    }

    public RoleResponse toResponse(Role role){

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
