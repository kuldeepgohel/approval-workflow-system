
package com.kd.aws.repository;

import com.kd.aws.entity.Department;

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
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void shouldSaveDepartmentSuccessfully() {

        Department department = new Department();
        department.setName("Engineering");
        department.setDescription("Engineering Department");

        Department savedDepartment =
                departmentRepository.save(department);

        assertNotNull(savedDepartment.getId());
        assertEquals("Engineering", savedDepartment.getName());
        assertEquals(
                "Engineering Department",
                savedDepartment.getDescription()
        );
    }

    @Test
    void shouldFindDepartmentById() {

        Department department = new Department();
        department.setName("Engineering");
        department.setDescription("Engineering Department");

        Department savedDepartment =
                departmentRepository.save(department);

        Optional<Department> result =
                departmentRepository.findById(savedDepartment.getId());

        assertTrue(result.isPresent());
        assertEquals("Engineering", result.get().getName());
    }

    @Test
    void shouldUpdateDepartmentSuccessfully() {

        Department department = new Department();
        department.setName("Old Department");
        department.setDescription("Old Description");

        Department savedDepartment =
                departmentRepository.save(department);

        savedDepartment.setName("Updated Department");
        savedDepartment.setDescription("Updated Description");

        departmentRepository.save(savedDepartment);

        Department updatedDepartment =
                departmentRepository.findById(savedDepartment.getId())
                        .orElseThrow();

        assertEquals("Updated Department", updatedDepartment.getName());
        assertEquals(
                "Updated Description",
                updatedDepartment.getDescription()
        );
    }

    @Test
    void shouldDeleteDepartmentSuccessfully() {

        Department department = new Department();
        department.setName("Delete Department");
        department.setDescription("Temporary Department");

        Department savedDepartment =
                departmentRepository.save(department);

        Long departmentId = savedDepartment.getId();

        departmentRepository.deleteById(departmentId);

        assertFalse(departmentRepository.existsById(departmentId));
    }

    @Test
    void shouldReturnEmptyWhenDepartmentDoesNotExist() {

        Optional<Department> result =
                departmentRepository.findById(999999L);

        assertTrue(result.isEmpty());
    }
}
