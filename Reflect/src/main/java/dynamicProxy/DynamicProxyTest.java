package dynamicProxy;

/**
 * @program: rain-java-ideas
 * @description: 测试动态代理
 * @author: Rain
 * @create: 2021-02-16 20:59
 **/
public class DynamicProxyTest {
    public static void main(String[] args) {
        // 被代理类对象
        SuperMan superMan = new SuperMan();
        // 获得代理类对象
        Human human = (Human) DynamicProxyFactory.getProxyInstance(superMan);
        // 通过代理类对象调用相应的方法时，代理类对象会自动的调用被代理类中 与该方法同名的方法
        human.morning("Doria");
    }
}
