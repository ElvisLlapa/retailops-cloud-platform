package com.elvisllapa.retailops.product.integration;

import com.elvisllapa.retailops.product.domain.Product;
import com.elvisllapa.retailops.product.domain.ProductStatus;
import com.elvisllapa.retailops.product.dto.ProductResponse;
import com.elvisllapa.retailops.product.repository.ProductRepository;
import com.elvisllapa.retailops.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:17:///product_test_db",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Transactional
class ProductIntegrationTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        productRepository.flush();
    }

    @Test
    void shouldRunFlywayMigration() {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'products'
                """,
                Integer.class
        );

        assertEquals(1, tableCount);
    }

    @Test
    void shouldPersistAndRetrieveProduct() {
        Product product = createProduct(
                "LAPTOP-001",
                "RetailOps Laptop",
                "Computers"
        );

        Product savedProduct =
                productRepository.saveAndFlush(product);

        assertNotNull(savedProduct.getId());

        Product foundProduct = productRepository
                .findBySkuIgnoreCase("LAPTOP-001")
                .orElseThrow();

        assertEquals("RetailOps Laptop", foundProduct.getName());
        assertEquals(ProductStatus.ACTIVE, foundProduct.getStatus());
    }

    @Test
    void shouldEnforceUniqueSkuConstraint() {
        Product firstProduct = createProduct(
                "MOUSE-001",
                "Wireless Mouse",
                "Accessories"
        );

        Product duplicateProduct = createProduct(
                "MOUSE-001",
                "Another Wireless Mouse",
                "Accessories"
        );

        productRepository.saveAndFlush(firstProduct);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(duplicateProduct)
        );
    }

    @Test
    void shouldFilterAndPaginateProducts() {
        productRepository.saveAndFlush(createProduct(
                "AUDIO-001",
                "Bluetooth Speaker",
                "Electronics"
        ));

        productRepository.saveAndFlush(createProduct(
                "AUDIO-002",
                "Wireless Headphones",
                "Electronics"
        ));

        productRepository.saveAndFlush(createProduct(
                "HOME-001",
                "Coffee Maker",
                "Home"
        ));

        PageRequest pageRequest = PageRequest.of(
                0,
                1,
                Sort.by("name").ascending()
        );

        Page<ProductResponse> result =
                productService.getProducts(
                        "Electronics",
                        null,
                        ProductStatus.ACTIVE,
                        null,
                        pageRequest
                );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertTrue(
                result.getContent().getFirst()
                        .category()
                        .equalsIgnoreCase("Electronics")
        );
    }

    private Product createProduct(
            String sku,
            String name,
            String category
    ) {
        return new Product(
                sku,
                name,
                "Integration test product",
                "RetailOps",
                category,
                ProductStatus.ACTIVE
        );
    }
}