package dynamicProxy.staticProxyPattern;

import lombok.extern.slf4j.Slf4j;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-16 11:49
 **/
@Slf4j
public class UserServiceProxy implements UserServiceInterface{
    private UserServiceInterface userService;

    public UserServiceProxy(UserServiceInterface userService) {
        this.userService = userService;
    }

    @Override
    public void addUser(String userName) {
        log.info("Proxy ---> start");
        log.info("Proxy ---> filter some thing...");
        userService.addUser(userName);
        log.info("Proxy ---> handle result thing...");
        log.info("Proxy ---> end");
    }

    @Override
    public void delUser(String userName) {
        userService.delUser(userName);
    }

    @Override
    public void updateUser(String userName) {
        userService.updateUser(userName);
    }

    @Override
    public String getUser(String userName) {
        return userService.getUser(userName);
    }
}
