package com.resume.forum.search;

import com.resume.forum.domain.Post;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Getter
@Document(indexName = "forum_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Long)
    private long viewCount;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    public static PostDocument from(Post post) {
        PostDocument document = new PostDocument();
        document.id = post.getId();
        document.title = post.getTitle();
        document.content = post.getContent();
        document.author = post.getAuthor().getUsername();
        document.viewCount = post.getViewCount();
        document.createdAt = post.getCreatedAt();
        return document;
    }
}
