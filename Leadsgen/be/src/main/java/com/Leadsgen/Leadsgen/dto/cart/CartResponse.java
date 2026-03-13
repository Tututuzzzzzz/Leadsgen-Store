package com.Leadsgen.Leadsgen.dto.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartResponse {
    Long cartId;
    Long userId;
    BigDecimal total;
    Integer itemCount;
    List<CartItemResponse> items;
}
