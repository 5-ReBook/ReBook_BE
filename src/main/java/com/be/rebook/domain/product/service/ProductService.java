package com.be.rebook.domain.product.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.be.rebook.domain.members.entity.Members;
import com.be.rebook.domain.members.repository.MembersRepository;
import com.be.rebook.domain.product.domain.Product;
import com.be.rebook.domain.product.dto.ProductRequestDTO;
import com.be.rebook.domain.product.dto.ProductResponseDTO;
import com.be.rebook.domain.product.repository.ProductRepository;
import com.be.rebook.domain.productImage.domain.ProductImage;
import com.be.rebook.domain.productImage.repository.ProductImageRepository;
import com.be.rebook.global.exception.BaseException;
import com.be.rebook.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.be.rebook.domain.product.dto.ProductResponseDTO.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductImageRepository productImageRepository;
    private final AmazonS3Client amazonS3Client;
    private final ProductRepository productRepository;
    private final MembersRepository membersRepository;

    /**
     *  S3
     */
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    // TODO -> 이미지파일 업로드 후 product 엔티티 저장
    @Transactional
    public Long createProduct(Long memberId, ProductRequestDTO.ProductSaveRequestDTO productSaveRequestDTO, List<MultipartFile> imageFiles) throws IOException {

        Members member = membersRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.NO_USER_INFO));

        // Product 먼저 저장
        Product product = Product.of(member, productSaveRequestDTO);
        Long savedProductId = productRepository.save(product).getId();

        // 각 이미지 파일을 S3에 업로드 한 후 prductImage로 저장(Product에도 추가)
        for (MultipartFile imageFile : imageFiles) {
            // 원본 파일명
            String originalFileName = imageFile.getOriginalFilename();
            // 저장 파일명
            String storeFileName = createStoreFileName(originalFileName);

            // S3에 저장
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(imageFile.getContentType());
            metadata.setContentLength(imageFile.getSize());
            amazonS3Client.putObject(bucket, storeFileName, imageFile.getInputStream(), metadata);

            ProductImage productImage = ProductImage.builder()
                    .uploadFileName(originalFileName)
                    .storeFileName(storeFileName)
                    .product(product)
                    .build();

            productImageRepository.save(productImage);
        }

        return savedProductId;
    }

    /**
     * 상품 목록 조회
     */
    public List<ProductListResponseDTO> findAllProductByFilter(String university, String major, String title, Integer minPrice, Integer maxPrice, String order) {
        List<Product> products = productRepository.findProductsByFilter(university, major, title, minPrice, maxPrice, order);

        return products.stream()
                .map(ProductListResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 내가 쓴 글 조회
     */
    public List<ProductListResponseDTO> findAllProductByMember(Long memberId) {
        Members member = membersRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.NO_USER_INFO));

        List<Product> products = productRepository.findProductsBySeller(member);

        return products.stream()
                .map(ProductListResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 상품 단건 조회
     */
    public ProductDetailResponseDTO findProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_EXIST_PRODUCT));

        return new ProductDetailResponseDTO(product);
    }

    /**
     * 상품 상태 변경
     */
    @Transactional
    public Long changeStatus(Long productId, String status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_EXIST_PRODUCT));

        product.changeStatus(status);
        return productId;
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public String deleteById(Long productId) {
        productRepository.deleteById(productId);
        return "상품 삭제 완료";
    }


    /**
     * 파일명이 겹치는 것을 방지하기위해 중복되지않는 UUID를 생성해서 반환(ext는 확장자)
     */
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * 파일 확장자를 추출하기 위해 만든 메서드
     */
    private String extractExt(String originalFilename) {
        int post = originalFilename.lastIndexOf(".");
        return originalFilename.substring(post + 1);
    }
}