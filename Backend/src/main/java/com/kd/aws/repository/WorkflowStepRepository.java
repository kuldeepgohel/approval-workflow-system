package com.kd.aws.repository;

import com.kd.aws.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep,Long> {
    List<WorkflowStep> findByWorkflowIdOrderByLevelAsc(Long workflowId);
    Optional<WorkflowStep> findByWorkflowIdAndLevel(
            Long workflowId,
            Integer level
    );
}
