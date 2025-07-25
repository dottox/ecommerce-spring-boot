package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.mappper.ProductUpdater;
import com.ecommerce.project.models.Cart;
import com.ecommerce.project.models.Category;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductUpdater productUpdater;

    @Autowired
    private FileService fileService;

    @Override
    public ProductDTO addProduct(
            ProductDTO productDTO,
            Long categoryId)
    {
        // Check if the category exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Check if the product already exists in the category
        for (Product product : category.getProducts()) {
            if (product.getName().equalsIgnoreCase(productDTO.getName())) {
                throw new ApiException("Product with the same name already exists in this category", HttpStatus.BAD_REQUEST);
            }
        }

        // Map ProductDTO to Product entity
        Product product = modelMapper.map(productDTO, Product.class);

        // Set default image value, category and calculate special price
        product.setImage("default.png");
        product.setCategory(category);
        Double specialPrice = product.getPrice() - (product.getPrice() * product.getDiscount() / 100);
        product.setSpecialPrice(specialPrice);

        // Add the product to the category's product list and save the product to the repository
        category.getProducts().add(product);
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection
    ) {
        // Check if there are any products available
        if (productRepository.count() == 0)
            throw new ApiException("No products availables", HttpStatus.NOT_FOUND);

        // Create pageable object and find all products with pagination
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,
                sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Page<Product> productPage = productRepository.findAll(pageDetails);

        // If the page comes empty, throw an exception
        if (productPage.isEmpty())
            throw new ApiException("No products found for the given page and size", HttpStatus.NOT_FOUND);

        // Map the products to ProductDTOs
        List<ProductDTO> productsDTOs = productPage.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // Return the response with pagination details
        return new ProductResponse(
                productsDTOs,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    public ProductResponse getProductsByCategory(
            Long categoryId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection
    ) {
        // Check if the category exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Create a pageable object and find products by category with pagination
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,
                sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Page<Product> productPage = productRepository.findByCategory(category, pageDetails);

        // If the page comes empty, throw an exception
        if (productPage.isEmpty())
            throw new ApiException("No products found for the given category", HttpStatus.NOT_FOUND);

        // Map the products to ProductDTOs
        List<ProductDTO> productsDTOs = productPage.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // Return the response with pagination details
        return new ProductResponse(
                productsDTOs,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    public ProductResponse getProductsByKeyword(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection
    ) {
        // Create a pageable object and find products by keyword with pagination
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,
                sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Page<Product> productPage = productRepository.findByNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        // If the page comes empty, throw an exception
        if (productPage.isEmpty())
            throw new ApiException("No products found for the given keyword", HttpStatus.NOT_FOUND);

        // Map the products to ProductDTOs
        List<ProductDTO> productsDTOs = productPage.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // Return the response with pagination details
        return new ProductResponse(
                productsDTOs,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        // Check if the product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Map the product to ProductDTO and return it
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(
            ProductDTO updateProduct,
            Long productId
    ) {
        // Check if the product exists
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Update the existing product with the new values from updateProduct
        productUpdater.updateProduct(existingProduct, updateProduct);

        // Calculate the special price based on the updated price and discount
        Double specialPrice = existingProduct.getPrice() - (existingProduct.getPrice() * existingProduct.getDiscount() / 100);
        existingProduct.setSpecialPrice(specialPrice);

        // Save the updated product to the repository
        Product updatedProduct = productRepository.save(existingProduct);

        // Get all carts that contain this product
        List<Cart> carts = cartRepository.findCartByProductId(productId);
        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                    .toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();

        // Update the cart (like total price) for each cart that contains this product
        cartDTOs.forEach(cartDTO -> cartService.updateProductInCarts(cartDTO.getCartId(), productId));

        // Return the updated product as ProductDTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        // Check if the product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Rmove the product from its category's product list and delete it
        product.getCategory().getProducts().remove(product);
        productRepository.delete(product);

        // Get all carts that contain this product
        List<Cart> carts = cartRepository.findCartByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));


        // Return the deleted product as ProductDTO
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(
            MultipartFile image,
            Long productId
    ) {
        // Check if the product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if the image file is empty
        if (image.isEmpty()) {
            throw new ApiException("Image file is empty", HttpStatus.BAD_REQUEST);
        }

        // Upload the image and delete the old one
        String fileName = fileService.uploadImage(image);
        fileService.deleteImage(product.getImage());

        // Set the new image name in the product and update the product in the repository
        product.setImage(fileName);
        Product updatedProduct = productRepository.save(product);

        // Return the updated product as ProductDTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }
}
