package com.resume.forum.service;

import com.resume.forum.domain.Post;
import com.resume.forum.domain.User;
import com.resume.forum.dto.CreatePostRequest;
import com.resume.forum.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final String POST_VIEW_KEY = "post:views:";
    private static final String HOT_POST_KEY = "post:hot";

    private final PostRepository postRepository;
    private final UserService userService;
    private final SearchIndexService searchIndexService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post create(CreatePostRequest request) {
        User author = userService.getRequired(request.authorId());
        Post post = postRepository.save(new Post(author, request.title(), request.content()));
        searchIndexService.index(post);
        return post;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<Post> list(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public Post getAndRecordView(Long postId) {
        Post post = getRequired(postId);
        try {
            redisTemplate.opsForValue().increment(POST_VIEW_KEY + postId);
            redisTemplate.opsForZSet().incrementScore(HOT_POST_KEY, postId.toString(), 1);
        } catch (RuntimeException ex) {
            log.warn("Skipped Redis view tracking for post {}: {}", postId, ex.getMessage());
        }
        return post;
    }

    @Transactional
    public long flushViews(Long postId) {
        String key = POST_VIEW_KEY + postId;
        String views;
        try {
            views = redisTemplate.opsForValue().getAndDelete(key);
        } catch (RuntimeException ex) {
            log.warn("Skipped Redis view flush for post {}: {}", postId, ex.getMessage());
            return 0;
        }
        long delta = views == null ? 0 : Long.parseLong(views);
        if (delta > 0) {
            Post post = getRequired(postId);
            post.addViews(delta);
            searchIndexService.index(post);
        }
        return delta;
    }

    @Transactional(readOnly = true)
    public List<String> hotPostIds(long limit) {
        Set<String> ids;
        try {
            ids = redisTemplate.opsForZSet().reverseRange(HOT_POST_KEY, 0, limit - 1);
        } catch (RuntimeException ex) {
            log.warn("Skipped Redis hot post lookup: {}", ex.getMessage());
            return Collections.emptyList();
        }
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().toList();
    }

    @Transactional(readOnly = true)
    public Post getRequired(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void delete(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found: " + postId);
        }
        postRepository.deleteById(postId);
        searchIndexService.delete(postId);
        redisTemplate.delete(POST_VIEW_KEY + postId);
        redisTemplate.opsForZSet().remove(HOT_POST_KEY, Objects.toString(postId));
    }
}
