package com.tutor.service.impl;

import com.tutor.dto.ReviewCreateDTO;
import com.tutor.dto.ReviewDTO;
import com.tutor.entity.Course;
import com.tutor.entity.Order;
import com.tutor.entity.Review;
import com.tutor.entity.User;
import com.tutor.exception.ResourceNotFoundException;
import com.tutor.repository.CourseRepository;
import com.tutor.repository.OrderRepository;
import com.tutor.repository.ReviewRepository;
import com.tutor.repository.UserRepository;
import com.tutor.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public ReviewDTO createReview(ReviewCreateDTO reviewCreateDTO, Long studentId) {
        // 获取订单信息
        Order order = orderRepository.findById(reviewCreateDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + reviewCreateDTO.getOrderId()));
        
        // 验证学生身份
        if (!order.getStudent().getId().equals(studentId)) {
            throw new IllegalStateException("You don't have permission to review this order");
        }
        
        // 验证订单状态
        if (!"completed".equals(order.getStatus())) {
            throw new IllegalStateException("You can only review completed orders");
        }
        
        // 检查是否已经评价过
        if (reviewRepository.existsByOrder(order)) {
            throw new IllegalStateException("You have already reviewed this order");
        }
        
        // 创建评价
        Review review = Review.builder()
                .course(order.getCourse())
                .student(order.getStudent())
                .order(order)
                .rating(reviewCreateDTO.getRating())
                .content(reviewCreateDTO.getContent())
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // 更新课程评分
        updateCourseRating(order.getCourse());
        
        return convertToDTO(savedReview);
    }
    
    @Override
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        
        return convertToDTO(review);
    }
    
    @Override
    public Page<ReviewDTO> getCourseReviews(Long courseId, int page, int size) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return reviewRepository.findByCourse(course, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public Page<ReviewDTO> getStudentReviews(Long studentId, int page, int size) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + studentId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return reviewRepository.findByStudent(student, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public Page<ReviewDTO> getTeacherReviews(Long teacherId, int page, int size) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + teacherId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return reviewRepository.findByTeacher(teacher, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    public ReviewDTO getOrderReview(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        Review review = reviewRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for order with id: " + orderId));
        
        return convertToDTO(review);
    }
    
    @Override
    public boolean hasReview(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        return reviewRepository.existsByOrder(order);
    }
    
    // 辅助方法 - 转换为DTO
    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .courseId(review.getCourse().getId())
                .courseTitle(review.getCourse().getTitle())
                .orderId(review.getOrder().getId())
                .studentName(review.getStudent().getRealName())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
    
    // 更新课程评分
    @Transactional
    private void updateCourseRating(Course course) {
        Double avgRating = reviewRepository.calculateAverageRating(course);
        
        if (avgRating != null) {
            course.setRating(avgRating);
            courseRepository.save(course);
        }
    }
} 