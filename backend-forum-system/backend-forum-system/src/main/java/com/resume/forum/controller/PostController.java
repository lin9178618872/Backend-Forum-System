package com.resume.forum.controller;

import com.resume.forum.dto.CreatePostRequest;
import com.resume.forum.dto.FlushViewsResponse;
import com.resume.forum.dto.PostResponse;
import com.resume.forum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Value("${forum.hot-post-limit:20}")
    private long hotPostLimit;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody CreatePostRequest request) {
        return PostResponse.from(postService.create(request));
    }

    @GetMapping
    public Page<PostResponse> list(Pageable pageable) {
        return postService.list(pageable).map(PostResponse::from);
    }

    @GetMapping("/{postId}")
    public PostResponse get(@PathVariable Long postId) {
        return PostResponse.from(postService.getAndRecordView(postId));
    }

    @PostMapping("/{postId}/flush-views")
    public FlushViewsResponse flushViews(@PathVariable Long postId) {
        return new FlushViewsResponse(postId, postService.flushViews(postId));
    }

    @GetMapping("/hot")
    public java.util.List<Long> hot() {
        return postService.hotPostIds(hotPostLimit).stream()
                .map(Long::parseLong)
                .toList();
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long postId) {
        postService.delete(postId);
    }
}
