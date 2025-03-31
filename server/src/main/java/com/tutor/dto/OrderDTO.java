package com.tutor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseCover;
    private String teacherName;
    private Double amount;
    private String status;
    private LocalDateTime bookingTime;
    private LocalDateTime paymentTime;
    private LocalDateTime completionTime;
    private LocalDateTime cancellationTime;
    private String cancellationReason;
    private String remark;
    private boolean hasReview;
    private ReviewDTO review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 