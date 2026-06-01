package com.resume.forum.controller;

import com.resume.forum.dto.SearchPostResponse;
import com.resume.forum.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final PostSearchService postSearchService;

    @GetMapping("/posts")
    public Page<SearchPostResponse> search(@RequestParam String keyword, Pageable pageable) {
        return postSearchService.search(keyword, pageable).map(SearchPostResponse::from);
    }
}
