package com.tutor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateDTO {
    private Long courseId;
    private LocalDateTime bookingTime;
    private String remark;
} 