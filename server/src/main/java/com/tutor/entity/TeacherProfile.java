package com.tutor.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String education;
    private String major;

    @Column(columnDefinition = "TEXT")
    private String subjects; // 存储为逗号分隔的字符串
    
    private Integer experience;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    // 审核状态: PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)
    @Column(nullable = false)
    private String status = "PENDING";
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
} 