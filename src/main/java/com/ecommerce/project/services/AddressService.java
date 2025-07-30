package com.ecommerce.project.services;

import com.ecommerce.project.models.User;
import com.ecommerce.project.payload.AddressDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);
    List<AddressDTO> getAllAddresses();
    AddressDTO getAddressById(Long addressId);
    List<AddressDTO> getLoggedUserAddresses();
    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);
    AddressDTO deleteAddress(Long addressId);
}
