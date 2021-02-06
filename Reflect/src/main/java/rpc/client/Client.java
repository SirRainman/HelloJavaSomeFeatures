package rpc.client;

import lombok.extern.slf4j.Slf4j;
import rpc.common.UserServiceInterface;

@Slf4j
public class Client {
    public static void main(String[] args) {
        UserServiceInterface userServiceInterface = (UserServiceInterface) Stub.getStub(UserServiceInterface.class);
        log.info("{}", userServiceInterface.getUserById("123"));
    }
}
