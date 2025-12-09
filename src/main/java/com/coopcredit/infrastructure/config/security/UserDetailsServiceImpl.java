package com.coopcredit.infrastructure.config.security;

import com.coopcredit.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Implementation of UserDetailsService for loading user-specific data.
 * Used by Spring Security for authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (!user.getActive()) {
                        log.warn("Attempted login with inactive user: {}", username);
                        throw new UsernameNotFoundException("User is inactive: " + username);
                    }

                    log.debug("User {} loaded successfully with role: {}", username, user.getRole());

                    return User.builder()
                            .username(user.getUsername())
                            .password(user.getPassword())
                            .authorities(Collections.singletonList(
                                    new SimpleGrantedAuthority(user.getRole().name())))
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(!user.getActive())
                            .build();
                })
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }
}