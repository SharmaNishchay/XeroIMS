package com.darkhex.xeroims.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "Supplier name cannot be blank")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    private String phone;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;
}
