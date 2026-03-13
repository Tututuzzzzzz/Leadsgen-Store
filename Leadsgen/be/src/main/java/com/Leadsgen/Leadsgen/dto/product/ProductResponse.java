package com.Leadsgen.Leadsgen.dto.product;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponse {
    Long id;
    String name;
    String description;
    BigDecimal price;
    String image;
    Integer stock;
}
