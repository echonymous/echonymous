package com.echonymous.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email cannot be blank.")
    private String email;

    @NotBlank(message = "Username cannot be blank.")
    private String username;

    @NotBlank(message = "Password cannot be blank.")
    private String password;
}
