package com.rain.service;

import com.rain.common.User;

public interface UserService {
    User getUserById(String id );
    Integer insertUser(User user);
}
