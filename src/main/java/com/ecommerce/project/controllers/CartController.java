package com.ecommerce.project.controllers;

import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.services.CartService;
import com.ecommerce.project.util.AuthUtil;
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

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long productId,
            @PathVariable Integer quantity
    ) {
        // Logic to add product to cart
        CartDTO savedCartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(savedCartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartLoggedUser() {
        CartDTO cartDTO = cartService.getCartLoggedUser();
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/carts/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateProductQuantity(
            @PathVariable Long productId,
            @PathVariable String operation
    ) {
        CartDTO updatedCartDTO = cartService.updateProductQuantity(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);
        return new ResponseEntity<>(updatedCartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/products/{productId}")
    public ResponseEntity<CartDTO> deleteProductFromCart(
            @PathVariable Long productId
    ) {
        CartDTO deletedCartDTO = cartService.deleteProductFromActualCart(productId);
        return new ResponseEntity<>(deletedCartDTO, HttpStatus.OK);
    }
}
