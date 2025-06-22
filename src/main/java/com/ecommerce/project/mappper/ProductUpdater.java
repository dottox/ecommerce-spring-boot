package com.ecommerce.project.mappper;

import com.ecommerce.project.models.Product;
import com.ecommerce.project.payload.ProductDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductUpdater {

    @Autowired
    ModelMapper modelMapper;

    public void updateProduct(Product existing, ProductDTO updateDTO) {

        Product update = modelMapper.map(updateDTO, Product.class);

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getPrice() != null) {
            existing.setPrice(update.getPrice());
        }
        if (update.getDiscount() != null) {
            existing.setDiscount(update.getDiscount());
        }
        if (update.getSpecialPrice() != null) {
            existing.setSpecialPrice(update.getSpecialPrice());
        }
    }
}
