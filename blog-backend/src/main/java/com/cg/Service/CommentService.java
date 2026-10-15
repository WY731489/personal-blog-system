package com.cg.Service;

import com.cg.dto.CommentDTO;
import com.cg.entity.Article;
import com.cg.entity.Comment;
import com.cg.entity.User;
import com.cg.repository.ArticleRepository;
import com.cg.repository.CommentRepository;
import com.cg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<CommentDTO.Response> getCommentsByArticleId(Long articleId, Pageable pageable) {
        return commentRepository.findByArticleIdAndIsDeletedFalse(articleId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public CommentDTO.Response createComment(Long articleId, CommentDTO.CreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setUser(user);
        comment.setArticle(article);
        comment.setIsDeleted(false);

        Comment savedComment = commentRepository.save(comment);

        // 更新文章评论数
        article.setCommentsCount(article.getCommentsCount() + 1);
        articleRepository.save(article);

        return convertToResponse(savedComment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 检查权限：评论作者或管理员可以删除
        if (!comment.getUser().getUsername().equals(username) &&
                !authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("没有权限删除此评论");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);

        // 更新文章评论数（实际项目中可能需要更精确的统计）
        Article article = comment.getArticle();
        article.setCommentsCount(Math.max(0, article.getCommentsCount() - 1));
        articleRepository.save(article);
    }

    private CommentDTO.Response convertToResponse(Comment comment) {
        CommentDTO.Response response = new CommentDTO.Response();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParentId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        CommentDTO.Response.UserInfo userInfo = new CommentDTO.Response.UserInfo();
        userInfo.setId(comment.getUser().getId());
        userInfo.setUsername(comment.getUser().getUsername());
        userInfo.setNickname(comment.getUser().getNickname());
        userInfo.setAvatar(comment.getUser().getAvatar());
        response.setUser(userInfo);

        return response;
    }
}