
package com.kd.aws.repository;

import com.kd.aws.entity.Role;

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
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldSaveRoleSuccessfully() {

        Role role = new Role();
        role.setName("TEST_ROLE");

        Role savedRole = roleRepository.save(role);

        assertNotNull(savedRole.getId());
        assertEquals("TEST_ROLE", savedRole.getName());
    }

    @Test
    void shouldFindRoleById() {

        Role role = new Role();
        role.setName("TEST_ROLE");

        Role savedRole = roleRepository.save(role);

        Optional<Role> result =
                roleRepository.findById(savedRole.getId());

        assertTrue(result.isPresent());
        assertEquals("TEST_ROLE", result.get().getName());
    }

    @Test
    void shouldUpdateRoleSuccessfully() {

        Role role = new Role();
        role.setName("OLD_ROLE");

        Role savedRole = roleRepository.save(role);

        savedRole.setName("UPDATED_ROLE");

        Role updatedRole = roleRepository.save(savedRole);

        assertEquals("UPDATED_ROLE", updatedRole.getName());

        Role fetchedRole = roleRepository
                .findById(updatedRole.getId())
                .orElseThrow();

        assertEquals("UPDATED_ROLE", fetchedRole.getName());
    }

    @Test
    void shouldDeleteRoleSuccessfully() {

        Role role = new Role();
        role.setName("DELETE_ROLE");

        Role savedRole = roleRepository.save(role);

        Long roleId = savedRole.getId();

        roleRepository.deleteById(roleId);

        assertFalse(roleRepository.existsById(roleId));
    }
}
