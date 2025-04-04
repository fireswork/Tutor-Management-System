package com.tutor.service.impl;

import com.tutor.dto.CourseCreateDTO;
import com.tutor.dto.CourseDTO;
import com.tutor.entity.Course;
import com.tutor.entity.User;
import com.tutor.exception.ResourceNotFoundException;
import com.tutor.repository.CourseRepository;
import com.tutor.repository.OrderRepository;
import com.tutor.repository.ReviewRepository;
import com.tutor.repository.UserRepository;
import com.tutor.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseServiceImpl implements CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
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
                .orElseThrow(() -> new RuntimeException("未找到教师"));
        
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
    public void deleteCourse(Long id, Long teacherId) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        
        // 验证是否是课程的教师
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("您无权删除该课程");
        }
        
        // 检查订单和评价
        int orderCount = orderRepository.countByCourse(course);
        if (orderCount > 0) {
            throw new RuntimeException("该课程存在关联的订单，无法删除");
        }
        
        int reviewCount = reviewRepository.countByCourse(course);
        if (reviewCount > 0) {
            throw new RuntimeException("该课程存在关联的评价，无法删除");
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
            throw new RuntimeException("您无权更新该课程");
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
                .teacherName(course.getTeacher().getRealName())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
} 