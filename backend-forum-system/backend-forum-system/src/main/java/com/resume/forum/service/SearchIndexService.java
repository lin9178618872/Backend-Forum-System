package com.resume.forum.service;

import com.resume.forum.domain.Post;
import com.resume.forum.search.PostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Async("forumExecutor")
    public void index(Post post) {
        try {
            elasticsearchOperations.save(PostDocument.from(post));
        } catch (RuntimeException ex) {
            log.warn("Skipped Elasticsearch indexing for post {}: {}", post.getId(), ex.getMessage());
        }
    }

    @Async("forumExecutor")
    public void delete(Long postId) {
        try {
            elasticsearchOperations.delete(postId.toString(), PostDocument.class);
        } catch (RuntimeException ex) {
            log.warn("Skipped Elasticsearch delete for post {}: {}", postId, ex.getMessage());
        }
    }
}
