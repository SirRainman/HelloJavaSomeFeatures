package com.rain.client;

import com.rain.common.User;
import com.rain.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-03-01 20:09
 **/
@Slf4j
public class Client {
    public static void main(String[] args) {
        UserService userService = (UserService) Stub.getStub(UserService.class);
        for(int i = 0; i < 10; i++) {
            log.info("UserID=123 is {}", userService.getUserById("123"));
            log.info("Insert User result is {}", userService.insertUser(new User("1", "Rain", "23")));
        }
    }
}
