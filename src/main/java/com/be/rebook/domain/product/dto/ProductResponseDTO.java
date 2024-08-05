package com.be.rebook.domain.product.dto;

import com.be.rebook.domain.product.entity.Product;
import com.be.rebook.domain.product.type.ProductStatus;
import com.be.rebook.domain.productImage.domain.ProductImage;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class ProductResponseDTO {

    /**
     * 전체 목록 반환 DTO
     */
    @Getter
    public static class ProductListResponseDTO {

        private Long productId;

        private String storeFileName;

        private String title;

        private int price;

        private String university;

        private String major;

        private ProductStatus status;


        public ProductListResponseDTO(Product product) {
            this.productId = product.getId();
            this.storeFileName = product.getProductImages().get(0).getStoreFileName();
            this.title = product.getTitle();
            this.price = product.getPrice();
            this.university = product.getUniversity();
            this.major = product.getMajor();
            this.status = product.getStatus();
        }
    }

    @Getter
    public static class ProductDetailResponseDTO {

        private Long productId;

        private List<String> storeFileNameList;

        private String nickname;

        private Long university;

        private String majors;

        private String title;

        private String content;

        private int price;

        private String isbn;

        private String bookTitle;

        private String author;

        private String publisher;

        private String publishDate;

        // 책 사용 대학
        private String bookUniversity;

        // 책 사용 전공학과
        private String bookMajor;

        private ProductStatus status;

        public ProductDetailResponseDTO(Product product) {
            this.productId = product.getId();
            this.storeFileNameList = product.getProductImages().stream()
                    .map(ProductImage::getStoreFileName)
                    .collect(Collectors.toList());
            this.nickname = product.getSeller().getNickname();
            this.university = product.getSeller().getUniversity();
            this.majors = product.getSeller().getMajors();
            this.title = product.getTitle();
            this.content = product.getContent();
            this.price = product.getPrice();
            this.isbn = product.getIsbn();
            this.bookTitle = product.getBookTitle();
            this.author = product.getAuthor();
            this.publisher = product.getPublisher();
            this.publishDate = product.getPublishDate();
            this.bookUniversity = product.getUniversity();
            this.bookMajor = product.getMajor();
            this.status = product.getStatus();
        }
    }

}
