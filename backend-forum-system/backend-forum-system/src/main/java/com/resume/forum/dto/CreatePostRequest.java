package com.resume.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull Long authorId,
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 10000) String content
) {
}
