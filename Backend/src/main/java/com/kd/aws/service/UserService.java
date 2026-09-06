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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserResponse createUser(CreateUserRequest request) {

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + request.getDepartmentId()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: "
                                        + request.getRoleId()));

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDepartment(department);
        user.setRole(role);
        user.setActive(true);

        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + request.getDepartmentId()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: "
                                        + request.getRoleId()));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDepartment(department);
        user.setRole(role);
        user.setActive(request.getActive());

        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));

        userRepository.delete(user);
    }
}
