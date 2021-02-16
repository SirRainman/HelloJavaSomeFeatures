package dynamicProxy.staticProxyPattern;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-16 11:44
 **/
public interface UserServiceInterface {
    public void addUser(String userName);
    public void delUser(String userName);
    public void updateUser(String userName);
    public String getUser(String userName);
}
