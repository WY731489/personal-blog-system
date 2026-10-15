package com.cg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class CategoryDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称不能超过50个字符")
        private String name;

        @Size(max = 200, message = "描述不能超过200个字符")
        private String description;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称不能超过50个字符")
        private String name;

        @Size(max = 200, message = "描述不能超过200个字符")
        private String description;

        private Boolean status;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Boolean status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}