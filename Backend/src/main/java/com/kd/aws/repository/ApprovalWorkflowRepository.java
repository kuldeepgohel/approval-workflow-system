package com.kd.aws.repository;

import com.kd.aws.entity.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {
    Optional<ApprovalWorkflow>
    findByDepartmentIdAndActiveTrue(Long departmentId);
}
