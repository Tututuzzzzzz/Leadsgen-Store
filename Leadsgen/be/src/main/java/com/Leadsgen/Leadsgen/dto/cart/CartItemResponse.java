package com.Leadsgen.Leadsgen.dto.cart;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartItemResponse {
    Long productId;
    String name;
    String thumbnail;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal lineTotal;
}
