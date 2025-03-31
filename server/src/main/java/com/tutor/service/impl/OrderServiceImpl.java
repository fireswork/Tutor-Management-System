package com.tutor.service.impl;

import com.tutor.dto.OrderCreateDTO;
import com.tutor.dto.OrderDTO;
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
import com.tutor.service.OrderService;
import com.tutor.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ReviewService reviewService;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO orderCreateDTO, Long studentId) {
        // 获取学生信息
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + studentId));
        
        // 获取课程信息
        Course course = courseRepository.findById(orderCreateDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + orderCreateDTO.getCourseId()));
        
        // 检查是否已有未取消的订单
        boolean hasActiveOrder = orderRepository.existsByStudentAndCourseAndStatusNot(student, course, "cancelled");
        if (hasActiveOrder) {
            throw new IllegalStateException("You already have an active order for this course");
        }
        
        // 创建订单
        Order order = Order.builder()
                .course(course)
                .student(student)
                .amount(course.getPrice())
                .status("pending")
                .bookingTime(orderCreateDTO.getBookingTime())
                .remark(orderCreateDTO.getRemark())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        return convertToDTO(savedOrder, false, null);
    }

    @Override
    public OrderDTO getOrderById(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        // 检查权限 - 只有订单学生或课程教师可以查看
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (!order.getStudent().getId().equals(userId) && !order.getCourse().getTeacher().getId().equals(userId)) {
            throw new IllegalStateException("You don't have permission to view this order");
        }
        
        // 检查是否有评价
        boolean hasReview = reviewRepository.existsByOrder(order);
        ReviewDTO reviewDTO = null;
        
        if (hasReview) {
            reviewDTO = reviewService.getOrderReview(id);
        }
        
        return convertToDTO(order, hasReview, reviewDTO);
    }

    @Override
    public Page<OrderDTO> getStudentOrders(Long studentId, String status, String keyword, int page, int size) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + studentId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 按关键字搜索
            orders = orderRepository.searchStudentOrders(student, keyword.trim(), pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            // 按状态过滤
            orders = orderRepository.findByStudentAndStatus(student, status.trim(), pageable);
        } else {
            // 获取所有订单
            orders = orderRepository.findByStudent(student, pageable);
        }
        
        return orders.map(order -> {
            boolean hasReview = reviewRepository.existsByOrder(order);
            ReviewDTO reviewDTO = null;
            
            if (hasReview) {
                reviewDTO = reviewService.getOrderReview(order.getId());
            }
            
            return convertToDTO(order, hasReview, reviewDTO);
        });
    }

    @Override
    public Page<OrderDTO> getTeacherOrders(Long teacherId, String status, String keyword, int page, int size) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + teacherId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 按关键字搜索
            orders = orderRepository.searchTeacherOrders(teacher, keyword.trim(), pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            // 按状态过滤
            orders = orderRepository.findByTeacherAndStatus(teacher, status.trim(), pageable);
        } else {
            // 获取所有订单
            orders = orderRepository.findByTeacher(teacher, pageable);
        }
        
        return orders.map(order -> {
            boolean hasReview = reviewRepository.existsByOrder(order);
            ReviewDTO reviewDTO = null;
            
            if (hasReview) {
                reviewDTO = reviewService.getOrderReview(order.getId());
            }
            
            return convertToDTO(order, hasReview, reviewDTO);
        });
    }

    @Override
    @Transactional
    public OrderDTO payOrder(Long orderId, Long studentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // 验证学生身份
        if (!order.getStudent().getId().equals(studentId)) {
            throw new IllegalStateException("You don't have permission to pay this order");
        }
        
        // 验证订单状态
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Order is not in pending status");
        }
        
        // 支付逻辑 (实际项目中应该对接支付平台)
        order.setStatus("paid");
        order.setPaymentTime(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        // 检查是否有评价（这里应该是假的，因为刚刚支付）
        boolean hasReview = reviewRepository.existsByOrder(updatedOrder);
        ReviewDTO reviewDTO = null;
        
        return convertToDTO(updatedOrder, hasReview, reviewDTO);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // 验证权限 - 学生可以取消pending订单，教师可以取消paid订单
        boolean isStudent = order.getStudent().getId().equals(userId);
        boolean isTeacher = order.getCourse().getTeacher().getId().equals(userId);
        
        if (!isStudent && !isTeacher) {
            throw new IllegalStateException("You don't have permission to cancel this order");
        }
        
        if (isStudent && !"pending".equals(order.getStatus())) {
            throw new IllegalStateException("Student can only cancel pending orders");
        }
        
        if (isTeacher && !"paid".equals(order.getStatus())) {
            throw new IllegalStateException("Teacher can only cancel paid orders");
        }
        
        // 取消订单
        order.setStatus("cancelled");
        order.setCancellationTime(LocalDateTime.now());
        order.setCancellationReason(reason);
        
        Order updatedOrder = orderRepository.save(order);
        
        return convertToDTO(updatedOrder, false, null);
    }

    @Override
    @Transactional
    public OrderDTO completeOrder(Long orderId, Long teacherId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // 验证教师身份
        if (!order.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new IllegalStateException("You don't have permission to complete this order");
        }
        
        // 验证订单状态
        if (!"paid".equals(order.getStatus())) {
            throw new IllegalStateException("Order is not in paid status");
        }
        
        // 完成订单
        order.setStatus("completed");
        order.setCompletionTime(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        
        return convertToDTO(updatedOrder, false, null);
    }
    
    // 辅助方法 - 转换为DTO
    private OrderDTO convertToDTO(Order order, boolean hasReview, ReviewDTO reviewDTO) {
        return OrderDTO.builder()
                .id(order.getId())
                .courseId(order.getCourse().getId())
                .courseTitle(order.getCourse().getTitle())
                .courseCover(order.getCourse().getCover())
                .teacherName(order.getCourse().getTeacher().getRealName())
                .amount(order.getAmount())
                .status(order.getStatus())
                .bookingTime(order.getBookingTime())
                .paymentTime(order.getPaymentTime())
                .completionTime(order.getCompletionTime())
                .cancellationTime(order.getCancellationTime())
                .cancellationReason(order.getCancellationReason())
                .remark(order.getRemark())
                .hasReview(hasReview)
                .review(reviewDTO)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
} 