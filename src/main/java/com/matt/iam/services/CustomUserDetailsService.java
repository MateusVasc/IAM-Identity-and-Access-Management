package com.matt.iam.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.matt.iam.exception.CustomException;
import com.matt.iam.exception.ExceptionMessages;
import com.matt.iam.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByEmail(username)
            .map(user -> new User(
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities()
            ))
            .orElseThrow(() -> new CustomException(ExceptionMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
