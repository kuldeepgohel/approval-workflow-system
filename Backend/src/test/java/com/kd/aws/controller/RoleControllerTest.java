
package com.kd.aws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.aws.dto.role.CreateRoleRequest;
import com.kd.aws.dto.role.RoleResponse;
import com.kd.aws.dto.role.UpdateRoleRequest;
import com.kd.aws.exception.GlobalExceptionHandler;
import com.kd.aws.service.RoleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }


    // --------------------------------------------------
    // Common test data
    // --------------------------------------------------

    private RoleResponse createRoleResponse() {

        RoleResponse response = new RoleResponse();

        response.setId(1L);
        response.setName("ADMIN");

        return response;
    }


    // --------------------------------------------------
    // POST /api/roles
    // --------------------------------------------------

    @Test
    void createRole_shouldReturn201() throws Exception {

        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("ADMIN");

        when(roleService.createRole(any(CreateRoleRequest.class)))
                .thenReturn(createRoleResponse());

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));

        verify(roleService).createRole(any(CreateRoleRequest.class));
    }


    @Test
    void createRole_withEmptyName_shouldReturn400() throws Exception {

        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(roleService);
    }


    // --------------------------------------------------
    // GET /api/roles
    // --------------------------------------------------

    @Test
    void getAllRoles_shouldReturn200() throws Exception {

        when(roleService.getAllRoles())
                .thenReturn(List.of(createRoleResponse()));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));

        verify(roleService).getAllRoles();
    }


    @Test
    void getAllRoles_whenEmpty_shouldReturnEmptyList() throws Exception {

        when(roleService.getAllRoles())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(roleService).getAllRoles();
    }


    // --------------------------------------------------
    // GET /api/roles/{id}
    // --------------------------------------------------

    @Test
    void getRoleById_shouldReturn200() throws Exception {

        when(roleService.getRoleById(1L))
                .thenReturn(createRoleResponse());

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));

        verify(roleService).getRoleById(1L);
    }


    // --------------------------------------------------
    // PATCH /api/roles/{id}
    // --------------------------------------------------

    @Test
    void updateRole_shouldReturn200() throws Exception {

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("MANAGER");

        RoleResponse response = createRoleResponse();
        response.setName("MANAGER");

        when(roleService.updateRole(
                eq(1L),
                any(UpdateRoleRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("MANAGER"));

        verify(roleService).updateRole(
                eq(1L),
                any(UpdateRoleRequest.class)
        );
    }


    @Test
    void updateRole_withEmptyName_shouldReturn400() throws Exception {

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("");

        mockMvc.perform(patch("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(roleService);
    }


    // --------------------------------------------------
    // DELETE /api/roles/{id}
    // --------------------------------------------------

    @Test
    void deleteRole_shouldReturn204() throws Exception {

        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(roleService).deleteRole(1L);
    }

}
