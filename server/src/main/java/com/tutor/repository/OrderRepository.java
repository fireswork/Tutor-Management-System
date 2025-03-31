package com.tutor.repository;

import com.tutor.entity.Course;
import com.tutor.entity.Order;
import com.tutor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 查询学生的所有订单
    Page<Order> findByStudent(User student, Pageable pageable);
    
    // 查询学生的特定状态订单
    Page<Order> findByStudentAndStatus(User student, String status, Pageable pageable);
    
    // 查询教师的所有课程订单
    @Query("SELECT o FROM Order o WHERE o.course.teacher = :teacher")
    Page<Order> findByTeacher(@Param("teacher") User teacher, Pageable pageable);
    
    // 查询教师的特定状态课程订单
    @Query("SELECT o FROM Order o WHERE o.course.teacher = :teacher AND o.status = :status")
    Page<Order> findByTeacherAndStatus(@Param("teacher") User teacher, @Param("status") String status, Pageable pageable);
    
    // 根据课程查询订单
    Page<Order> findByCourse(Course course, Pageable pageable);
    
    // 关键字搜索学生订单 (课程名称或教师名)
    @Query("SELECT o FROM Order o WHERE o.student = :student AND (LOWER(o.course.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.course.teacher.realName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Order> searchStudentOrders(@Param("student") User student, @Param("keyword") String keyword, Pageable pageable);
    
    // 关键字搜索教师订单 (课程名称或学生名)
    @Query("SELECT o FROM Order o WHERE o.course.teacher = :teacher AND (LOWER(o.course.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.student.realName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Order> searchTeacherOrders(@Param("teacher") User teacher, @Param("keyword") String keyword, Pageable pageable);
    
    // 检查订单是否存在
    boolean existsByStudentAndCourseAndStatusNot(User student, Course course, String status);
} 