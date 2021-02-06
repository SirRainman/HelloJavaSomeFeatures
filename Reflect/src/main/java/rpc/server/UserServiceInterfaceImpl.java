package rpc.server;

import rpc.common.User;

public class UserServiceInterfaceImpl {
    public User getUserById(String id) {
        return new User(id, "Rain", "23");
    }
}
