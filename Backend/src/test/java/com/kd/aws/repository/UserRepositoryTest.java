
package com.kd.aws.repository;

import com.kd.aws.entity.Department;
import com.kd.aws.entity.Role;
import com.kd.aws.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User createUser() {

        Role role = new Role();
        role.setName("TEST_USER_ROLE");
        role = roleRepository.save(role);

        Department department = new Department();
        department.setName("TEST_DEPARTMENT");
        department.setDescription("Department for testing");
        department = departmentRepository.save(department);

        User user = new User();
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(true);

        return userRepository.save(user);
    }

    @Test
    void shouldSaveUserSuccessfully() {

        User savedUser = createUser();

        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getName());
        assertEquals("testuser@example.com", savedUser.getEmail());
        assertEquals("TEST_USER_ROLE", savedUser.getRole().getName());
        assertEquals("TEST_DEPARTMENT", savedUser.getDepartment().getName());
        assertTrue(savedUser.getActive());
    }

    @Test
    void shouldFindUserById() {

        User savedUser = createUser();

        Optional<User> result =
                userRepository.findById(savedUser.getId());

        assertTrue(result.isPresent());
        assertEquals("testuser@example.com", result.get().getEmail());
    }

    @Test
    void shouldFindUserByEmail() {

        User savedUser = createUser();

        Optional<User> result =
                userRepository.findByEmail(savedUser.getEmail());

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        User savedUser = createUser();

        savedUser.setName("Updated User");
        savedUser.setEmail("updated@example.com");

        userRepository.save(savedUser);

        User updatedUser = userRepository
                .findById(savedUser.getId())
                .orElseThrow();

        assertEquals("Updated User", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldDeleteUserSuccessfully() {

        User savedUser = createUser();

        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {

        Optional<User> result =
                userRepository.findByEmail("unknown@example.com");

        assertTrue(result.isEmpty());
    }
}
