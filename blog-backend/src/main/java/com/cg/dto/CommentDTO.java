package com.cg.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CommentDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "评论内容不能为空")
        private String content;

        private Long parentId = 0L;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank(message = "评论内容不能为空")
        private String content;
    }

    @Data
    public static class Response {
        private Long id;
        private String content;
        private Long parentId;
        private UserInfo user;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Data
        public static class UserInfo {
            private Long id;
            private String username;
            private String nickname;
            private String avatar;
        }
    }
}