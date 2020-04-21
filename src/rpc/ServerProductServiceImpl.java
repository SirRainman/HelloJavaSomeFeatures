package rpc;

public class ServerProductServiceImpl {
    public Product findProductById(String id) {
        return new Product(id, "zhangsan");
    }
}
