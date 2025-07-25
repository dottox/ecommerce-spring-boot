package com.ecommerce.project.services;

import com.ecommerce.project.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
    List<CartDTO> getAllCarts();
    CartDTO getCartLoggedUser();

    @Transactional
    CartDTO updateProductQuantity(Long productId, Integer quantity);

    CartDTO deleteProductFromActualCart(Long productId);

    CartDTO deleteProductFromCart(Long cartId, Long productId);


    void updateProductInCarts(Long cartId, Long productId);
}
