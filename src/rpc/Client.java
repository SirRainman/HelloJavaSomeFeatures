package rpc;

public class Client {
    public static void main(String[] args) {

        // ʹ�ö�̬����ķ�ʽ��ȡһ���ӿڵĶ���
        // ---ʵ������ͨ�����ö�̬����+����ȼ���ʵʱ�Ĵ�����һ��ʵ�ָýӿڵ���Ķ���
        ProductServiceInterface productServiceInterface = (ProductServiceInterface) Stub.getStub(ProductServiceInterface.class);
        System.out.println(productServiceInterface.findProductById("321"));
        System.out.println(productServiceInterface.findProductById("123"));
    }
}
