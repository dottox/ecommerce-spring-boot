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
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
            userCart.setTotalPrice(0.00);
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
                    throw new ApiException("Product " + product.getName() + " already exists in the cart");
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

    @Override
    public List<CartDTO> getAllCarts() {
        // Get all carts from the repository
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new ApiException("No carts available", HttpStatus.NOT_FOUND);
        }

        // Create a list of CartDTOs from the carts retrieved
        List<CartDTO> cartDTOs = carts.stream()
                .map(cart -> {
                    // Map each Cart to CartDTO using ModelMapper
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    // Create a list of ProductDTOs from the cart items
                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(ci -> {
                                ProductDTO productDTO = modelMapper.map(ci.getProduct(), ProductDTO.class);
                                productDTO.setQuantity(ci.getQuantity()); // Set the quantity of the cart item to the ProductDTO, because the product has the stock quantity
                                return productDTO;
                            })
                            .toList();
                    // Set the products in the CartDTO
                    cartDTO.setProducts(products);
                    return cartDTO;
                }).toList(); // Collect the CartDTOs into a list

        return cartDTOs;
    }

    @Override
    public CartDTO getCartLoggedUser() {

        // Get the logged-in user's email and find their cart
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ApiException("Cart not found for user with email: " + email, HttpStatus.NOT_FOUND);
        }

        // Map the Cart to CartDTO and add the products from the cart items
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(cartItem -> {
                    ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(cartItem.getQuantity());
                    return productDTO;
                }).collect(Collectors.toList());
        cartDTO.setProducts(productDTOs);

        return cartDTO;
    }
}
