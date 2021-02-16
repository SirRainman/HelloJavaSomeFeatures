package dynamicProxy.staticProxyPattern;

import lombok.extern.slf4j.Slf4j;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-16 11:53
 **/
@Slf4j
public class Main {
    public static void main(String[] args) {
        UserServiceInterface userService = new UserServiceImpl();
        UserServiceProxy proxy = new UserServiceProxy(userService);
        log.info("Main ---> receive request from client");
        log.info("Main ---> send request to proxy");
        proxy.addUser("Doria");
        log.info("Main ---> get result from proxy");
        log.info("Main ---> respond result to client");
    }
}
