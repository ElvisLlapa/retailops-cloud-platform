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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductServiceImpl productService;

    private UUID productId;
    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(
                productRepository,
                productMapper
        );

        productId = UUID.randomUUID();

        product = new Product(
                "HEADPHONES-005",
                "Wireless Headphones Pro",
                "Noise-canceling wireless headphones",
                "RetailOps",
                "Electronics",
                ProductStatus.ACTIVE
        );

        productResponse = new ProductResponse(
                productId,
                "HEADPHONES-005",
                "Wireless Headphones Pro",
                "Noise-canceling wireless headphones",
                "RetailOps",
                "Electronics",
                ProductStatus.ACTIVE,
                Instant.now(),
                Instant.now(),
                0L
        );
    }

    @Test
    void shouldCreateValidProduct() {
        CreateProductRequest request = new CreateProductRequest(
                " headphones-005 ",
                " Wireless Headphones Pro ",
                " Noise-canceling wireless headphones ",
                " RetailOps ",
                " Electronics "
        );

        when(productRepository.existsBySkuIgnoreCase("HEADPHONES-005"))
                .thenReturn(false);
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(any(Product.class)))
                .thenReturn(productResponse);

        ProductResponse result = productService.createProduct(request);

        assertEquals(productResponse, result);

        ArgumentCaptor<Product> captor =
                ArgumentCaptor.forClass(Product.class);

        verify(productRepository).saveAndFlush(captor.capture());

        Product savedProduct = captor.getValue();

        assertEquals("HEADPHONES-005", savedProduct.getSku());
        assertEquals("Wireless Headphones Pro", savedProduct.getName());
        assertEquals("RetailOps", savedProduct.getBrand());
        assertEquals("Electronics", savedProduct.getCategory());
        assertEquals(ProductStatus.ACTIVE, savedProduct.getStatus());
    }

    @Test
    void shouldRejectDuplicateSku() {
        CreateProductRequest request = new CreateProductRequest(
                "HEADPHONES-005",
                "Wireless Headphones Pro",
                "Noise-canceling wireless headphones",
                "RetailOps",
                "Electronics"
        );

        when(productRepository.existsBySkuIgnoreCase("HEADPHONES-005"))
                .thenReturn(true);

        assertThrows(
                DuplicateSkuException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void shouldReturnExistingProductById() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        ProductResponse result =
                productService.getProductById(productId);

        assertEquals(productResponse, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductIsMissing() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void shouldUpdateExistingProduct() {
        UpdateProductRequest request = new UpdateProductRequest(
                "HEADPHONES-005",
                "RetailOps Wireless Headphones Pro",
                "Premium noise-canceling wireless headphones",
                "RetailOps",
                "Audio"
        );

        ProductResponse updatedResponse = new ProductResponse(
                productId,
                "HEADPHONES-005",
                "RetailOps Wireless Headphones Pro",
                "Premium noise-canceling wireless headphones",
                "RetailOps",
                "Audio",
                ProductStatus.ACTIVE,
                Instant.now(),
                Instant.now(),
                1L
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.findBySkuIgnoreCase("HEADPHONES-005"))
                .thenReturn(Optional.empty());
        when(productRepository.saveAndFlush(product))
                .thenReturn(product);
        when(productMapper.toResponse(product))
                .thenReturn(updatedResponse);

        ProductResponse result =
                productService.updateProduct(productId, request);

        assertEquals(updatedResponse, result);
        assertEquals(
                "RetailOps Wireless Headphones Pro",
                product.getName()
        );
        assertEquals("Audio", product.getCategory());

        verify(productRepository).saveAndFlush(product);
    }

    @Test
    void shouldDiscontinueExistingProduct() {
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.saveAndFlush(product))
                .thenReturn(product);

        productService.discontinueProduct(productId);

        assertEquals(
                ProductStatus.DISCONTINUED,
                product.getStatus()
        );

        verify(productRepository).saveAndFlush(product);
    }
}