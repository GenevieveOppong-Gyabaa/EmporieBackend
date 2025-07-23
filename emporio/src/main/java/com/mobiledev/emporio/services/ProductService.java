package com.mobiledev.emporio.services;

import java.util.List;
import java.util.Optional;

import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.model.Role;
import com.mobiledev.emporio.repositories.ProductRepository;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
}

