package com.resume.forum.dto;

import com.resume.forum.domain.Post;

import java.time.Instant;

public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        long viewCount,
        Instant createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
