package com.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class UserProfileDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime registerTime;
    
    // 用于密码修改
    private String currentPassword;
    private String newPassword;
} 