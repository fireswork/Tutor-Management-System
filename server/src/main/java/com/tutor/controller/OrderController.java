package com.tutor.controller;

import com.tutor.dto.OrderCreateDTO;
import com.tutor.dto.OrderDTO;
import com.tutor.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        try {
            Long userId = getCurrentUserId();
            OrderDTO createdOrder = orderService.createOrder(orderCreateDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'TEACHER')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            OrderDTO order = orderService.getOrderById(id, userId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生订单列表
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getStudentOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Page<OrderDTO> orders = orderService.getStudentOrders(userId, status, keyword, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders.getContent());
            response.put("currentPage", orders.getNumber());
            response.put("totalItems", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取教师订单列表
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getTeacherOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Page<OrderDTO> orders = orderService.getTeacherOrders(userId, status, keyword, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders.getContent());
            response.put("currentPage", orders.getNumber());
            response.put("totalItems", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            OrderDTO updatedOrder = orderService.payOrder(id, userId);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'TEACHER')")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            Long userId = getCurrentUserId();
            OrderDTO updatedOrder = orderService.cancelOrder(id, userId, reason);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 完成订单
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            OrderDTO updatedOrder = orderService.completeOrder(id, userId);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            throw new RuntimeException("用户未授权");
        }
        return Long.valueOf(authentication.getDetails().toString());
    }
} 