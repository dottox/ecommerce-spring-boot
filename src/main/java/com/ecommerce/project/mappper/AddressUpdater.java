package com.ecommerce.project.mappper;

import com.ecommerce.project.models.Address;
import com.ecommerce.project.payload.AddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddressUpdater {

    @Autowired
    private ModelMapper modelMapper;

    public void updateAddress(Address existing, AddressDTO updateDTO) {

        Address updatedAddress = modelMapper.map(updateDTO, Address.class);

        if (updateDTO.getStreet() != null) {
            existing.setStreet(updatedAddress.getStreet());
        }
        if (updateDTO.getCity() != null) {
            existing.setCity(updatedAddress.getCity());
        }
        if (updateDTO.getState() != null) {
            existing.setState(updatedAddress.getState());
        }
        if (updateDTO.getNumber() != null) {
            existing.setNumber(updatedAddress.getNumber());
        }
        if (updateDTO.getCountry() != null) {
            existing.setCountry(updatedAddress.getCountry());
        }
        if (updateDTO.getPincode() != null) {
            existing.setPincode(updatedAddress.getPincode());
        }

    }

}
