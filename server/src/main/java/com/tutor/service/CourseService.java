package com.tutor.service;

import com.tutor.dto.CourseCreateDTO;
import com.tutor.dto.CourseDTO;
import org.springframework.data.domain.Page;

public interface CourseService {
    
    Page<CourseDTO> getAllCourses(String category, String keyword, int page, int size);
    
    Page<CourseDTO> getTeacherCourses(Long teacherId, String category, String keyword, int page, int size);
    
    CourseDTO getCourseById(Long id);
    
    CourseDTO createCourse(CourseCreateDTO courseCreateDTO, Long teacherId);
    
    CourseDTO updateCourseStatus(Long id, String status);
    
    void deleteCourse(Long id);
} 