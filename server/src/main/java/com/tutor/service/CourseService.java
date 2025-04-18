package com.tutor.service;

import com.tutor.dto.CourseCreateDTO;
import com.tutor.dto.CourseDTO;
import org.springframework.data.domain.Page;

public interface CourseService {
    
    Page<CourseDTO> getAllCourses(String category, String keyword, int page, int size);
    
    Page<CourseDTO> getTeacherCourses(Long teacherId, String category, String keyword, int page, int size);
    
    CourseDTO getCourseById(Long id);
    
    CourseDTO createCourse(CourseCreateDTO courseCreateDTO, Long teacherId);
    
    CourseDTO updateCourse(Long id, CourseCreateDTO courseUpdateDTO, Long teacherId);
    
    void deleteCourse(Long id, Long teacherId);
} 