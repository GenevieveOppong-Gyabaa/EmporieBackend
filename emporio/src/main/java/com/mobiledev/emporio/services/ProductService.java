package com.mobiledev.emporio.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mobiledev.emporio.dto.DealDto;
import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.Role;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewService reviewService;

    public ProductService(ProductRepository productRepository, ReviewService reviewService) {
        this.productRepository = productRepository;
        this.reviewService = reviewService;
    }

    public Product createProduct(Product product, User user) {
        if (user == null || user.getRole() != Role.SELLER) {
            throw new IllegalArgumentException("Only sellers can create products.");
        }
        product.setSeller(user);
        return productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(Long id, Product updatedProduct, User user) {
        Product existingProduct = getProductById(id);
        if (existingProduct != null && updatedProduct != null) {
            if (user == null || user.getRole() != Role.SELLER || !existingProduct.getSeller().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Only the seller can update their own products.");
            }
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setStock(updatedProduct.getStock());
            existingProduct.setDiscountPrice(updatedProduct.getDiscountPrice());
            existingProduct.setDiscountPercent(updatedProduct.getDiscountPercent());
            existingProduct.setOnDeal(updatedProduct.getOnDeal());
            existingProduct.setCategory(updatedProduct.getCategory());
            existingProduct.setTags(updatedProduct.getTags());
            existingProduct.setImageUrls(updatedProduct.getImageUrls());
            return productRepository.save(existingProduct);
        }
        return null;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsBySeller(User seller) {
        return productRepository.findBySeller(seller);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<DealDto> getDealDtos() {
        List<Product> deals = productRepository.findByOnDealTrue();
        List<DealDto> dealDtos = new java.util.ArrayList<>();
        for (Product product : deals) {
            dealDtos.add(mapToDealDto(product));
        }
        return dealDtos;
    }

    public DealDto mapToDealDto(Product product) {
        DealDto dto = new DealDto();
        dto.setId(product.getId());
        dto.setTitle(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getPrice() != null ? String.format("₵%.2f", product.getPrice()) : null);
        dto.setSalePrice(product.getDiscountPrice() != null ? String.format("₵%.2f", product.getDiscountPrice()) : null);
        dto.setDiscount(product.getDiscountPercent() != null ? String.format("%.0f%%", product.getDiscountPercent()) : null);
        dto.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
        dto.setInStock(product.getStock() != null && product.getStock() > 0);
        dto.setImage(product.getImageUrls() != null && !product.getImageUrls().isEmpty() ? product.getImageUrls().get(0) : null);
        // Optional fields: brand, colors, sizes (not present in Product, set null or default)
        dto.setBrand(null);
        dto.setColors(null);
        dto.setSizes(null);
        // Aggregate rating and reviews
        double avgRating = reviewService.getAverageRatingForProduct(product.getId());
        int reviewCount = reviewService.getReviewCountForProduct(product.getId());
        dto.setRating(avgRating);
        dto.setReviews(reviewCount);
        return dto;
    }
}

