package createPattern.Singleton;

/**
 * @program: DesignPattern
 * @description: 单例模式
 *
 * 问题1 为什么要判断两次null？
 * 如果N个线程同时并发来获取实例，其他线程都阻塞在第一次检查，等待第一个线程初始化实例完成后。
 * 后面的N - 1线程会串行执行synchronized代码块，会再"new" N - 1 个实例出来，无法达到单例的目的。
 *
 * 问题2 为什么要volatile？
 * new对象其实是分为三步执行：
 * 1. 分配内存空间
 * 2. 初始化对象
 * 3. 将对象指向分配的内存地址
 * 指令重排后，初始化对象可能放在最后一步。假设A线程还未初始化对象，B线程调用 getInstance() 后发现 不为空，就会返回未被初始化的对象。
 * 使用 volatile 可以禁止 JVM 的指令重排，保证在多线程环境下也能正常运行。
 * 用途：spring bean 作用域为singleton时采用单例模式
 *
 *
 * @author: Rain
 * @create: 2021-05-19 12:17
 **/
public class Singleton {
    public static volatile Singleton singleton;

    //构造函数私有，禁止外部实例化
    private Singleton() {};

    public static Singleton getInstance() {
        if (singleton == null) { // 在synchronized保护范围外
            // 首次访问会同步，而之后的使用没有 synchronized
            synchronized (singleton) {
                if (singleton == null) {
                    singleton = new Singleton();
                }
            }
        }

        return singleton;
    }
}
