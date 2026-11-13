
package com.kd.aws.service;

import com.kd.aws.dto.role.CreateRoleRequest;
import com.kd.aws.dto.role.RoleResponse;
import com.kd.aws.dto.role.UpdateRoleRequest;
import com.kd.aws.entity.Role;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.RoleMapper;
import com.kd.aws.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("Manager");

        roleResponse = RoleResponse.builder()
                .id(1L)
                .name("Manager")
                .build();
    }

    @Test
    void createRole_ShouldReturnCreatedRole() {

        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("Manager");

        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.createRole(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Manager", result.getName());

        verify(roleMapper).toEntity(request);
        verify(roleRepository).save(role);
        verify(roleMapper).toResponse(role);
    }

    @Test
    void getAllRoles_ShouldReturnAllRoles() {

        Role secondRole = new Role();
        secondRole.setId(2L);
        secondRole.setName("Admin");

        RoleResponse secondResponse = RoleResponse.builder()
                .id(2L)
                .name("Admin")
                .build();

        when(roleRepository.findAll()).thenReturn(List.of(role, secondRole));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);
        when(roleMapper.toResponse(secondRole)).thenReturn(secondResponse);

        List<RoleResponse> result = roleService.getAllRoles();

        assertEquals(2, result.size());
        assertEquals("Manager", result.get(0).getName());
        assertEquals("Admin", result.get(1).getName());

        verify(roleRepository).findAll();
        verify(roleMapper, times(2)).toResponse(any(Role.class));
    }

    @Test
    void getRoleById_ShouldReturnRole_WhenRoleExists() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.getRoleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Manager", result.getName());

        verify(roleRepository).findById(1L);
        verify(roleMapper).toResponse(role);
    }

    @Test
    void getRoleById_ShouldThrowException_WhenRoleDoesNotExist() {

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getRoleById(99L)
        );

        assertEquals("Role not found with id:99", exception.getMessage());

        verify(roleRepository).findById(99L);
        verify(roleMapper, never()).toResponse(any());
    }

    @Test
    void updateRole_ShouldReturnUpdatedRole_WhenRoleExists() {

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Admin");

        RoleResponse updatedResponse = RoleResponse.builder()
                .id(1L)
                .name("Admin")
                .build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(updatedResponse);

        RoleResponse result = roleService.updateRole(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Admin", result.getName());
        assertEquals("Admin", role.getName());

        verify(roleRepository).findById(1L);
        verify(roleRepository).save(role);
        verify(roleMapper).toResponse(role);
    }

    @Test
    void updateRole_ShouldThrowException_WhenRoleDoesNotExist() {

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Admin");

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.updateRole(99L, request)
        );

        assertEquals("Role not found with id:99", exception.getMessage());

        verify(roleRepository, never()).save(any());
    }

    @Test
    void deleteRole_ShouldDeleteRole_WhenRoleExists() {

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        assertDoesNotThrow(() -> roleService.deleteRole(1L));

        verify(roleRepository).findById(1L);
        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRole_ShouldThrowException_WhenRoleDoesNotExist() {

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.deleteRole(99L)
        );

        assertEquals("Role not found with id:99", exception.getMessage());

        verify(roleRepository, never()).delete(any());
    }
}
