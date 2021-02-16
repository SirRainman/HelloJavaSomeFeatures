package dynamicProxy.staticProxyPattern;

import lombok.extern.slf4j.Slf4j;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-16 11:46
 **/
@Slf4j
public class UserServiceImpl implements UserServiceInterface {
    @Override
    public void addUser(String userName) {
        log.info("UserServiceImpl.addUser()");
    }

    @Override
    public void delUser(String userName) {
        log.info("UserServiceImpl.delUser()");
    }

    @Override
    public void updateUser(String userName) {
        log.info("UserServiceImpl.updateUser()");
    }

    @Override
    public String getUser(String userName) {
        log.info("UserServiceImpl.getUser()");
        return "UserServiceImpl.Rain";
    }
}
