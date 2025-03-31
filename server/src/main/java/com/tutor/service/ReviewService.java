package com.tutor.service;

import com.tutor.dto.ReviewCreateDTO;
import com.tutor.dto.ReviewDTO;
import org.springframework.data.domain.Page;

public interface ReviewService {
    
    // 提交评价
    ReviewDTO createReview(ReviewCreateDTO reviewCreateDTO, Long studentId);
    
    // 获取评价详情
    ReviewDTO getReviewById(Long id);
    
    // 获取课程评价列表
    Page<ReviewDTO> getCourseReviews(Long courseId, int page, int size);
    
    // 获取学生提交的评价列表
    Page<ReviewDTO> getStudentReviews(Long studentId, int page, int size);
    
    // 获取教师所有课程的评价列表
    Page<ReviewDTO> getTeacherReviews(Long teacherId, int page, int size);
    
    // 获取订单评价
    ReviewDTO getOrderReview(Long orderId);
    
    // 检查订单是否已评价
    boolean hasReview(Long orderId);
} 