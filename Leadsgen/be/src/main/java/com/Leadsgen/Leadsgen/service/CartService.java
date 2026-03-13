package com.Leadsgen.Leadsgen.service;

import com.Leadsgen.Leadsgen.dto.cart.AddToCartRequest;
import com.Leadsgen.Leadsgen.dto.cart.CartItemResponse;
import com.Leadsgen.Leadsgen.dto.cart.CartResponse;
import com.Leadsgen.Leadsgen.dto.cart.UpdateCartItemRequest;
import com.Leadsgen.Leadsgen.entity.Cart;
import com.Leadsgen.Leadsgen.entity.CartItem;
import com.Leadsgen.Leadsgen.entity.Product;
import com.Leadsgen.Leadsgen.exception.BusinessException;
import com.Leadsgen.Leadsgen.exception.ResourceNotFoundException;
import com.Leadsgen.Leadsgen.repository.CartItemRepository;
import com.Leadsgen.Leadsgen.repository.CartRepository;
import com.Leadsgen.Leadsgen.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartResponse addToCart(AddToCartRequest request) {
        Product product = getProductOrThrow(request.getProductId());
        validateStock(product, request.getQuantity());

        Cart cart = cartRepository.findByUserId(request.getUserId())
            .orElseGet(() -> cartRepository.save(Cart.builder().userId(request.getUserId()).build()));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseGet(() -> CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(0)
                .unitPrice(product.getPrice())
                .build());

        item.setQuantity(item.getQuantity() + request.getQuantity());
        item.setUnitPrice(product.getPrice());
        product.setStock(product.getStock() - request.getQuantity());

        productRepository.save(product);
        cartItemRepository.save(item);

        if (cart.getItems().stream().noneMatch(existing -> existing.getProduct().getId().equals(product.getId()))) {
            cart.getItems().add(item);
        }

        recalculateTotal(cart);
        return toCartResponse(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public CartResponse getCartById(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        return toCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for userId: " + userId));
        return toCartResponse(cart);
    }

    public CartResponse removeItem(Long cartId, Long productId) {
        Cart cart = getCartOrThrow(cartId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, productId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found for productId: " + productId));

        Product product = item.getProduct();
        product.setStock(product.getStock() + item.getQuantity());
        productRepository.save(product);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        recalculateTotal(cart);
        return toCartResponse(cartRepository.save(cart));
    }

    public CartResponse updateItemQuantity(Long cartId, Long productId, UpdateCartItemRequest request) {
        Cart cart = getCartOrThrow(cartId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, productId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found for productId: " + productId));

        int oldQuantity = item.getQuantity();
        int newQuantity = request.getQuantity();
        int delta = newQuantity - oldQuantity;

        Product product = item.getProduct();
        if (delta > 0) {
            validateStock(product, delta);
            product.setStock(product.getStock() - delta);
        } else if (delta < 0) {
            product.setStock(product.getStock() + Math.abs(delta));
        }

        item.setQuantity(newQuantity);

        productRepository.save(product);
        cartItemRepository.save(item);

        recalculateTotal(cart);
        return toCartResponse(cartRepository.save(cart));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Cart getCartOrThrow(Long cartId) {
        return cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
    }

    private void validateStock(Product product, int requiredQuantity) {
        if (product.getStock() < requiredQuantity) {
            throw new BusinessException(
                "Insufficient stock for product " + product.getId() + ". Available: " + product.getStock()
            );
        }
    }

    private void recalculateTotal(Cart cart) {
        BigDecimal total = cart.getItems()
            .stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal(total);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(item -> CartItemResponse.builder()
                .productId(item.getProduct().getId())
                .name(item.getProduct().getName())
                .thumbnail(item.getProduct().getImage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build())
            .toList();

        return CartResponse.builder()
            .cartId(cart.getId())
            .userId(cart.getUserId())
            .total(cart.getTotal())
            .itemCount(items.stream().mapToInt(CartItemResponse::getQuantity).sum())
            .items(items)
            .build();
    }
}
