package com.tutor.controller;

import com.tutor.dto.ReviewCreateDTO;
import com.tutor.dto.ReviewDTO;
import com.tutor.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 提交评价
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createReview(@RequestBody ReviewCreateDTO reviewCreateDTO) {
        try {
            Long userId = getCurrentUserId();
            ReviewDTO createdReview = reviewService.createReview(reviewCreateDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            ReviewDTO review = reviewService.getReviewById(id);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取课程评价列表
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ReviewDTO> reviews = reviewService.getCourseReviews(courseId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews.getContent());
            response.put("currentPage", reviews.getNumber());
            response.put("totalItems", reviews.getTotalElements());
            response.put("totalPages", reviews.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生提交的评价列表
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getStudentReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Page<ReviewDTO> reviews = reviewService.getStudentReviews(userId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews.getContent());
            response.put("currentPage", reviews.getNumber());
            response.put("totalItems", reviews.getTotalElements());
            response.put("totalPages", reviews.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取教师课程的评价列表
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getTeacherReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Page<ReviewDTO> reviews = reviewService.getTeacherReviews(userId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews.getContent());
            response.put("currentPage", reviews.getNumber());
            response.put("totalItems", reviews.getTotalElements());
            response.put("totalPages", reviews.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取订单评价
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'TEACHER')")
    public ResponseEntity<?> getOrderReview(@PathVariable Long orderId) {
        try {
            ReviewDTO review = reviewService.getOrderReview(orderId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查订单是否已评价
     */
    @GetMapping("/order/{orderId}/exists")
    @PreAuthorize("hasAnyRole('USER', 'TEACHER')")
    public ResponseEntity<?> hasReview(@PathVariable Long orderId) {
        try {
            boolean hasReview = reviewService.hasReview(orderId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("hasReview", hasReview);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新评价
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody ReviewCreateDTO reviewUpdateDTO) {
        try {
            Long userId = getCurrentUserId();
            ReviewDTO updatedReview = reviewService.updateReview(id, reviewUpdateDTO, userId);
            return ResponseEntity.ok(updatedReview);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            reviewService.deleteReview(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "评价已成功删除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            throw new RuntimeException("用户未认证");
        }
        return Long.valueOf(authentication.getDetails().toString());
    }
} 