package com.tutor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
} 