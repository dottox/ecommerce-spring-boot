package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.models.Cart;
import com.ecommerce.project.models.CartItem;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart == null) {
            userCart = new Cart();
            userCart.setUser(authUtil.loggedInUser());
            return cartRepository.save(userCart);
        } else {
            return userCart;
        }
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart or create a new one
        Cart cart = createCart();

        // Retrieve product details
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Perform validations
        if (quantity <= 0) {
            throw new ApiException("Quantity must be greater than zero");
        }

        cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId)
                .ifPresent(cartItem -> {
                    throw new ApiException("Product + " + product.getName() + " already exists in the cart");
                });

        if (product.getQuantity() == 0) {
            throw new ApiException("Product " + product.getName() + " is out of stock");
        }

        if (product.getQuantity() < quantity) {
            throw new ApiException("Only " + product.getQuantity() + " items of " + product.getName() + " are available");
        }

        // Create cart item
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setCart(cart);
        cartItem.setQuantity(quantity);
        cartItem.setDiscount(product.getDiscount());
        cartItem.setSpecialPrice(product.getSpecialPrice());

        // Save cart item
        cartItemRepository.save(cartItem);

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cart.setQuantity(cart.getQuantity() + quantity);
        cart.getCartItems().add(cartItem);
        Cart newCart = cartRepository.save(cart);

        // Return updated cart
        CartDTO cartDTO = modelMapper.map(newCart, CartDTO.class);

        Stream<ProductDTO> productDTOStream = newCart.getCartItems().stream().map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
        });

        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }
}
