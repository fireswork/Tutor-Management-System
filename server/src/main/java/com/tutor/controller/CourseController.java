package com.tutor.controller;

import com.tutor.dto.CourseCreateDTO;
import com.tutor.dto.CourseDTO;
import com.tutor.dto.QualificationDTO;
import com.tutor.dto.TeacherQualificationsDTO;
import com.tutor.entity.User;
import com.tutor.exception.ResourceNotFoundException;
import com.tutor.service.CourseService;
import com.tutor.service.QualificationService;
import com.tutor.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    private final QualificationService qualificationService;
    private final TeacherService teacherService;
    
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
    
    @GetMapping("/{id}/detail")
    public ResponseEntity<Map<String, Object>> getCourseDetail(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
            
        // 获取教师资质信息
        List<QualificationDTO> qualifications = qualificationService.getUserQualifications(course.getTeacherId());
        
        // 获取教师学历和专业等详细资质信息 - 使用userId而不是teacherId
        TeacherQualificationsDTO teacherQualifications = teacherService.getTeacherQualificationsByUserId(course.getTeacherId());
        
        // 组合返回数据
        Map<String, Object> response = new HashMap<>();
        response.put("course", course);
        response.put("teacherQualifications", qualifications);
        
        // 只返回教师的学历和专业信息
        Map<String, String> teacherEduInfo = new HashMap<>();
        teacherEduInfo.put("education", teacherQualifications.getEducation());
        teacherEduInfo.put("major", teacherQualifications.getMajor());
        response.put("teacherInfo", teacherEduInfo);
        
        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            courseService.deleteCourse(id, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "课程删除成功");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "删除课程失败，请稍后再试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
            throw new RuntimeException("用户未授权");
        }
        return Long.valueOf(authentication.getDetails().toString());
    }
} 