package com.tutor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false, length = 20)
    private String status; // pending(待支付), paid(已支付), completed(已完成), cancelled(已取消)
    
    @Column(nullable = false)
    private LocalDateTime bookingTime; // 预约时间
    
    private LocalDateTime paymentTime; // 支付时间
    
    private LocalDateTime completionTime; // 完成时间
    
    private LocalDateTime cancellationTime; // 取消时间
    
    private String cancellationReason; // 取消原因
    
    @Column(columnDefinition = "TEXT")
    private String remark; // 备注
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "pending";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 