package com.ecommerce.project.services;

import com.ecommerce.project.models.User;
import com.ecommerce.project.payload.AddressDTO;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);
}
