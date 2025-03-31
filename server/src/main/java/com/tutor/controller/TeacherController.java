package com.tutor.controller;

import com.tutor.dto.TeacherDTO;
import com.tutor.dto.TeacherQualificationsDTO;
import com.tutor.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    
    /**
     * 获取所有教师列表
     */
    @GetMapping
    public ResponseEntity<?> getAllTeachers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String status) {
        try {
            List<TeacherDTO> teachers;
            if (name != null || subject != null || status != null) {
                teachers = teacherService.getTeachersByFilters(name, subject, status);
            } else {
                teachers = teacherService.getAllTeachers();
            }
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取单个教师信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable Long id) {
        try {
            TeacherDTO teacher = teacherService.getTeacherById(id);
            return ResponseEntity.ok(teacher);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 添加教师（仅管理员可操作）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addTeacher(@RequestBody TeacherDTO teacherDTO) {
        try {
            TeacherDTO savedTeacher = teacherService.addTeacher(teacherDTO);
            return ResponseEntity.ok(savedTeacher);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新教师信息（仅管理员可操作）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody TeacherDTO teacherDTO) {
        try {
            TeacherDTO updatedTeacher = teacherService.updateTeacher(id, teacherDTO);
            return ResponseEntity.ok(updatedTeacher);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除教师（仅管理员可操作）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        try {
            teacherService.deleteTeacher(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Teacher deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取教师资质信息
     */
    @GetMapping("/{id}/qualifications")
    public ResponseEntity<?> getTeacherQualifications(@PathVariable Long id) {
        try {
            TeacherQualificationsDTO qualifications = teacherService.getTeacherQualifications(id);
            return ResponseEntity.ok(qualifications);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 