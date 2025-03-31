package com.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    
    // 教师特有字段
    private String education;
    private String major;
    private List<String> subjects;
    private Integer experience;
    private String bio;
} 