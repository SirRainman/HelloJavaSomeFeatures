package com.rain.server.serviceImpl;

import com.rain.common.User;
import com.rain.service.UserService;

/**
 * @Program: rain-java-ideas
 * @Description: 实现获取用户的操作
 * @Author: HouHao Ye
 * @Create: 2021-03-01 19:34
 **/
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(String id) {
        return new User("001", "Rain", "24");
    }

    @Override
    public Integer insertUser(User user) {
        return 200;
    }
}
