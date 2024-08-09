package com.be.rebook.product.repository;

import com.be.rebook.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    List<Product> findProductsBySellerId(Long sellerId);
}
