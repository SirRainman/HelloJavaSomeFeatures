package com.rain.client;

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
        log.info("{}", userService.getUserById("123"));
    }
}
