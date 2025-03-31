package com.tutor.service.impl;

import com.tutor.dto.CourseCreateDTO;
import com.tutor.dto.CourseDTO;
import com.tutor.entity.Course;
import com.tutor.entity.User;
import com.tutor.exception.ResourceNotFoundException;
import com.tutor.repository.CourseRepository;
import com.tutor.repository.UserRepository;
import com.tutor.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    @Override
    public Page<CourseDTO> getAllCourses(String category, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseRepository.findByFilters(category, keyword, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public Page<CourseDTO> getTeacherCourses(Long teacherId, String category, String keyword, int page, int size) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseRepository.findByTeacherAndFilters(teacher, category, keyword, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return convertToDTO(course);
    }
    
    @Override
    @Transactional
    public CourseDTO createCourse(CourseCreateDTO courseCreateDTO, Long teacherId) {
        // 手动验证
        if (courseCreateDTO.getTitle() == null || courseCreateDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("课程标题不能为空");
        }
        
        if (courseCreateDTO.getCategory() == null || courseCreateDTO.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("课程分类不能为空");
        }
        
        if (courseCreateDTO.getDuration() == null || courseCreateDTO.getDuration() < 30) {
            throw new IllegalArgumentException("课程时长不能小于30分钟");
        }
        
        if (courseCreateDTO.getPrice() == null || courseCreateDTO.getPrice() < 0) {
            throw new IllegalArgumentException("课程价格不能为负数");
        }
        
        if (courseCreateDTO.getDescription() == null || courseCreateDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("课程描述不能为空");
        }
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        
        Course course = Course.builder()
                .title(courseCreateDTO.getTitle())
                .category(courseCreateDTO.getCategory())
                .duration(courseCreateDTO.getDuration())
                .price(courseCreateDTO.getPrice())
                .description(courseCreateDTO.getDescription())
                .cover(courseCreateDTO.getCover())
                .status("pending")
                .rating(5.0)
                .studentCount(0)
                .teacher(teacher)
                .build();
        
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }
    
    @Override
    @Transactional
    public CourseDTO updateCourseStatus(Long id, String status) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        course.setStatus(status);
        Course updatedCourse = courseRepository.save(course);
        return convertToDTO(updatedCourse);
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
    
    private CourseDTO convertToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .category(course.getCategory())
                .duration(course.getDuration())
                .price(course.getPrice())
                .description(course.getDescription())
                .cover(course.getCover())
                .status(course.getStatus())
                .rating(course.getRating())
                .studentCount(course.getStudentCount())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getUsername())
                .build();
    }
} 