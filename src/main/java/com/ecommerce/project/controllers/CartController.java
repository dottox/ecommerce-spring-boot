package com.ecommerce.project.controllers;

import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.services.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long productId,
            @PathVariable Integer quantity
    ) {
        // Logic to add product to cart
        CartDTO savedCartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(savedCartDTO, HttpStatus.CREATED);
    }
}
