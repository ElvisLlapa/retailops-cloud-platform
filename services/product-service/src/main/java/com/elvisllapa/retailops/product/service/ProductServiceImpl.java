package com.elvisllapa.retailops.product.service;

import com.elvisllapa.retailops.product.domain.Product;
import com.elvisllapa.retailops.product.domain.ProductStatus;
import com.elvisllapa.retailops.product.dto.CreateProductRequest;
import com.elvisllapa.retailops.product.dto.ProductResponse;
import com.elvisllapa.retailops.product.dto.UpdateProductRequest;
import com.elvisllapa.retailops.product.exception.DuplicateSkuException;
import com.elvisllapa.retailops.product.exception.ProductNotFoundException;
import com.elvisllapa.retailops.product.mapper.ProductMapper;
import com.elvisllapa.retailops.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        String normalizedSku = request.sku().trim().toUpperCase(Locale.ROOT);

        if (productRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new DuplicateSkuException(normalizedSku);
        }

        Product product = new Product(
                normalizedSku,
                request.name().trim(),
                normalizeOptionalText(request.description()),
                request.brand().trim(),
                request.category().trim(),
                ProductStatus.ACTIVE
        );

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String category,
            String brand,
            ProductStatus status,
            String search,
            Pageable pageable
    ) {
        Specification<Product> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (category != null && !category.isBlank()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    criteriaBuilder.lower(root.get("category")),
                                    category.trim().toLowerCase(Locale.ROOT)
                            )
            );
        }

        if (brand != null && !brand.isBlank()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    criteriaBuilder.lower(root.get("brand")),
                                    brand.trim().toLowerCase(Locale.ROOT)
                            )
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("status"), status)
            );
        }

        if (search != null && !search.isBlank()) {
            String searchPattern =
                    "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.or(
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(root.get("name")),
                                            searchPattern
                                    ),
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(root.get("sku")),
                                            searchPattern
                                    ),
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(root.get("description")),
                                            searchPattern
                                    )
                            )
            );
        }

        return productRepository
                .findAll(specification, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        return productMapper.toResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository
                .findBySkuIgnoreCase(sku.trim())
                .orElseThrow(() -> new ProductNotFoundException(sku));

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(
            UUID id,
            UpdateProductRequest request
    ) {
        Product product = findProductById(id);
        String normalizedSku = request.sku().trim().toUpperCase(Locale.ROOT);

        productRepository.findBySkuIgnoreCase(normalizedSku)
                .filter(existingProduct ->
                        !existingProduct.getId().equals(product.getId()))
                .ifPresent(existingProduct -> {
                    throw new DuplicateSkuException(normalizedSku);
                });

        product.setSku(normalizedSku);
        product.setName(request.name().trim());
        product.setDescription(normalizeOptionalText(request.description()));
        product.setBrand(request.brand().trim());
        product.setCategory(request.category().trim());

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public ProductResponse updateProductStatus(
            UUID id,
            ProductStatus status
    ) {
        Product product = findProductById(id);
        product.setStatus(status);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public void discontinueProduct(UUID id) {
        Product product = findProductById(id);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}