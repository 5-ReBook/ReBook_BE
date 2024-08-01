package com.be.rebook.domain.productImage.repository;

import com.be.rebook.domain.productImage.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

}
