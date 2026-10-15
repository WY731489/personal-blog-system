package com.cg.Service;

import com.cg.dto.AuthDTO;
import com.cg.entity.User;
import com.cg.repository.UserRepository;
import com.cg.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        log.info("尝试登录用户: {}", request.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            log.info("认证成功: {}", authentication.isAuthenticated());
            log.info("用户角色: {}", authentication.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String token = jwtTokenProvider.generateToken(authentication);
            log.info("生成的JWT Token: {}", token.substring(0, 50) + "...");

            AuthDTO.LoginResponse response = new AuthDTO.LoginResponse();
            response.setToken(token);

            AuthDTO.LoginResponse.UserInfo userInfo = new AuthDTO.LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setRole(user.getRole().name());

            response.setUser(userInfo);
            log.info("登录成功，用户ID: {}", user.getId());

            return response;
            
        } catch (AuthenticationException e) {
            log.error("认证失败: {}", e.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    @Transactional
    public AuthDTO.LoginResponse register(AuthDTO.RegisterRequest request) {
        log.info("开始注册用户: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("用户名已存在: {}", request.getUsername());
            throw new IllegalArgumentException("用户名已存在");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty() && userRepository.existsByEmail(request.getEmail())) {
            log.warn("邮箱已存在: {}", request.getEmail());
            throw new IllegalArgumentException("邮箱已被注册");
        }

        log.info("创建新用户对象...");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(User.Role.ROLE_USER);
        user.setStatus(true);

        log.info("保存用户到数据库: {}", request.getUsername());
        User savedUser = userRepository.save(user);
        log.info("用户已保存，用户ID: {}", savedUser.getId());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        AuthDTO.LoginResponse response = new AuthDTO.LoginResponse();
        response.setToken(token);

        AuthDTO.LoginResponse.UserInfo userInfo = new AuthDTO.LoginResponse.UserInfo();
        userInfo.setId(savedUser.getId());
        userInfo.setUsername(savedUser.getUsername());
        userInfo.setEmail(savedUser.getEmail());
        userInfo.setNickname(savedUser.getNickname());
        userInfo.setAvatar(savedUser.getAvatar());
        userInfo.setRole(savedUser.getRole().name());

        response.setUser(userInfo);
        log.info("注册完成，用户信息: {}", response.getUser());

        return response;
    }
}