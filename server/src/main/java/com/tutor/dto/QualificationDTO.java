package com.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class QualificationDTO {
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private String issuer;
    private String description;
    private String fileUrl;
    private String status;
    private String comment;
    private String uploadTime;
    private String reviewComment;
    private String reviewDate;
} 