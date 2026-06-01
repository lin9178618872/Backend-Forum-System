package com.resume.forum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 40) String username,
        @NotBlank @Email @Size(max = 80) String email
) {
}
