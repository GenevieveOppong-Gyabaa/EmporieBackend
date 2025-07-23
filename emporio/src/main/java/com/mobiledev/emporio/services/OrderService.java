package com.mobiledev.emporio.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobiledev.emporio.dto.OrderItemRequest;
import com.mobiledev.emporio.dto.OrderRequestDto;
import com.mobiledev.emporio.dto.OrderStatusUpdateDTO;
import com.mobiledev.emporio.model.Order;
import com.mobiledev.emporio.model.OrderItem;
import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.OrderRepository;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public Order placeOrder(OrderRequestDto orderDTO) {
        Order order = new Order();
        order.setBuyer(userRepository.findByUsername(orderDTO.getBuyerUsername()));
        order.setStatus("PLACED");
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod("CASH");
        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;
        for (OrderItemRequest itemReq : orderDTO.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId()).orElse(null);
            if (product == null) continue;
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            double price = (product.getDiscountPrice() != null && product.getOnDeal() != null && product.getOnDeal()) ? product.getDiscountPrice() : product.getPrice();
            item.setPrice(price);
            item.setQuantity(itemReq.getQuantity());
            item.setSubtotal(price * itemReq.getQuantity());
            total += item.getSubtotal();
            items.add(item);
        }
        order.setItems(items);
        order.setTotal(total);
        // Optionally set seller if all items are from one seller
        order.setSeller(null);
        Order savedOrder = orderRepository.save(order);
        // Notify seller(s) and buyer
        if (!items.isEmpty()) {
            // Notify all unique sellers for the products in the order
            java.util.Set<User> sellers = new java.util.HashSet<>();
            for (OrderItem item : items) {
                if (item.getProduct().getSeller() != null) {
                    sellers.add(item.getProduct().getSeller());
                }
            }
            for (User seller : sellers) {
                notificationService.createNotification(seller, "Product Sold!", "One of your products has been purchased.");
                notificationService.sendProductSoldEmail(seller.getUsername(), "A product you listed has been purchased. Order ID: " + savedOrder.getId());
            }
        }
        if (order.getBuyer() != null) {
            notificationService.createNotification(order.getBuyer(), "Order Placed", "Your order has been placed successfully. Order ID: " + savedOrder.getId());
        }
        return savedOrder;
    }

    public List<Order> getOrdersByBuyer(String username) {
        User buyer = userRepository.findByUsername(username);
        return orderRepository.findByBuyer(buyer);
    }

    public List<Order> getOrdersBySeller(String username) {
        User seller = userRepository.findByUsername(username);
        return orderRepository.findBySeller(seller);
    }

    public Order updateOrderStatus(Long orderId, OrderStatusUpdateDTO statusUpdateDTO) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(statusUpdateDTO.getStatus());
            return orderRepository.save(order);
        }
        return null;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }
    }
}
