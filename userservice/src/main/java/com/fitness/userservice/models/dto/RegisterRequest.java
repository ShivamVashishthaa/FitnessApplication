package com.fitness.userservice.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Message formate")
    private String email;
    @NotBlank(message = "Password is Required")
    @Size(min = 6, message = "must have aleast 6 character long")
    private String password;
    private String firstName;
    private String lastName;
}
