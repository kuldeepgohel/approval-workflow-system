package com.kd.aws.repository;

import com.kd.aws.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep,Long> {
    List<WorkflowStep> findByWorkflowIdOrderByLevelAsc(Long workflowId);
}
