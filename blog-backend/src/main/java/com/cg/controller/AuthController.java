package com.cg.controller;

import com.cg.dto.AuthDTO;
import com.cg.Service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        log.info("登录请求到达，用户名: {}", request.getUsername());
        try {
            AuthDTO.LoginResponse response = authService.login(request);
            log.info("登录成功，用户名: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败，用户名: {}, 错误: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        log.info("注册请求到达控制器，用户名: {}, 邮箱: {}, 昵称: {}", 
                request.getUsername(), request.getEmail(), request.getNickname());
        
        try {
            log.info("开始调用 AuthService.register()");
            AuthDTO.LoginResponse response = authService.register(request);
            log.info("注册成功，用户名: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("注册失败，用户名: {}, 错误: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("登出成功");
    }
}