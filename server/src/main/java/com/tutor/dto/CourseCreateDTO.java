package com.tutor.dto;

import lombok.Data;

@Data
public class CourseCreateDTO {
    private String title;
    
    private String category;
    
    private Integer duration;
    
    private Double price;
    
    private String description;
    
    private String cover;
} 