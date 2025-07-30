package com.shuttleshadow.services.impl;

import com.shuttleshadow.entities.Users;
import com.shuttleshadow.repositories.UserRepository;
import com.shuttleshadow.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Users registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken.");
        }
        return userRepository.save(new Users(username, password));
    }

    @Override
    public Users authenticate(String username, String password) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt.get();
        }
        throw new RuntimeException("Invalid credentials.");
    }


    @Override
    public Users findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public void save(Users user) {
        // Optional: hash password here
        userRepository.save(user);
    }
    @Override
    public Users findByUsername(String name) {
        return userRepository.findByUsername(name).orElse(null);
    }
}
