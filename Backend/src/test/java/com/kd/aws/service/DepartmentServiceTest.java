
package com.kd.aws.service;

import com.kd.aws.dto.DepartmentDTO;
import com.kd.aws.entity.Department;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.DepartmentMapper;
import com.kd.aws.repository.DepartmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    private DepartmentService departmentService;

    private Department department;
    private DepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {

        departmentService = new DepartmentService(departmentRepository);

        ReflectionTestUtils.setField(
                departmentService,
                "departmentMapper",
                departmentMapper
        );

        department = new Department();
        department.setId(1L);
        department.setName("IT");
        department.setDescription("Information Technology");

        departmentDTO = new DepartmentDTO();
        departmentDTO.setId(1L);
        departmentDTO.setName("IT");
        departmentDTO.setDescription("Information Technology");
    }

    @Test
    void createDepartment_ShouldReturnCreatedDepartment() {

        when(departmentRepository.save(any(Department.class)))
                .thenReturn(department);

        when(departmentMapper.mapToDTO(department))
                .thenReturn(departmentDTO);

        DepartmentDTO result =
                departmentService.createDepartment(departmentDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("IT", result.getName());
        assertEquals("Information Technology", result.getDescription());

        verify(departmentRepository).save(any(Department.class));
        verify(departmentMapper).mapToDTO(department);
    }

    @Test
    void getAllDepartments_ShouldReturnAllDepartments() {

        Department secondDepartment = new Department();
        secondDepartment.setId(2L);
        secondDepartment.setName("HR");
        secondDepartment.setDescription("Human Resources");

        DepartmentDTO secondDTO = new DepartmentDTO();
        secondDTO.setId(2L);
        secondDTO.setName("HR");
        secondDTO.setDescription("Human Resources");

        when(departmentRepository.findAll())
                .thenReturn(List.of(department, secondDepartment));

        when(departmentMapper.mapToDTO(department))
                .thenReturn(departmentDTO);

        when(departmentMapper.mapToDTO(secondDepartment))
                .thenReturn(secondDTO);

        List<DepartmentDTO> result =
                departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("IT", result.get(0).getName());
        assertEquals("HR", result.get(1).getName());

        verify(departmentRepository).findAll();
        verify(departmentMapper, times(2))
                .mapToDTO(any(Department.class));
    }

    @Test
    void getAllDepartments_ShouldReturnEmptyList_WhenNoDepartmentsExist() {

        when(departmentRepository.findAll())
                .thenReturn(List.of());

        List<DepartmentDTO> result =
                departmentService.getAllDepartments();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(departmentRepository).findAll();
        verify(departmentMapper, never()).mapToDTO(any());
    }

    @Test
    void getDepartmentById_ShouldReturnDepartment_WhenDepartmentExists() {

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentMapper.mapToDTO(department))
                .thenReturn(departmentDTO);

        DepartmentDTO result =
                departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("IT", result.getName());

        verify(departmentRepository).findById(1L);
        verify(departmentMapper).mapToDTO(department);
    }

    @Test
    void getDepartmentById_ShouldThrowException_WhenDepartmentDoesNotExist() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(99L)
        );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository).findById(99L);
        verify(departmentMapper, never()).mapToDTO(any());
    }

    @Test
    void updateDepartment_ShouldReturnUpdatedDepartment_WhenDepartmentExists() {

        DepartmentDTO updateDTO = new DepartmentDTO();
        updateDTO.setName("Engineering");
        updateDTO.setDescription("Engineering Department");

        DepartmentDTO updatedResponse = new DepartmentDTO();
        updatedResponse.setId(1L);
        updatedResponse.setName("Engineering");
        updatedResponse.setDescription("Engineering Department");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(department))
                .thenReturn(department);

        when(departmentMapper.mapToDTO(department))
                .thenReturn(updatedResponse);

        DepartmentDTO result =
                departmentService.updateDepartment(1L, updateDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering", result.getName());
        assertEquals("Engineering Department", result.getDescription());

        verify(departmentRepository).findById(1L);
        verify(departmentRepository).save(department);
        verify(departmentMapper).mapToDTO(department);
    }

    @Test
    void updateDepartment_ShouldThrowException_WhenDepartmentDoesNotExist() {

        DepartmentDTO updateDTO = new DepartmentDTO();
        updateDTO.setName("Engineering");

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(99L, updateDTO)
        );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository, never()).save(any());
        verify(departmentMapper, never()).mapToDTO(any());
    }

    @Test
    void deleteDepartment_ShouldDeleteDepartment_WhenDepartmentExists() {

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        assertDoesNotThrow(
                () -> departmentService.deleteDepartment(1L)
        );

        verify(departmentRepository).findById(1L);
        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void deleteDepartment_ShouldThrowException_WhenDepartmentDoesNotExist() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(99L)
        );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository).findById(99L);
        verify(departmentRepository, never()).deleteById(anyLong());
    }
}
