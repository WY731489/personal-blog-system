package com.cg.Security;

import com.cg.entity.User;
import com.cg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("尝试加载用户: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        log.info("用户找到 - ID: {}, 状态: {}, 角色: {}", 
                user.getId(), user.getStatus(), user.getRole());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .accountExpired(false)
                .accountLocked(!user.getStatus())
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}