package com.udhayanidhi.fitly.service;

import com.udhayanidhi.fitly.dto.UserRequest;
import com.udhayanidhi.fitly.model.User;
import com.udhayanidhi.fitly.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }
}