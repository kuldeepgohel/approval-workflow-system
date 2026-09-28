package com.kd.aws.entity;

import com.kd.aws.enums.ApprovalAction;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_history")
@Data
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalAction action;

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false)
    private String actionBy;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false,updatable = false)
    private LocalDateTime actionDate;

    @PrePersist
    public void onCreate(){
        actionDate = LocalDateTime.now();
    }
}
