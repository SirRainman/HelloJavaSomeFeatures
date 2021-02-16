package dynamicProxy;

import lombok.extern.slf4j.Slf4j;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-16 20:25
 **/
@Slf4j
public class SuperMan implements Human{
    @Override
    public void morning(String name) {
        log.info("*** SuperHuman say hello to {} ***", name);
    }
}
