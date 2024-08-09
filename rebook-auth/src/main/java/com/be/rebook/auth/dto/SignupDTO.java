package com.be.rebook.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupDTO {

    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Username can only contain alphanumeric characters")
    @Email(message = "Invalid email format")
    private String email;
    private String username;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%^&+=]*$", message = "Password can only contain alphanumeric characters and '@','#','$','%','^','&','+','='")
    private String password;
}
