package com.resume.forum.dto;

import com.resume.forum.search.PostDocument;

import java.time.Instant;

public record SearchPostResponse(
        Long id,
        String title,
        String content,
        String author,
        long viewCount,
        Instant createdAt
) {
    public static SearchPostResponse from(PostDocument document) {
        return new SearchPostResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getAuthor(),
                document.getViewCount(),
                document.getCreatedAt()
        );
    }
}
