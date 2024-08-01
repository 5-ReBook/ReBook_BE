package com.be.rebook.domain.product.repository;

import com.be.rebook.domain.product.domain.Product;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.be.rebook.domain.product.domain.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<Product> findProductsByFilter(String university, String major, String title, Integer minPrice, Integer maxPrice, String order) {
        return jpaQueryFactory.selectFrom(product)
                .where(
                        universityContains(university),
                        majorContains(major),
                        titleContains(title),
                        priceBetween(minPrice, maxPrice)
                )
                .orderBy(getOrderSpecifier(order))
                .fetch();
    }

    private BooleanExpression universityContains(String university) {
        return university != null ? product.university.contains(university) : null;
    }

    private BooleanExpression majorContains(String major) {
        return major != null ? product.major.contains(major) : null;
    }

    private BooleanExpression titleContains(String title) {
        return title != null ? product.title.contains(title) : null;
    }

    private BooleanExpression priceBetween(Integer minPrice, Integer maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return product.price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return product.price.goe(minPrice);
        } else if (maxPrice != null) {
            return product.price.loe(maxPrice);
        } else {
            return null;
        }
    }

    private OrderSpecifier<?> getOrderSpecifier(String order) {
        if ("asc".equalsIgnoreCase(order)) {
            return product.price.asc();
        } else if ("desc".equalsIgnoreCase(order)) {
            return product.price.desc();
        } else {
            return product.createdAt.desc(); // 기본값: 최신순 정렬
        }
    }
}
