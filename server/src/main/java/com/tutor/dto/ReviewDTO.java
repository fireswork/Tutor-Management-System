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
public class ReviewDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseCover;
    private String teacherName;
    private Long orderId;
    private String studentName;
    private Double rating;
    private String content;
    private LocalDateTime createdAt;
} 