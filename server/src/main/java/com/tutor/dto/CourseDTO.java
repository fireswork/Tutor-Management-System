package com.tutor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String category;
    private Integer duration;
    private Double price;
    private String description;
    private String cover;
    private String status;
    private Double rating;
    private Integer studentCount;
    private Long teacherId;
    private String teacherName;
} 