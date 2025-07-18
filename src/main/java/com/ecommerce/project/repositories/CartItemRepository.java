package com.ecommerce.project.repositories;

import com.ecommerce.project.models.Cart;
import com.ecommerce.project.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    CartItem findCartItemByCartIdAndProductId(Long cartId, Long productId);
    // Additional methods for CartItem can be defined here if needed
}
