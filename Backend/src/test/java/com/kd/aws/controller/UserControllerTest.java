
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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
    // COMMON TEST DATA
    // --------------------------------------------------

    private UserResponse createUserResponse() {

        UserResponse response = new UserResponse();

        response.setId(1L);
        response.setName("Test User");
        response.setEmail("test@gmail.com");

        return response;
    }

    private UserResponse createSecondUserResponse() {

        UserResponse response = new UserResponse();

        response.setId(2L);
        response.setName("Second User");
        response.setEmail("second@gmail.com");

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
    // GET /api/users - PAGINATION
    // --------------------------------------------------

    @Test
    void getAllUsers_shouldReturn200() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> userPage = new PageImpl<>(
                List.of(createUserResponse()),
                pageable,
                1
        );

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test User"))
                .andExpect(jsonPath("$.content[0].email").value("test@gmail.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    void getAllUsers_whenEmpty_shouldReturnEmptyPage() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> emptyPage = Page.empty(pageable);

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.number").value(0));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    // --------------------------------------------------
    // GET /api/users - SORTING
    // --------------------------------------------------

    @Test
    void getAllUsers_shouldSupportSorting() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> userPage = new PageImpl<>(
                List.of(createUserResponse()),
                pageable,
                1
        );

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    void getAllUsers_shouldSupportAscendingSorting() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> userPage = new PageImpl<>(
                List.of(createUserResponse()),
                pageable,
                1
        );

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test User"));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    // --------------------------------------------------
    // GET /api/users - PAGE NAVIGATION
    // --------------------------------------------------

    @Test
    void getAllUsers_shouldReturnRequestedPage() throws Exception {

        Pageable pageable = PageRequest.of(1, 1);

        Page<UserResponse> userPage = new PageImpl<>(
                List.of(createSecondUserResponse()),
                pageable,
                2
        );

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Second User"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));

        verify(userService).getAllUsers(any(Pageable.class));
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
