package com.resume.forum.service;

import com.resume.forum.domain.Comment;
import com.resume.forum.domain.Post;
import com.resume.forum.domain.User;
import com.resume.forum.dto.CreateCommentRequest;
import com.resume.forum.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    @Transactional
    @CacheEvict(value = "post-comments", key = "#postId")
    public Comment create(Long postId, CreateCommentRequest request) {
        Post post = postService.getRequired(postId);
        User author = userService.getRequired(request.authorId());
        return commentRepository.save(new Comment(post, author, request.content()));
    }

    @Transactional(readOnly = true)
    public Page<Comment> list(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable);
    }
}
