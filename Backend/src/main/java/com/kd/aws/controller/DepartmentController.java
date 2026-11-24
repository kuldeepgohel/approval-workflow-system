package com.kd.aws.controller;

import com.kd.aws.dto.DepartmentDTO;
import com.kd.aws.entity.Department;
import com.kd.aws.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    //create
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public DepartmentDTO createDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        return departmentService.createDepartment(departmentDTO);
    }

    //get all department
    @GetMapping
    public List<DepartmentDTO> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    //get department
    @GetMapping("/{id}")
    public DepartmentDTO getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public DepartmentDTO updateDepartment
            (@PathVariable Long id, @Valid @RequestBody DepartmentDTO departmentDTO){
        return departmentService.updateDepartment(id,departmentDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteDepartment
            (@PathVariable Long id){
        departmentService.deleteDepartment(id);
        return "Department Deleted Successfully !";
    }
}
