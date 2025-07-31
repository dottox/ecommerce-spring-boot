package com.ecommerce.project.controllers;

import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.services.CartService;
import com.ecommerce.project.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Tag(name = "Cart Management", description = "APIs for managing shopping carts")
    @Operation(summary = "Add product to cart", description = "API to add a product to the logged user's cart with specified quantity")
    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long productId,
            @PathVariable Integer quantity
    ) {
        // Logic to add product to cart
        CartDTO savedCartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(savedCartDTO, HttpStatus.CREATED);
    }

    @Tag(name = "Cart Management", description = "APIs for managing shopping carts")
    @Operation(summary = "Get all carts", description = "API to retrieve all carts")
    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);
    }

    @Tag(name = "Cart Management", description = "APIs for managing shopping carts")
    @Operation(summary = "Get cart", description = "API to retrieve the cart of the logged-in user")
    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartLoggedUser() {
        CartDTO cartDTO = cartService.getCartLoggedUser();
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @Tag(name = "Cart Management", description = "APIs for managing shopping carts")
    @Operation(summary = "Update product quantity in cart", description = "API to update the quantity of a product in the logged user's cart")
    @PutMapping("/carts/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateProductQuantity(
            @PathVariable Long productId,
            @PathVariable String operation
    ) {
        CartDTO updatedCartDTO = cartService.updateProductQuantity(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);
        return new ResponseEntity<>(updatedCartDTO, HttpStatus.OK);
    }

    @Tag(name = "Cart Management", description = "APIs for managing shopping carts")
    @Operation(summary = "Delete product from cart", description = "API to delete a product from the logged user's cart")
    @DeleteMapping("/carts/products/{productId}")
    public ResponseEntity<CartDTO> deleteProductFromCart(
            @PathVariable Long productId
    ) {
        CartDTO deletedCartDTO = cartService.deleteProductFromActualCart(productId);
        return new ResponseEntity<>(deletedCartDTO, HttpStatus.OK);
    }
}
