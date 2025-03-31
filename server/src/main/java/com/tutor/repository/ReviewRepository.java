package com.tutor.repository;

import com.tutor.entity.Course;
import com.tutor.entity.Order;
import com.tutor.entity.Review;
import com.tutor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 查询课程的所有评价
    Page<Review> findByCourse(Course course, Pageable pageable);
    
    // 查询学生的所有评价
    Page<Review> findByStudent(User student, Pageable pageable);
    
    // 查询教师课程的所有评价
    @Query("SELECT r FROM Review r WHERE r.course.teacher = :teacher")
    Page<Review> findByTeacher(@Param("teacher") User teacher, Pageable pageable);
    
    // 计算课程的平均评分
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course = :course")
    Double calculateAverageRating(@Param("course") Course course);
    
    // 根据订单查找评价
    Optional<Review> findByOrder(Order order);
    
    // 检查订单是否已经评价
    boolean existsByOrder(Order order);
    
    // 统计课程评价数量
    int countByCourse(Course course);
} 