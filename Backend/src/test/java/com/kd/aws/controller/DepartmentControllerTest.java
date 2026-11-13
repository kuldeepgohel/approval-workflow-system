
package com.kd.aws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.aws.dto.DepartmentDTO;
import com.kd.aws.exception.GlobalExceptionHandler;
import com.kd.aws.service.DepartmentService;

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
class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(departmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }


    // --------------------------------------------------
    // Common test data
    // --------------------------------------------------

    private DepartmentDTO createDepartmentDTO() {

        DepartmentDTO dto = new DepartmentDTO();

        dto.setId(1L);
        dto.setName("IT");
        dto.setDescription("Information Technology Department");

        return dto;
    }


    // --------------------------------------------------
    // POST /api/departments
    // --------------------------------------------------

    @Test
    void createDepartment_shouldReturn200() throws Exception {

        DepartmentDTO request = createDepartmentDTO();
        request.setId(null);

        when(departmentService.createDepartment(any(DepartmentDTO.class)))
                .thenReturn(createDepartmentDTO());

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"))
                .andExpect(jsonPath("$.description")
                        .value("Information Technology Department"));

        verify(departmentService).createDepartment(any(DepartmentDTO.class));
    }


    // --------------------------------------------------
    // GET /api/departments
    // --------------------------------------------------

    @Test
    void getAllDepartments_shouldReturn200() throws Exception {

        when(departmentService.getAllDepartments())
                .thenReturn(List.of(createDepartmentDTO()));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("IT"));

        verify(departmentService).getAllDepartments();
    }


    @Test
    void getAllDepartments_whenEmpty_shouldReturnEmptyList() throws Exception {

        when(departmentService.getAllDepartments())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(departmentService).getAllDepartments();
    }


    // --------------------------------------------------
    // GET /api/departments/{id}
    // --------------------------------------------------

    @Test
    void getDepartmentById_shouldReturn200() throws Exception {

        when(departmentService.getDepartmentById(1L))
                .thenReturn(createDepartmentDTO());

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"));

        verify(departmentService).getDepartmentById(1L);
    }


    // --------------------------------------------------
    // PUT /api/departments/{id}
    // --------------------------------------------------

    @Test
    void updateDepartment_shouldReturn200() throws Exception {

        DepartmentDTO request = createDepartmentDTO();
        request.setName("Engineering");

        DepartmentDTO response = createDepartmentDTO();
        response.setName("Engineering");

        when(departmentService.updateDepartment(
                eq(1L),
                any(DepartmentDTO.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"));

        verify(departmentService).updateDepartment(
                eq(1L),
                any(DepartmentDTO.class)
        );
    }


    // --------------------------------------------------
    // DELETE /api/departments/{id}
    // --------------------------------------------------

    @Test
    void deleteDepartment_shouldReturn200() throws Exception {

        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Department Deleted Successfully !"
                ));

        verify(departmentService).deleteDepartment(1L);
    }

}
