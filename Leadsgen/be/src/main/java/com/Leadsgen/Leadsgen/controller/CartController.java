package com.Leadsgen.Leadsgen.controller;

import com.Leadsgen.Leadsgen.dto.cart.AddToCartRequest;
import com.Leadsgen.Leadsgen.dto.cart.CartResponse;
import com.Leadsgen.Leadsgen.dto.cart.UpdateCartItemRequest;
import com.Leadsgen.Leadsgen.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(@Valid @RequestBody AddToCartRequest request) {
        return cartService.addToCart(request);
    }

    @GetMapping("/{cartId}")
    public CartResponse getCartById(@PathVariable Long cartId) {
        return cartService.getCartById(cartId);
    }

    @GetMapping("/user/{userId}")
    public CartResponse getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public CartResponse removeItem(@PathVariable Long cartId, @PathVariable Long productId) {
        return cartService.removeItem(cartId, productId);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public CartResponse updateItemQuantity(
        @PathVariable Long cartId,
        @PathVariable Long productId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItemQuantity(cartId, productId, request);
    }
}
