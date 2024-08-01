package com.be.rebook.domain.product.repository;

import com.be.rebook.domain.product.domain.Product;

import java.util.List;

public interface ProductRepositoryCustom {

    List<Product> findProductsByFilter(String university, String major, String title, Integer minPrice, Integer maxPrice, String order);
}
