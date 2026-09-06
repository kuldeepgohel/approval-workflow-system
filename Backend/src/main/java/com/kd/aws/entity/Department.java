package com.kd.aws.entity;

import com.kd.aws.enums.RequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department")
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    @NotBlank(message = "Department name is required.")
    @Size(max = 100, message = "Department name cannot exceed 100 characters.")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    private String description;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    @PrePersist
    public void onCreate(){
        createAt=LocalDateTime.now();
        updateAt=LocalDateTime.now();
    }
    @PreUpdate
    public void onUpdate(){
        updateAt=LocalDateTime.now();
    }
}
