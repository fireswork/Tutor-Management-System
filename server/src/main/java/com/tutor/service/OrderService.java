package com.tutor.service;

import com.tutor.dto.OrderCreateDTO;
import com.tutor.dto.OrderDTO;
import org.springframework.data.domain.Page;

public interface OrderService {
    
    // 创建订单
    OrderDTO createOrder(OrderCreateDTO orderCreateDTO, Long studentId);
    
    // 获取订单详情
    OrderDTO getOrderById(Long id, Long userId);
    
    // 获取学生订单列表
    Page<OrderDTO> getStudentOrders(Long studentId, String status, String keyword, int page, int size);
    
    // 获取教师订单列表
    Page<OrderDTO> getTeacherOrders(Long teacherId, String status, String keyword, int page, int size);
    
    // 支付订单
    OrderDTO payOrder(Long orderId, Long studentId);
    
    // 取消订单
    OrderDTO cancelOrder(Long orderId, Long userId, String reason);
    
    // 完成订单
    OrderDTO completeOrder(Long orderId, Long teacherId);
} 