package com.ecommerce.project.controllers;

import com.ecommerce.project.models.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.services.AddressService;
import com.ecommerce.project.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressService addressService;

    @Tag(name = "Address Management", description = "APIs for managing addresses")
    @Operation(summary = "Create address", description = "API to create a new address for the logged-in user")
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(
            @Valid @RequestBody AddressDTO addressDTO
    ) {
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @Tag(name = "Address Management", description = "APIs for managing addresses")
    @Operation(summary = "Get all addresses", description = "API to retrieve all addresses")
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addressList = addressService.getAllAddresses();
        return new ResponseEntity<>(addressList, HttpStatus.OK);
    }

    @Tag(name = "Address Management", description = "APIs for managing addresses")
    @Operation(summary = "Get address by ID", description = "API to retrieve an address by its ID")
    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(
            @PathVariable Long addressId
    ) {
        AddressDTO address = addressService.getAddressById(addressId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @Tag(name = "Address Management", description = "APIs for managing addresses")
    @Operation(summary = "Get logged user's addresses", description = "API to retrieve all addresses of the logged-in user")
    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getLoggedUserAddresses() {
        List<AddressDTO> addressList = addressService.getLoggedUserAddresses();
        return new ResponseEntity<>(addressList, HttpStatus.OK);
    }

    @Tag(name = "Address Management", description = "APIs for managing addresses")
    @Operation(summary = "Update address by ID", description = "API to update an address by its ID")
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO
    ) {
        AddressDTO updatedAddress = addressService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @Tag(name = "Address Management", description = "APIs for managing addresses")
    @Operation(summary = "Delete address by ID", description = "API to delete an address by its ID")
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> deleteAddressById(
            @PathVariable Long addressId
    ) {
        AddressDTO address = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }
}
