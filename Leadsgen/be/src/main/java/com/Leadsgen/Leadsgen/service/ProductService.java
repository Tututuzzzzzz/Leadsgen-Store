package com.Leadsgen.Leadsgen.service;

import com.Leadsgen.Leadsgen.dto.product.ProductResponse;
import com.Leadsgen.Leadsgen.entity.Product;
import com.Leadsgen.Leadsgen.exception.ResourceNotFoundException;
import com.Leadsgen.Leadsgen.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::toProductResponse)
            .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .image(product.getImage())
            .stock(product.getStock())
            .build();
    }
}
