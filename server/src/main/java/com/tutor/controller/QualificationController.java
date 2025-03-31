package com.tutor.controller;

import com.tutor.dto.QualificationDTO;
import com.tutor.service.QualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/qualifications")
public class QualificationController {

    @Autowired
    private QualificationService qualificationService;
    
    /**
     * 获取当前用户的所有资质证书
     */
    @GetMapping
    public ResponseEntity<?> getUserQualifications() {
        try {
            Long userId = getCurrentUserId();
            List<QualificationDTO> qualifications = qualificationService.getUserQualifications(userId);
            return ResponseEntity.ok(qualifications);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取当前用户指定状态的资质证书
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUserQualificationsByStatus(@PathVariable String status) {
        try {
            Long userId = getCurrentUserId();
            List<QualificationDTO> qualifications = qualificationService.getUserQualificationsByStatus(userId, status);
            return ResponseEntity.ok(qualifications);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 上传新的资质证书
     */
    @PostMapping
    public ResponseEntity<?> addQualification(@RequestBody QualificationDTO qualificationDTO) {
        try {
            Long userId = getCurrentUserId();
            QualificationDTO savedQualification = qualificationService.addQualification(userId, qualificationDTO);
            return ResponseEntity.ok(savedQualification);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除资质证书
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQualification(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            qualificationService.deleteQualification(userId, id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Qualification deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 从SecurityContext获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            throw new RuntimeException("用户未授权");
        }
        return Long.valueOf(authentication.getDetails().toString());
    }
} 