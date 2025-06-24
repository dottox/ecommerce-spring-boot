package com.ecommerce.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 1, max = 5, message = "Number must be between 1 and 5 characters")
    private Integer number;

    @NotBlank
    @Size(min = 3, max = 50, message = "Street must be between 3 and 50 characters")
    private String street;

    @NotBlank
    @Size(min = 2, max = 30, message = "City must be between 2 and 30 characters")
    private String city;

    @NotBlank
    @Size(min = 2, max = 30, message = "State must be between 2 and 30 characters")
    private String state;

    @NotBlank
    @Size(min = 2, max = 20, message = "Country must be between 2 and 20 characters")
    private String country;

    @NotBlank
    @Size(min = 6, max = 10, message = "Pincode must be between 6 and 10 characters")
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();

    public Address(Integer number, String street, String city, String state, String country, String pincode) {
        this.number = number;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
    }
}

