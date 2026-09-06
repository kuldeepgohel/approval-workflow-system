package com.kd.aws.entity;

import com.kd.aws.enums.ApprovalAction;
import com.kd.aws.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvalHistory")
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
    private String approvedBy;

    @Column(nullable = false,updatable = false)
    private LocalDateTime actionDate;

    @PrePersist
    public void onCreate(){
        actionDate = LocalDateTime.now();
    }
}
