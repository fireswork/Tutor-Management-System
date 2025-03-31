package com.tutor.dto;

import lombok.Data;

@Data
public class ReviewCreateDTO {
    private Long orderId;
    private Double rating;
    private String content;
} 