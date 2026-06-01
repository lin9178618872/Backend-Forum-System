package com.resume.forum.dto;

import com.resume.forum.domain.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        String author,
        String content,
        Instant createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getUsername(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
