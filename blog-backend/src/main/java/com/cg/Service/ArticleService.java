package com.cg.Service;

import com.cg.dto.ArticleDTO;
import com.cg.entity.*;
import com.cg.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    @Transactional
    public ArticleDTO.Response createArticle(ArticleDTO.CreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setCategory(category);
        article.setAuthor(author);
        article.setStatus(Article.Status.PENDING);  // 用户发布文章需要审核
        article.setPublishedAt(null);  // 待审核状态，不设置发布时间

        // 处理标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            article.setTags(tags);
        }

        articleRepository.save(article);

        return convertToResponse(article);
    }

    @Transactional
    public ArticleDTO.Response updateArticle(Long id, ArticleDTO.UpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查权限：作者或管理员可以修改
        if (!article.getAuthor().getUsername().equals(username) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new RuntimeException("没有权限修改此文章");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setCategory(category);

        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
            if (request.getStatus() == Article.Status.PUBLISHED) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }

        // 更新标签
        if (request.getTagIds() != null) {
            // 删除原有标签关联
            articleTagRepository.deleteByArticleId(article.getId());

            // 添加新标签
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            article.setTags(tags);
        }

        articleRepository.save(article);

        return convertToResponse(article);
    }

    @Transactional(readOnly = true)
    public ArticleDTO.Response getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        if (article.getIsDeleted()) {
            throw new RuntimeException("文章已被删除");
        }

        // 增加浏览量
        article.setViews(article.getViews() + 1);
        articleRepository.save(article);

        return convertToResponse(article);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO.SimpleResponse> getPublishedArticles(Long categoryId, Pageable pageable) {
        Page<Article> articles;
        if (categoryId != null) {
            articles = articleRepository.findByCategoryIdAndStatusAndIsDeletedFalse(
                    categoryId, Article.Status.PUBLISHED, pageable);
        } else {
            articles = articleRepository.findByStatusAndIsDeletedFalse(Article.Status.PUBLISHED, pageable);
        }

        return articles.map(this::convertToSimpleResponse);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO.Response> getUserArticles(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Page<Article> articles = articleRepository.findByAuthorIdAndIsDeletedFalse(user.getId(), pageable);

        return articles.map(this::convertToResponse);
    }

    @Transactional
    public void deleteArticle(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查权限：作者或管理员可以删除
        if (!article.getAuthor().getUsername().equals(username) &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new RuntimeException("没有权限删除此文章");
        }

        articleRepository.delete(article);
    }

    @Transactional
    public ArticleDTO.Response reviewArticle(Long id, ArticleDTO.ReviewRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        article.setStatus(request.getStatus());
        if (request.getStatus() == Article.Status.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleRepository.save(article);

        return convertToResponse(article);
    }

    // 添加缺失的方法
    @Transactional(readOnly = true)
    public Page<ArticleDTO.Response> getArticlesByStatus(Article.Status status, Pageable pageable) {
        Page<Article> articles = articleRepository.findByStatus(status, pageable);
        return articles.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO.Response> getAllArticles(Article.Status status, Pageable pageable) {
        Page<Article> articles;
        if (status != null) {
            articles = articleRepository.findByIsDeletedFalseAndStatus(status, pageable);
        } else {
            articles = articleRepository.findByIsDeletedFalse(pageable);
        }
        return articles.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO.Response> getPendingArticles(Pageable pageable) {
        Page<Article> articles = articleRepository.findByIsDeletedFalseAndStatus(Article.Status.PENDING, pageable);
        return articles.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public ArticleDTO.StatisticsResponse getStatistics() {
        ArticleDTO.StatisticsResponse stats = new ArticleDTO.StatisticsResponse();
        stats.setTotalArticles(articleRepository.countByIsDeletedFalse()); // 修改：移除传递的参数
        stats.setPublishedArticles(articleRepository.countByStatusAndIsDeletedFalse(Article.Status.PUBLISHED));
        stats.setPendingArticles(articleRepository.countByStatusAndIsDeletedFalse(Article.Status.PENDING));
        stats.setDraftArticles(articleRepository.countByStatusAndIsDeletedFalse(Article.Status.DRAFT));
        stats.setRejectedArticles(articleRepository.countByStatusAndIsDeletedFalse(Article.Status.REJECTED));
        return stats;
    }

    private ArticleDTO.Response convertToResponse(Article article) {
        ArticleDTO.Response response = new ArticleDTO.Response();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setContent(article.getContent());
        response.setSummary(article.getSummary());
        response.setCoverImage(article.getCoverImage());
        response.setViews(article.getViews());
        response.setLikes(article.getLikes());
        response.setCommentsCount(article.getCommentsCount());
        response.setStatus(article.getStatus());
        response.setPublishedAt(article.getPublishedAt());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());

        // 分类信息
        ArticleDTO.CategoryResponse categoryResponse = new ArticleDTO.CategoryResponse();
        categoryResponse.setId(article.getCategory().getId());
        categoryResponse.setName(article.getCategory().getName());
        response.setCategory(categoryResponse);

        // 作者信息
        ArticleDTO.UserResponse userResponse = new ArticleDTO.UserResponse();
        userResponse.setId(article.getAuthor().getId());
        userResponse.setUsername(article.getAuthor().getUsername());
        userResponse.setNickname(article.getAuthor().getNickname());
        userResponse.setAvatar(article.getAuthor().getAvatar());
        response.setAuthor(userResponse);

        // 标签信息
        Set<ArticleDTO.TagResponse> tagResponses = new HashSet<>();
        if (article.getTags() != null) {
            tagResponses = article.getTags().stream()
                    .map(tag -> {
                        ArticleDTO.TagResponse tagResponse = new ArticleDTO.TagResponse();
                        tagResponse.setId(tag.getId());
                        tagResponse.setName(tag.getName());
                        tagResponse.setColor(tag.getColor());
                        return tagResponse;
                    })
                    .collect(Collectors.toSet());
        }
        response.setTags(tagResponses);

        return response;
    }

    // 添加缺失的方法
    @Transactional(readOnly = true)
    public Page<ArticleDTO.SimpleResponse> getMyArticles(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Page<Article> articles = articleRepository.findByAuthorUsernameAndIsDeletedFalseOrderByCreatedAtDesc(username, pageable);
        return articles.map(article -> {
            ArticleDTO.SimpleResponse response = new ArticleDTO.SimpleResponse();
            response.setId(article.getId());
            response.setTitle(article.getTitle());
            response.setSummary(article.getSummary());
            response.setCoverImage(article.getCoverImage());
            response.setCategoryName(article.getCategory() != null ? article.getCategory().getName() : "");
            response.setCategoryId(article.getCategory() != null ? article.getCategory().getId() : null);
            response.setAuthorName(article.getAuthor().getNickname() != null ? article.getAuthor().getNickname() : article.getAuthor().getUsername());
            response.setViews(article.getViews());
            response.setCommentsCount(article.getCommentsCount());
            response.setPublishedAt(article.getPublishedAt());
            response.setStatus(article.getStatus());
            return response;
        });
    }
    
    private ArticleDTO.SimpleResponse convertToSimpleResponse(Article article) {
        ArticleDTO.SimpleResponse response = new ArticleDTO.SimpleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSummary(article.getSummary());
        response.setCoverImage(article.getCoverImage());
        response.setCategoryName(article.getCategory() != null ? article.getCategory().getName() : "");
        response.setCategoryId(article.getCategory() != null ? article.getCategory().getId() : null);
        response.setAuthorName(article.getAuthor().getNickname() != null ? article.getAuthor().getNickname() : article.getAuthor().getUsername());
        response.setViews(article.getViews());
        response.setCommentsCount(article.getCommentsCount());
        response.setPublishedAt(article.getPublishedAt());
        response.setStatus(article.getStatus());
        return response;
    }
}