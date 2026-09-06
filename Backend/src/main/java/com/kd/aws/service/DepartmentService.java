package com.kd.aws.service;

import com.kd.aws.dto.DepartmentDTO;
import com.kd.aws.entity.Department;
import com.kd.aws.exception.ResourceNotFoundException;
import com.kd.aws.mapper.DepartmentMapper;
import com.kd.aws.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    @Autowired
    private DepartmentMapper departmentMapper;
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO){
        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.mapToDTO(savedDepartment);
    }

    public List<DepartmentDTO> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream().map(departmentMapper::mapToDTO).toList();
    }

    public DepartmentDTO getDepartmentById(Long id){
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return departmentMapper.mapToDTO(department);
    }

    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO){
        //find the existing entry from table
        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: "+id));
        //check duplicate name only if the name is being changed
//        if(!department.getName().equalsIgnoreCase(departmentDTO.getName())
//                && departmentRepository.existByName(departmentDTO.getName())){
//            throw  new RuntimeException("Department already exists with name: " + departmentDTO.getName());
//        }
        //set the changed nam & description
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());

        //save to department
        Department updatedDepartment = departmentRepository.save(department);
        return departmentMapper.mapToDTO(updatedDepartment);
    }

    public void deleteDepartment(Long id) {
        //find the existing entry from table
        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found with id: "+id));
        departmentRepository.deleteById(id);
    }
}
