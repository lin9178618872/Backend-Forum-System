package com.resume.forum.service;

import com.resume.forum.domain.User;
import com.resume.forum.dto.CreateUserRequest;
import com.resume.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User create(CreateUserRequest request) {
        return userRepository.save(new User(request.username(), request.email()));
    }

    @Transactional(readOnly = true)
    public User getRequired(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
