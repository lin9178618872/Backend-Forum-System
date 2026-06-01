package com.resume.forum.controller;

import com.resume.forum.dto.CommentResponse;
import com.resume.forum.dto.CreateCommentRequest;
import com.resume.forum.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request) {
        return CommentResponse.from(commentService.create(postId, request));
    }

    @GetMapping
    public Page<CommentResponse> list(@PathVariable Long postId, Pageable pageable) {
        return commentService.list(postId, pageable).map(CommentResponse::from);
    }
}
