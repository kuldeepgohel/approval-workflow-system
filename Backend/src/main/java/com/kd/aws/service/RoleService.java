package com.kd.aws.service;

import com.kd.aws.dto.role.CreateRoleRequest;
import com.kd.aws.dto.role.RoleResponse;
import com.kd.aws.dto.role.UpdateRoleRequest;
import com.kd.aws.entity.Role;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.RoleMapper;
import com.kd.aws.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    //create a new role
    public RoleResponse createRole(CreateRoleRequest request){
        Role role = roleMapper.toEntity(request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    //get all roles
    public List<RoleResponse> getAllRoles(){
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    //get role by Id
    public RoleResponse getRoleById(Long id){
        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id:" + id
                        ));
        return roleMapper.toResponse(role);
    }

    // update role
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException(
                                "Role not found with id:" + id
                        ));
        role.setName(request.getName());
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }
    
    //delete the role
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id:" + id
                        ));
        roleRepository.delete(role);
    }
}
