package com.tutor.controller;

import com.tutor.dto.QualificationDTO;
import com.tutor.dto.QualificationReviewDTO;
import com.tutor.service.QualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/qualifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQualificationController {

    @Autowired
    private QualificationService qualificationService;
    
    /**
     * 获取待审核的资质列表
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingQualifications() {
        try {
            return ResponseEntity.ok(qualificationService.getPendingQualifications());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 审核资质
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewQualification(
            @PathVariable Long id,
            @RequestBody QualificationReviewDTO reviewDTO) {
        try {
            return ResponseEntity.ok(qualificationService.reviewQualification(id, reviewDTO));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 批量审核资质
     */
    @PutMapping("/batch-review")
    public ResponseEntity<?> batchReviewQualifications(
            @RequestBody Map<Long, QualificationReviewDTO> reviewsMap) {
        try {
            Map<Long, Object> results = qualificationService.batchReviewQualifications(reviewsMap);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取已审核的资质列表
     */
    @GetMapping("/reviewed")
    public ResponseEntity<?> getReviewedQualifications() {
        try {
            return ResponseEntity.ok(qualificationService.getReviewedQualifications());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 