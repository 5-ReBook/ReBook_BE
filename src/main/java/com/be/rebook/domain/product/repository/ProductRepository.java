package com.be.rebook.domain.product.repository;

import com.be.rebook.domain.members.entity.Members;
import com.be.rebook.domain.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    List<Product> findProductsBySeller(Members member);
}
