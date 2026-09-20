package com.kd.aws.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "approval_workflows")
@Data
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Boolean active = true;
}
