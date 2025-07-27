package com.mobiledev.emporio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.ApiError;
import com.mobiledev.emporio.dto.OrderRequestDto;
import com.mobiledev.emporio.dto.OrderResponseDto;
import com.mobiledev.emporio.dto.OrderStatusUpdateDTO;
import com.mobiledev.emporio.model.Order;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.OrderService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDto orderDTO){
        Order order = orderService.placeOrder(orderDTO);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<?> getCurrentUserOrders(HttpServletRequest request) {
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null) {
            return ResponseEntity.status(401).body(new ApiError("Authentication required"));
        }
        List<Order> orders = orderService.getOrdersByBuyer(authUsername);
        List<OrderResponseDto> orderDtos = orders.stream()
            .map(OrderResponseDto::new)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(orderDtos);
    }
    @GetMapping("/buyer/{username}")
    public ResponseEntity<?> getOrdersByBuyer(@PathVariable String username, HttpServletRequest request) {
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(username)) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own orders."));
        }
        return ResponseEntity.ok(orderService.getOrdersByBuyer(username));
    }
    @GetMapping("/seller/{username}")
    public ResponseEntity<?> getOrdersBySeller(@PathVariable String username, HttpServletRequest request) {
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(username)) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own sales."));
        }
        return ResponseEntity.ok(orderService.getOrdersBySeller(username));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody OrderStatusUpdateDTO statusUpdateDTO) {
        Order order = orderService.updateOrderStatus(orderId, statusUpdateDTO);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        Order order = orderService.getOrderById(orderId);
        if (order == null) return ResponseEntity.badRequest().body("Order not found");
        if (!order.getBuyer().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only cancel your own orders.");
        }
        if ("PAID".equals(order.getPaymentStatus())) {
            return ResponseEntity.status(403).body("Cannot cancel a paid/delivered order.");
        }
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled");
    }
}