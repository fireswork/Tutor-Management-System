package com.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
public class TeacherDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String education;
    private List<String> subjects;
    private Integer experience;
    private String status;
    private Map<String, Object> qualifications; // 包含资质信息
} 