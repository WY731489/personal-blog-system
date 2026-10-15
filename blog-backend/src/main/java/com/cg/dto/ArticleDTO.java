package com.cg.dto;

import com.cg.entity.Article;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题长度不能超过200")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        @Size(max = 500, message = "摘要长度不能超过500")
        private String summary;

        private String coverImage;

        @NotNull(message = "分类不能为空")
        private Long categoryId;

        private Set<Long> tagIds;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题长度不能超过200")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        @Size(max = 500, message = "摘要长度不能超过500")
        private String summary;

        private String coverImage;

        @NotNull(message = "分类不能为空")
        private Long categoryId;

        private Set<Long> tagIds;

        private Article.Status status;
    }

    @Data
    public static class ReviewRequest {
        @NotNull(message = "状态不能为空")
        private Article.Status status;

        private String reviewComment;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String summary;
        private String coverImage;
        private CategoryResponse category;
        private UserResponse author;
        private Integer views;
        private Integer likes;
        private Integer commentsCount;
        private Article.Status status;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Set<TagResponse> tags;
    }

    @Data
    public static class SimpleResponse {
        private Long id;
        private String title;
        private String summary;
        private String coverImage;
        private String categoryName;
        private Long categoryId;
        private String authorName;
        private Integer views;
        private Integer commentsCount;
        private LocalDateTime publishedAt;
        private Article.Status status;
    }

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
    }

    @Data
    public static class UserResponse {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }

    @Data
    public static class TagResponse {
        private Long id;
        private String name;
        private String color;
    }

    @Data
    public static class StatisticsResponse {
        private Long totalArticles;
        private Long publishedArticles;
        private Long pendingArticles;
        private Long draftArticles;
        private Long rejectedArticles;
    }
}