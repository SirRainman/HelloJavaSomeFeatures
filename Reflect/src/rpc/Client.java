package rpc;

public class Client {
    public static void main(String[] args) {

        // 使用动态代理的方式获取一个接口的对象
        // ---实际上是通过运用动态代理+反射等技术实时的创建了一个实现该接口的类的对象
        ProductServiceInterface productServiceInterface = (ProductServiceInterface) Stub.getStub(ProductServiceInterface.class);
        System.out.println(productServiceInterface.findProductById("321"));
        System.out.println(productServiceInterface.findProductById("123"));
    }
}

