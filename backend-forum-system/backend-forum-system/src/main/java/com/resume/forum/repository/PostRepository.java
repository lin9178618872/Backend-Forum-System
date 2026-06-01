package com.resume.forum.repository;

import com.resume.forum.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Post> findById(Long id);

    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
