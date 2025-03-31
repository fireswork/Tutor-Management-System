package com.tutor.controller;

import com.tutor.dto.UserProfileDTO;
import com.tutor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    @Autowired
    private UserService userService;
    
    /**
     * 获取当前登录用户的个人资料
     */
    @GetMapping
    public ResponseEntity<?> getUserProfile() {
        try {
            // 从SecurityContext中获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 获取用户信息（此处简化处理，实际应该根据用户名查询用户ID）
            Long userId = Long.valueOf(authentication.getDetails().toString());
            
            UserProfileDTO profileDTO = userService.getUserProfile(userId);
            return ResponseEntity.ok(profileDTO);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新用户个人资料
     */
    @PutMapping
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileDTO profileDTO) {
        try {
            // 从SecurityContext中获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.valueOf(authentication.getDetails().toString());
            
            UserProfileDTO updatedProfile = userService.updateUserProfile(userId, profileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 修改用户密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            // 获取请求中的密码数据
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                throw new RuntimeException("Current password and new password are required");
            }
            
            // 从SecurityContext中获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.valueOf(authentication.getDetails().toString());
            
            userService.changePassword(userId, currentPassword, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 