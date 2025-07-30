package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.mappper.AddressUpdater;
import com.ecommerce.project.models.Address;
import com.ecommerce.project.models.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressUpdater addressUpdater;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        // Map the DTO to the Address entity
        Address address = modelMapper.map(addressDTO, Address.class);

        // Get the addresses of the user, add the new address and set the updated list
        List<Address> addressesUser = user.getAddresses();
        addressesUser.add(address);
        user.setAddresses(addressesUser);

        // Set the user to the address
        address.setUser(user);

        // Save the address and return the DTO
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getLoggedUserAddresses() {
        User loggedUser = authUtil.loggedInUser();
        return loggedUser.getAddresses().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        // Check if address owner is the logged user
        User loggedUser = authUtil.loggedInUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        if (!loggedUser.getAddresses().contains(address)) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }

        // Update the address using the AddressUpdater
        addressUpdater.updateAddress(address, addressDTO);

        // Save the updated address and return the DTO
        Address updatedAddress = addressRepository.save(address);

        // Update the user's address list
        loggedUser.getAddresses().removeIf(addr -> addr.getAddressId().equals(addressId));
        loggedUser.getAddresses().add(updatedAddress);
        userRepository.save(loggedUser);

        return modelMapper.map(updatedAddress, AddressDTO.class);

    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        // Check if address owner is the logged user
        User loggedUser = authUtil.loggedInUser();
        if (!loggedUser.getAddresses().contains(address)) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }

        // Remove the address from the user's address list
        loggedUser.getAddresses().removeIf(addr -> addr.getAddressId().equals(addressId));
        userRepository.save(loggedUser);

        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);

        // Delete the address
        addressRepository.delete(address);
        return addressDTO;
    }
}
