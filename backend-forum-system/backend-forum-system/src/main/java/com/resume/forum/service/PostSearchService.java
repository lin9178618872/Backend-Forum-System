package com.resume.forum.service;

import com.resume.forum.search.PostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public Page<PostDocument> search(String keyword, Pageable pageable) {
        Criteria criteria = new Criteria("title").contains(keyword)
                .or(new Criteria("content").contains(keyword));
        CriteriaQuery query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<PostDocument> hits = elasticsearchOperations.search(query, PostDocument.class);
        return new PageImpl<>(
                hits.stream().map(SearchHit::getContent).toList(),
                pageable,
                hits.getTotalHits()
        );
    }
}
