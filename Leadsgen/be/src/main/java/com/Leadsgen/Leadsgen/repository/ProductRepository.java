package com.Leadsgen.Leadsgen.repository;

import com.Leadsgen.Leadsgen.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
