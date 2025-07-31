package com.ecommerce.project.controllers;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    ProductService productService;

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Add product", description = "API to add a new product to a category")
    @PostMapping("/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> addProduct(
            @RequestBody ProductDTO product,
            @PathVariable Long categoryId
    ) {
        ProductDTO productDTO = productService.addProduct(product, categoryId);
        return new ResponseEntity<>(productDTO, HttpStatus.CREATED);
    }

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Get all products", description = "API to retrieve all products with optional keyword search")
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCT) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION) String sortDirection
    ) {
        if (keyword == null || keyword.isEmpty()) {
            ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortDirection);
            return new ResponseEntity<>(productResponse, HttpStatus.OK);
        } else {
            ProductResponse productResponse = productService.getProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortDirection);
            return new ResponseEntity<>(productResponse, HttpStatus.OK);
        }
    }

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Get products by category", description = "API to retrieve products by category ID")
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCT) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION) String sortDirection
    ) {
        ProductResponse productResponse = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Get product by ID", description = "API to retrieve a product by its ID")
    @GetMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO productDTO = productService.getProductById(productId);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Update product", description = "API to update a product by its ID")
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @RequestBody ProductDTO product,
            @PathVariable Long productId
    ) {
        ProductDTO updatedProductDTO = productService.updateProduct(product, productId);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Delete product", description = "API to delete a product by its ID")
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO deletedProductDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProductDTO, HttpStatus.OK);
    }

    @Tag(name = "Product Management", description = "APIs for managing products")
    @Operation(summary = "Update product image", description = "API to update the image of a product")
    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(
            @RequestParam("image") MultipartFile image,
            @PathVariable Long productId
    ) {
        ProductDTO updatedProductDTO = productService.updateProductImage(image, productId);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }
}