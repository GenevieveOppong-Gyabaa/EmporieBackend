package com.mobiledev.emporio.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobiledev.emporio.dto.CartDto;
import com.mobiledev.emporio.dto.CartItemDto;
import com.mobiledev.emporio.exceptions.CartItemNotFoundException;
import com.mobiledev.emporio.exceptions.InvalidCartOperationException;
import com.mobiledev.emporio.exceptions.ProductNotFoundException;
import com.mobiledev.emporio.model.Cart;
import com.mobiledev.emporio.model.CartItem;
import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.CartItemRepository;
import com.mobiledev.emporio.repositories.CartRepository;
import com.mobiledev.emporio.repositories.ProductRepository;

@Service
@Transactional
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private void validateUser(User user) {
        if (user == null) throw new InvalidCartOperationException("User must not be null");
    }

    public Cart getCart(User user) {
        validateUser(user);
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            logger.info("Creating new cart for user: {}", user.getId());
            return cartRepository.save(newCart);
        });
    }

    public void addToCart(User user, Long productId, int quantity) {
        validateUser(user);
        if (quantity <= 0) throw new InvalidCartOperationException("Quantity must be greater than zero");
        Cart cart = getCart(user);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (quantity > product.getStock()) {
            throw new InvalidCartOperationException("Requested quantity exceeds available stock");
        }
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.addItem(item);
            logger.info("Added new item (productId: {}) to cart for user: {}", productId, user.getId());
            cartItemRepository.save(item);
        } else {
            int newQty = item.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                throw new InvalidCartOperationException("Requested quantity exceeds available stock");
            }
            item.setQuantity(newQty);
            logger.info("Updated quantity for productId {} in cart for user {}: {}", productId, user.getId(), newQty);
        }
        cartRepository.save(cart);
    }

    public void updateQuantity(User user, Long productId, int quantity) {
        validateUser(user);
        Cart cart = getCart(user);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
            .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));
        if (quantity < 0) throw new InvalidCartOperationException("Quantity must not be negative");
        if (quantity > product.getStock()) {
            throw new InvalidCartOperationException("Requested quantity exceeds available stock");
        }
        if (quantity == 0) {
            cart.removeItem(item);
            logger.info("Removed productId {} from cart for user {} due to zero quantity", productId, user.getId());
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            logger.info("Set quantity for productId {} in cart for user {}: {}", productId, user.getId(), quantity);
        }
        cartRepository.save(cart);
    }

    public void removeItem(User user, Long productId) {
        validateUser(user);
        Cart cart = getCart(user);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
            .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));
        cart.removeItem(item);
        logger.info("Removed productId {} from cart for user {}", productId, user.getId());
        cartItemRepository.delete(item);
        cartRepository.save(cart);
    }

    public void clearCart(User user) {
        validateUser(user);
        Cart cart = getCart(user);
        cart.getItems().clear();
        logger.info("Cleared cart for user {}", user.getId());
        cartItemRepository.deleteAll(cartItemRepository.findAll().stream().filter(i -> i.getCart().equals(cart)).toList());
        cartRepository.save(cart);
    }

    public double calculateCartTotal(User user) {
        validateUser(user);
        Cart cart = getCart(user);
        return cart.getItems().stream()
                   .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                   .sum();
    }

    public CartDto getCartDto(User user) {
        validateUser(user);
        Cart cart = getCart(user);
        CartDto cartDto = new CartDto();
        List<CartItemDto> itemDtos = new ArrayList<>();
        double totalPrice = 0.0;
        for (CartItem item : cart.getItems()) {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setPrice(item.getProduct().getPrice());
            itemDto.setQuantity(item.getQuantity());
            double itemTotal = item.getProduct().getPrice() * item.getQuantity();
            itemDto.setTotalPrice(itemTotal);
            totalPrice += itemTotal;
            itemDtos.add(itemDto);
        }
        cartDto.setItems(itemDtos);
        cartDto.setTotalPrice(totalPrice);
        return cartDto;
    }

    // Remove all cart items for a deleted product
    public void removeAllItemsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        List<CartItem> items = cartItemRepository.findAll();
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                Cart cart = item.getCart();
                cart.removeItem(item);
                logger.info("Removed productId {} from cartId {} due to product deletion", productId, cart.getId());
                cartItemRepository.delete(item);
                cartRepository.save(cart);
            }
        }
    }
}