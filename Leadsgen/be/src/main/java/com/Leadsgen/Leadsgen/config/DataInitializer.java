package com.Leadsgen.Leadsgen.config;

import com.Leadsgen.Leadsgen.entity.Product;
import com.Leadsgen.Leadsgen.repository.ProductRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DUMMY_JSON_URL = "https://dummyjson.com/products?limit=12";

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Bean
    CommandLineRunner seedProducts() {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            List<Product> products = fetchProductsFromDummyJson();
            if (products.isEmpty()) {
                log.warn("No products were seeded because DummyJSON is unavailable or returned no data.");
                return;
            }

            productRepository.saveAll(products);
        };
    }

    private List<Product> fetchProductsFromDummyJson() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(DUMMY_JSON_URL))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("DummyJSON call failed with status {}", response.statusCode());
                return List.of();
            }

            DummyProductsResponse payload = objectMapper.readValue(response.body(), DummyProductsResponse.class);
            if (payload.products() == null || payload.products().isEmpty()) {
                return List.of();
            }

            List<Product> mapped = new ArrayList<>();
            for (DummyProduct item : payload.products()) {
                mapped.add(Product.builder()
                    .name(item.title())
                    .description(item.description())
                    .image(item.thumbnail())
                    .price(BigDecimal.valueOf(item.price()))
                    .stock(item.stock())
                    .build());
            }
            return mapped;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("DummyJSON fetch interrupted: {}", ex.getMessage());
            return List.of();
        } catch (IOException ex) {
            log.warn("Cannot fetch products from DummyJSON: {}", ex.getMessage());
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DummyProductsResponse(List<DummyProduct> products) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DummyProduct(String title, String description, double price, String thumbnail, int stock) {}
}
