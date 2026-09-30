package com.kd.aws.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "workflow_steps", uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"workflow_id","level"}
        )
})
@Data
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // many workflow steps can use one approval workflow
    @JoinColumn(name = "workflow_id", nullable = false)
    private ApprovalWorkflow workflow;

    @Column(nullable = false)
    private Integer level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
