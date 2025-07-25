package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.models.Cart;
import com.ecommerce.project.models.CartItem;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private CartDTO getCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(ci -> {
                    ProductDTO productDTO = modelMapper.map(ci.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(ci.getQuantity());
                    return productDTO;
                }).toList();
        cartDTO.setProducts(productDTOs);

        return cartDTO;
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

        if (cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId) != null) {
            throw new ApiException("Product " + product.getName() + " already exists in the cart");
        }

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
        cartItem.setTotalPrice(product.getSpecialPrice() * quantity);

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

        return carts.stream()
                .map(this::getCartDTO).toList();
    }

    @Override
    public CartDTO getCartLoggedUser() {

        // Get the logged-in user's email and find their cart
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", email);
        }

        return getCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantity(Long productId, Integer quantity) {
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", email);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Perform validations
        if (product.getQuantity() == 0) {
            throw new ApiException("Product " + product.getName() + " is out of stock");
        }

        if (product.getQuantity() < quantity) {
            throw new ApiException("Only " + product.getQuantity() + " items of " + product.getName() + " are available");
        }

        // Check if the cart item with the product exists
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "ProductId", productId);
        }

        Double changePrice = product.getSpecialPrice() * quantity;

        // Update details of the cart item
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setDiscount(product.getDiscount());
        cartItem.setTotalPrice(cartItem.getTotalPrice() + changePrice);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Update the cart total price
        cart.setTotalPrice(cart.getTotalPrice() + changePrice);
        cartRepository.save(cart);

        // Update the product quantity in the repository
        cartItem.getProduct().setQuantity(product.getQuantity() - quantity);
        productRepository.save(cartItem.getProduct());

        // If the new quantity is zero:
        // delete the cart item and remove it from the cart and the product
        if (updatedCartItem.getQuantity() <= 0) {
            cart.getCartItems().remove(updatedCartItem);
            cartItem.getProduct().getCartItems().remove(updatedCartItem);
            cartItemRepository.delete(updatedCartItem);
        }


        // Map the Cart to CartDTO and add the products from the cart items
        return getCartDTO(cart);
    }

    @Override
    public CartDTO deleteProductFromActualCart(Long productId) {
        // Get the logged-in user's email and find their cart
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", email);
        }

        // Check if the product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Check if the product is in the cart
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "ProductId", productId);
        }

        // Update total price before deleting the cart item
        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getTotalPrice());

        // Remove the cart item from the cart and product
        cart.getCartItems().remove(cartItem);
        cartItem.getProduct().getCartItems().remove(cartItem);
        cartRepository.save(cart);
        productRepository.save(cartItem.getProduct());

        // Delete the cart item
        cartItemRepository.delete(cartItem);

        return getCartDTO(cart);
    }

    @Override
    public CartDTO deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findCartByCartId(cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        // Check if the product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        // Check if the product is in the cart
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "ProductId", productId);
        }

        // Update total price before deleting the cart item
        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getTotalPrice());

        // Remove the cart item from the cart and product
        cart.getCartItems().remove(cartItem);
        cartItem.getProduct().getCartItems().remove(cartItem);
        cartRepository.save(cart);
        productRepository.save(cartItem.getProduct());

        // Delete the cart item
        cartItemRepository.delete(cartItem);

        return getCartDTO(cart);
    }


    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "CartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);
        if (cartItem == null) {
            throw new ApiException("Product " + product.getName() + " not found in cart with ID " + cartId, HttpStatus.NOT_FOUND);
        }

        // Get the total price of the cart without the cart item
        double cartPrice = cart.getTotalPrice() - cartItem.getTotalPrice();

        // Update the cart item with the new product details
        cartItem.setTotalPrice(cartItem.getProduct().getSpecialPrice() * cartItem.getQuantity());

        // Set the total price of the cart with the new cart item total price
        cart.setTotalPrice(cartPrice + cartItem.getTotalPrice());

    }
}
