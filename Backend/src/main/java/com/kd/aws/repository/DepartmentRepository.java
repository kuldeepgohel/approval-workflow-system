package com.kd.aws.repository;

import com.kd.aws.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Long> {
//    boolean existByName(String name);
}
