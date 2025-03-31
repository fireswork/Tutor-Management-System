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
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        Course course = Course.builder()
                .title(courseCreateDTO.getTitle())
                .category(courseCreateDTO.getCategory())
                .duration(courseCreateDTO.getDuration())
                .price(courseCreateDTO.getPrice())
                .description(courseCreateDTO.getDescription())
                .cover(courseCreateDTO.getCover())
                .teacher(teacher)
                .build();
        
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseCreateDTO courseUpdateDTO, Long teacherId) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        // 验证是否是课程的教师
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not authorized to update this course");
        }
        
        // 更新课程信息
        course.setTitle(courseUpdateDTO.getTitle());
        course.setCategory(courseUpdateDTO.getCategory());
        course.setDuration(courseUpdateDTO.getDuration());
        course.setPrice(courseUpdateDTO.getPrice());
        course.setDescription(courseUpdateDTO.getDescription());
        if (courseUpdateDTO.getCover() != null) {
            course.setCover(courseUpdateDTO.getCover());
        }
        
        Course updatedCourse = courseRepository.save(course);
        return convertToDTO(updatedCourse);
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
                .rating(course.getRating())
                .studentCount(course.getStudentCount())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getUsername())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
} 