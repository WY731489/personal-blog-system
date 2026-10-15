package com.cg.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class AuthDTO {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
        private String password;

        @Email(message = "邮箱格式不正确", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        private String email;

        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 50, message = "昵称长度必须在2-50个字符之间")
        private String nickname;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserInfo user;

        @Data
        public static class UserInfo {
            private Long id;
            private String username;
            private String email;
            private String nickname;
            private String avatar;
            private String role;
        }
    }
}