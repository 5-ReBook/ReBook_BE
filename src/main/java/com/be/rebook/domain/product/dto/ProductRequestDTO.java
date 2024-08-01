package com.be.rebook.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ProductRequestDTO {

    @Getter
    @Builder
    public static class ProductSaveRequestDTO {
        private String title;

        private String content;

        private int price;

        private String isbn;

        private String bookTitle;

        private String author;

        private String publisher;

        private String publishDate;

        private String university;

        private String major;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStatusRequestDTO {
        private String status;
    }
}
