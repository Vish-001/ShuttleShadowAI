package com.shuttleshadow.services;

import com.shuttleshadow.entities.Users;

public interface UserService {
    Users registerUser(String username, String password);
    Users authenticate(String username, String password);
    Users findById(Long id);
    Users findByUsername(String username);
    void save(Users user);
}
