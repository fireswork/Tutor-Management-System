package com.tutor.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name = "qualifications")
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;  // 证书名称

    @Column(nullable = false)
    private String type;  // 证书类型: education, teaching, professional, language, other

    @Column(nullable = false)
    private String issuer; // 发证机构

    @Column(columnDefinition = "TEXT")
    private String description; // 备注说明

    @Column(columnDefinition = "LONGTEXT")
    private String fileUrl; // 文件URL或Base64内容
    
    @Column(nullable = false)
    private String status = "pending"; // 状态: pending, approved, rejected

    private String comment; // 审核意见

    private String reviewComment;
    
    private Date reviewDate;

    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
} 