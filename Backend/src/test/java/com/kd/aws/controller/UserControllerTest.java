
package com.kd.aws.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.aws.dto.user.CreateUserRequest;
import com.kd.aws.dto.user.UpdateUserRequest;
import com.kd.aws.dto.user.UserResponse;
import com.kd.aws.exception.GlobalExceptionHandler;
import com.kd.aws.service.UserService;

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
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }


    // --------------------------------------------------
    // Common test data
    // --------------------------------------------------

    private UserResponse createUserResponse() {

        UserResponse response = new UserResponse();

        response.setId(1L);
        response.setName("Test User");
        response.setEmail("test@gmail.com");

        return response;
    }


    // --------------------------------------------------
    // POST /api/users
    // --------------------------------------------------

    @Test
    void createUser_shouldReturn201() throws Exception {

        CreateUserRequest request = new CreateUserRequest();

        request.setName("Test User");
        request.setEmail("test@gmail.com");
        request.setPassword("Password@123");
        request.setDepartmentId(1L);
        request.setRoleId(2L);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(createUserResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }


    // --------------------------------------------------
    // GET /api/users
    // --------------------------------------------------

    @Test
    void getAllUsers_shouldReturn200() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of(createUserResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test User"))
                .andExpect(jsonPath("$[0].email").value("test@gmail.com"));

        verify(userService).getAllUsers();
    }


    @Test
    void getAllUsers_whenEmpty_shouldReturnEmptyList() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }


    // --------------------------------------------------
    // GET /api/users/{id}
    // --------------------------------------------------

    @Test
    void getUserById_shouldReturn200() throws Exception {

        when(userService.getUserById(1L))
                .thenReturn(createUserResponse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));

        verify(userService).getUserById(1L);
    }


    // --------------------------------------------------
    // PATCH /api/users/{id}
    // --------------------------------------------------

    @Test
    void updateUser_shouldReturn200() throws Exception {

        UpdateUserRequest request = new UpdateUserRequest();

        request.setName("Updated User");
        request.setEmail("updated@gmail.com");
        request.setDepartmentId(1L);
        request.setRoleId(2L);
        request.setActive(true);

        UserResponse response = createUserResponse();

        response.setName("Updated User");
        response.setEmail("updated@gmail.com");

        when(userService.updateUser(
                eq(1L),
                any(UpdateUserRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));

        verify(userService).updateUser(
                eq(1L),
                any(UpdateUserRequest.class)
        );
    }


    // --------------------------------------------------
    // DELETE /api/users/{id}
    // --------------------------------------------------

    @Test
    void deleteUser_shouldReturn200() throws Exception {

        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).deleteUser(1L);
    }

}
