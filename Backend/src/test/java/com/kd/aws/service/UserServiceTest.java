
package com.kd.aws.service;

import com.kd.aws.dto.user.CreateUserRequest;
import com.kd.aws.dto.user.UpdateUserRequest;
import com.kd.aws.dto.user.UserResponse;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Role;
import com.kd.aws.entity.User;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.UserMapper;
import com.kd.aws.repository.DepartmentRepository;
import com.kd.aws.repository.RoleRepository;
import com.kd.aws.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Department department;
    private Role role;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {

        department = new Department();
        department.setId(1L);
        department.setName("IT");

        role = new Role();
        role.setId(1L);
        role.setName("Manager");

        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setDepartment(department);
        user.setRole(role);
        user.setActive(true);

        userResponse = UserResponse.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .departmentId(1L)
                .departmentName("IT")
                .roleId(1L)
                .roleName("Manager")
                .active(true)
                .build();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {

        CreateUserRequest request = new CreateUserRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setDepartmentId(1L);
        request.setRoleId(1L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertTrue(result.getActive());

        verify(departmentRepository).findById(1L);
        verify(roleRepository).findById(1L);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(user);
    }

    @Test
    void createUser_ShouldThrowException_WhenDepartmentDoesNotExist() {

        CreateUserRequest request = new CreateUserRequest();
        request.setDepartmentId(99L);
        request.setRoleId(1L);

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(roleRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrowException_WhenRoleDoesNotExist() {

        CreateUserRequest request = new CreateUserRequest();
        request.setDepartmentId(1L);
        request.setRoleId(99L);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(roleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        assertEquals(
                "Role not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setName("Alice");

        UserResponse secondResponse = UserResponse.builder()
                .id(2L)
                .name("Alice")
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user, secondUser));

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        when(userMapper.toResponse(secondUser))
                .thenReturn(secondResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Alice", result.get(1).getName());

        verify(userRepository).findAll();
        verify(userMapper, times(2)).toResponse(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {

        when(userRepository.findAll())
                .thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(99L)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("John Updated");
        request.setEmail("john.updated@example.com");
        request.setDepartmentId(1L);
        request.setRoleId(1L);
        request.setActive(false);

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .name("John Updated")
                .email("john.updated@example.com")
                .departmentId(1L)
                .departmentName("IT")
                .roleId(1L)
                .roleName("Manager")
                .active(false)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(updatedResponse);

        UserResponse result = userService.updateUser(1L, request);

        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());
        assertFalse(result.getActive());

        verify(userRepository).findById(1L);
        verify(departmentRepository).findById(1L);
        verify(roleRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserDoesNotExist() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(1L);
        request.setRoleId(1L);

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(99L, request)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository, never()).findById(anyLong());
        verify(roleRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenDepartmentDoesNotExist() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(99L);
        request.setRoleId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(1L, request)
        );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenRoleDoesNotExist() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(1L);
        request.setRoleId(99L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(roleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(1L, request)
        );

        assertEquals(
                "Role not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(99L)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository, never()).delete(any());
    }
}
