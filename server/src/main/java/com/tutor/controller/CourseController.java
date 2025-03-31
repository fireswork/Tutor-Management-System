package com.tutor.controller;

import com.tutor.dto.CourseCreateDTO;
import com.tutor.dto.CourseDTO;
import com.tutor.entity.User;
import com.tutor.service.CourseService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        Page<CourseDTO> coursePage = courseService.getAllCourses(category, keyword, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("courses", coursePage.getContent());
        response.put("currentPage", coursePage.getNumber());
        response.put("totalItems", coursePage.getTotalElements());
        response.put("totalPages", coursePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeacherCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        Long userId = getCurrentUserId();
        
        Page<CourseDTO> coursePage = courseService.getTeacherCourses(userId, category, keyword, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("courses", coursePage.getContent());
        response.put("currentPage", coursePage.getNumber());
        response.put("totalItems", coursePage.getTotalElements());
        response.put("totalPages", coursePage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO courseDTO = courseService.getCourseById(id);
        return ResponseEntity.ok(courseDTO);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createCourse(@RequestBody CourseCreateDTO courseCreateDTO) {
        // 从SecurityContext获取当前用户ID
        Long teacherId = getCurrentUserId();
        
        CourseDTO createdCourse = courseService.createCourse(courseCreateDTO, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        courseService.deleteCourse(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseCreateDTO courseUpdateDTO) {
        Long userId = getCurrentUserId();
        CourseDTO updatedCourse = courseService.updateCourse(id, courseUpdateDTO, userId);
        return ResponseEntity.ok(updatedCourse);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return Long.valueOf(authentication.getDetails().toString());
    }
} 