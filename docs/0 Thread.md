# 背景

## 进程、线程

### 1 什么是进程？

* 程序：一段静态的代码，一组指令的有序集合，它本身没有任何运行的含义。
* 进程：是程序的一次执行过程，是系统运行程序时进行**资源分配和调度的基本单位**，因此进程是动态的。
    * **进程对应着从代码加载，执行至执行完毕的一个完整的过程**，是一个动态的实体，它有自己的生命周期。它因创建而产生，因调度而运行，因等待资源或事件而被处于等待状态，因完成任务而被撤消。
        * 通过进程控制块(PCB)唯一的标识某个进程。
    * 进程占据着相应的资源（例如包括cpu的使用 ，轮转时间以及一些其它设备的权限）。
* 例：在 Java 中，当启动 main 函数时其实就是启动了一个 JVM 的进程，而 main 函数所在的线程就是这个进程中的一个线程，也称主线程。

### 2 什么是线程？

* 线程与进程相似，但**线程是一个比进程更小的执行单位**。一个进程在其执行的过程中可以产生多个线程。
    * 同进程下的多个线程共享进程的**堆**和**方法区**等内存空间资源，
    * 但每个线程有自己的**程序计数器**、**虚拟机栈**和**本地方法栈**
    * 所以系统在产生一个线程，或是在各个线程之间作切换工作时，负担要比进程小得多，也正因为如此，线程也被称为轻量级进程。

通过 JMX 查看 的 Java 程序 线程的种类

```java
public void threadTypes() {
    // 获取 Java 线程管理 MXBean
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    // 不需要获取同步的 monitor 和 synchronizer 信息，仅获取线程和线程堆栈信息
    ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
    // 遍历线程信息，仅打印线程 ID 和线程名称信息
    for (ThreadInfo threadInfo : threadInfos) {
        log.info("[{}] {}", threadInfo.getThreadId(), threadInfo.getThreadName());
    }
}

/*
[6] Monitor Ctrl-Break 监听线程转储或“线程堆栈跟踪”的线程
[5] Attach Listener 获取当前运行环境的信息 内存 栈 系统信息，添加事件的线程
[4] Signal Dispatcher 分发处理给 JVM 信号的线程
[3] Finalizer 在垃圾收集前，调用对象 finalize 方法的线程
[2] Reference Handler 用于处理引用对象本身（软引用、弱引用、虚引用）的垃圾回收的线程
[1] main 主线程
*/
```

> 上面线程的种类那些都是干嘛的？

## 进程、线程的联系、区别？

**根本区别**：

* 进程是操作系统**资源分配和调度**的**独立单位**，使程序之间可以并发的执行，提高资源的利用率和系统的吞吐率
* 而线程是操作系统**任务调度和执行**的**基本单位**

**资源开销**：

* **每个进程都有独立的代码和数据空间（程序上下文）**，进程的创建、销毁、切换产生大量的时间和空间的开销，程序之间的切换会有较大的开销；
* 线程可以看做轻量级的进程，同一类线程共享代码和数据空间，每个线程都有自己**独立的运行栈、寄存器和程序计数器（PC）**，线程之间切换的开销小。

**包含关系**：

* 如果一个进程内有多个线程，则执行过程不是一条线的，而是多条线（线程）共同完成的；

**影响关系**：

* 多进程为什么比多线程健壮？
    * **一个进程崩溃后，在保护模式下不会对其他进程产生影响**
    * **一个线程崩溃整个进程都死掉。**

**执行过程**：

* 每个独立的进程有程序运行的入口、顺序执行序列和程序出口。
* 但是线程不能独立执行，必须依存在应用程序中，由应用程序提供多个线程执行控制，两者均可并发执行

注：以下*从 JVM 角度说进程和线程之间的关系*

![img](http://haoimg.hifool.cn/img/68747470733a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f323031392d332f4a564de8bf90e8a18ce697b6e695b0e68daee58cbae59f9f2e706e67.png)

关系：

* 多个线程共享进程的**堆**和**方法区 (JDK1.8 之后的元空间)资源**，但是每个线程有自己的**程序计数器**、**虚拟机栈** 和 **本地方法栈**。

总结： 

* 线程和进程最大的不同在于基本上各**进程是独立**的，而各线程则不一定，因为同一进程中的线程极有可能会相互影响。
* **线程执行开销小**，但不利于资源的管理和保护；而进程正相反。

> 为什么**程序计数器**、**虚拟机栈**和**本地方法栈**是线程私有的呢？
>
> 为什么堆和方法区是线程共享的呢？

### 1 为什么**程序计数器**是线程私有的？

* **为了线程切换后能恢复到正确的执行位置**。
* **程序计数器**：
    1. **字节码解释器通过改变程序计数器来依次读取指令**，从而实现代码的流程控制，如：顺序执行、选择、循环、异常处理。
    2. 在多线程的情况下，**程序计数器记录当前线程执行的位置**，从而当线程被切换回来的时候能够知道该线程上次运行到哪儿了。
    3. 需要注意的是，如果执行的是 native 方法，那么程序计数器记录的是 undefined 地址，只有执行的是 Java 代码时程序计数器记录的才是下一条指令的地址。

### 2 为什么**虚拟机栈 和 本地方法栈** 是私有的？

- 主要是为了**保证线程中的局部变量不被别的线程访问到**
- **虚拟机栈：** 
    - 每个 **Java 方法**在执行的同时会创建一个**栈帧用于存储局部变量表、操作数栈、常量池引用等信息**。
    - 从方法调用直至执行完成的过程，就对应着一个栈帧在 Java 虚拟机栈中入栈和出栈的过程。
- **本地方法栈：** 
    - 和虚拟机栈所发挥的作用非常相似，
    - 区别是： 虚拟机栈为虚拟机执行 Java 方法 （也就是字节码）服务，而本地方法栈则为虚拟机使用到的 Native 方法服务。

### 3 为什么 **堆和方法区** 是共享的？

* 方便多个线程**共享使用公共资源**，否则线程不就和进程一样了嘛。
* 堆是进程中最大的一块内存，**主要用于存放新创建的对象** (几乎所有对象都在这里分配内存)
* 方法区**主要用于存放已被加载的类信息、常量、静态变量、即时编译器编译后的代码等数据**。

## 并发 和 并行

- **并发：** 同一时间段，多个任务都在执行 (单位时间内不一定同时执行)；
- **并行：** 单位时间内，多个任务同时执行。

## 多线程的优点

总体上来说：

- **从计算机底层来说：** 
    - 线程可以比作是轻量级的进程，是程序执行的最小单位,线程间的**切换和调度的成本远远小于进程**。
    - **多核** CPU 时代意味着多个线程可以同时运行，这**减少了线程上下文切换的开销**。

再深入到计算机底层来探讨：

- **单核时代：** **在单核时代多线程主要是为了提高 CPU 和 IO 设备的综合利用率**。
    - 举个例子：当只有一个线程的时候会导致 CPU 计算时，IO 设备空闲；进行 IO 操作时，CPU 空闲。我们可以简单地说这两者的利用率目前都是 50%左右。但是当有两个线程的时候就不一样了，当一个线程执行 CPU 计算时，另外一个线程可以进行 IO 操作，这样两个的利用率就可以在理想情况下达到 100%了。
- **多核时代:** **多核时代多线程主要是为了提高 CPU 利用率**。
    - 举个例子：假如我们要计算一个复杂的任务，我们只用一个线程的话，CPU 只会一个 CPU 核心被利用到，而创建多个线程就可以让多个 CPU 核心被利用到，这样就提高了 CPU 的利用率。

## 使用多线程带来的问题

1. 内存泄漏
2. 死锁
3. 线程不安全
4. 。。。

> 还有其他哪些问题？

## 什么是上下文切换?

CPU 核心在任意时刻只能被一个线程使用，为了让这些线程都能得到有效执行，CPU 采取的策略是为每个线程分配时间片并轮转的形式。

当一个线程的时间片用完的时候就会先保存自己的状态，重新处于就绪状态让出CPU给其他线程使用，这个过程就属于一次上下文切换。

上下文切换通常是**计算密集型**的。

* 上下文切换需要相当可观的处理器时间，事实上，可能是操作系统中时间消耗最大的操作。
* Linux 相比与其他操作系统（包括其他类 Unix 系统）有很多的优点，其中有一项就是，其上下文切换和模式切换的时间消耗非常少。

## 什么是死锁？

![线程死锁示意图 ](http://haoimg.hifool.cn/img/68747470733a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f323031392d342f323031392d34254536254144254242254539253934253831312e706e67.png)

死锁：多个线程同时被阻塞，它们中的一个或者全部都在等待某个资源被释放，最终导致线程无限期被阻塞。

```java
public class DeadLock {
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();

    @Test
    public void createDeadLock() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                log.info("{} get lock1", Thread.currentThread());
                Sleeper.sleep(1);
                log.info("{} waiting for lock2", Thread.currentThread());
                synchronized (lock2) {
                    log.info("{} get lock2", Thread.currentThread());
                }
            }
        }, "t1");

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                log.info("{} get lock2", Thread.currentThread());
                Sleeper.sleep(1);
                log.info("{} waiting for lock1", Thread.currentThread());
                synchronized (lock1) {
                    log.info("{} get lock1", Thread.currentThread());
                }
            }
        }, "t2");

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}

```

### 1 产生死锁的条件

同时满足以下四种条件则会产生死锁：

1. **互斥条件**：该资源任意一个时刻只由一个线程占用。
2. **请求与保持条件**：一个进程因请求资源而阻塞时，对已获得的资源保持不放。
3. **不剥夺条件**：线程已获得的资源在未使用完之前不能被其他线程强行剥夺，只有自己使用完毕后才释放资源。
4. **循环等待条件**：若干进程之间形成一种头尾相接的循环等待资源关系。

### 2 消除死锁

为了避免死锁，只要破坏产生死锁的四个条件中的其中一个就可以了：

1. **破坏互斥条件** ：
    1. 这个条件不能被破坏，因为锁是使临界资源互斥访问的。
2. **破坏请求与保持条件** ：
    1. 要一次性申请所有的资源。
3. **破坏不剥夺条件** ：
    1. 占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源。
4. **破坏循环等待条件** ：
    1. 靠按序申请资源来预防。按某一顺序申请资源，释放资源则反序释放。破坏循环等待条件。



## 线程安全类

线程安全的是指，多个线程**调用它们同一个实例的某个方法时**，是线程安全的。

常见线程安全类

* String（不可变类线程安全，String 中 replace()等方法其实是创建一个新的对象返回的结果）
* Integer（不可变类线程安全）
* StringBuffer
* Random
* Vector
* Hashtable
* java.util.concurrent 包下的类

注意：

* 这些类中的每个方法是原子的，
* 但是**多个方法的组合不一定是原子**的



---

# 一、创建线程

## 1 创建线程的三种方法

1. **继承Thread类**
2. **实现Runnable接口**
3. **实现Callable接口**

三种方式的比较：**实现接口的方式更好**

* **Java不支持多重继承，继承了一个类并不能继承其他的类，但可以实现多个接口**
* **类可能只要求可执行就行，继承整个 Thread 类开销过大**。

```java
public static void main(String[] args) throws ExecutionException, InterruptedException {
    // 1 继承Thread类
    Thread t1 = new Thread("t1") {
        @Override
        public void run() {
            log.info("t1 running");
        }
    };
    // MyThread mt = new MyThread(); // MyThread类 继承Thread类
    t1.setName("t1");
    t1.start();

    // 2 实现Runnable接口：推荐
    Thread t2 = new Thread(() -> {
        log.info("t2 running");
    }, "t2");
    t2.setName("t2");
    t2.start();
    // 注：2 等同于上一种方式， MyRunnable类 实现了Runnable接口
    // MyRunnable instance = new MyRunnable();
    // Thread t2 = new Thread(instance, "t2");

    // 注：2 实现Runnable接口
    Runnable runnable = () -> {
        log.info("t3 running");
    };
    Thread t3 = new Thread(runnable, "t3");
    t3.start();


    // 3 实现Callable接口：FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况
    FutureTask task4 = new FutureTask<>(() -> {
        log.info("task4");
        return 100;
    });
    Thread t4 = new Thread(task4, "t4");

    // 等同于上面的方式，MyCallable类 实现了Callable接口
    // MyCallable mc = new MyCallable();
    // FutureTask<Integer> ft = new FutureTask<>(mc);
    // Thread task4 = new Thread(ft);

    t4.start();
    // 主线程阻塞，同步等待 task 执行完毕的结果
    log.info("get result from t4 = {}", task4.get());

    log.info("do other things ...");
}
```

## 2 run()与start()区别

- start()：**线程对象初始化完成进入NEW状态之后，调用start()方法会使用native的方式创建一个新线程，并执行run()方法**。
    1. start() 启动一个线程时：当前线程（parent线程）同步告知JVM，只要线程规划器空闲，应立即启动 调用start()方法的线程（即子进程进入到了就绪RUNNABLE状态）。
    2. 然后子进程通过此Thread类调用方法run()完成运行的操作， 这里方法run()称为线程体， 它包含了新开的线程要执行的的内容，
    3. run方法运行结束， 此线程终止， CPU再运行其它线程，start()不能被重复调用。
    4. 注：**只有处于NEW状态的线程才可以调用 start() 方法**，否则会抛出 IllegalThreadStateException 。也就是说，**一个线程不能调用两次start方法**，此时线程处于终止或者其他非NEW状态，不可以再次启动。
- **run()：子类需要覆盖Thread类的run方法。**
    - run()就和普通的成员方法一样，可以被重复调用。直接调用run方法并不会创建新的线程，只是作为一个普通的方法调用，在当前线程串行的执行run()方法中的代码。



---

# 二、线程 - 基础

## sleep()

Thread.sleep(millisec) 方法会休眠当前正在执行的线程(放弃CPU)。

需要注意的亮点：

1. **sleep() 可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。**
    2. sleep() 等会抛出InterruptedException的方法，**抛出中断异常前会清除线程中断标记位！**然后再抛出异常。
2. **在线程休眠阶段，仍然会持有锁，并不会释放锁。**

> sleep() **抛出中断异常后会清除线程中断标记位！**
>
> 什么是中断标记位？
>
> * 中断标识位：表示一个运行中的线程 是否被其他线程进行了中断操作，其他线程通过调用该线程的interrupt() 方法对其进行中断操作。

## yield()

```java
public static void main(String[] args) {
    Runnable task1 = () -> {
        int count = 0;
        while (true) {
            System.out.println("--->1= " + count++);
        }
    };

    Runnable task2 = () -> {
        int count = 0;
        while (true) {
            // Thread.yield();
            System.out.println("                   2= " + count++);
        }
    };

    Thread t1 = new Thread(task1, "t1");
    Thread t2 = new Thread(task2, "t2");

    t1.setPriority(Thread.MAX_PRIORITY);
    t2.setPriority(Thread.MIN_PRIORITY);
    t1.start();
    t2.start();
}
```

对静态方法 Thread.yield() 的调用声明了：

* **当前线程已经完成了生命周期中最重要的部分，使当前线程由“运行状态”进入到“就绪状态”**，可以切换给其它线程来执行。

* yield()方法主要是为了保障线程间调度的连续性，**防止某个线程一直长时间占用cpu资源**。
* **一般不推荐使用**，它主要用于debug和测试程序，用来减少bug以及对于并发程序结构的设计。

需要注意的几点：

1. **该方法只是对线程调度器的一个建议**，而且也只是建议具有相同优先级的其它线程可以运行。如果没有正在等待的线程，或者所有正在等待的线程的优先级都比较低，那么该线程会继续运行。
2. **yield方法不保证当前的线程会暂停或者停止**，**但是可以保证当前线程在调用yield方法时会放弃CPU。**
3. **执行了yield方法的线程什么时候会继续运行由线程调度器来决定**，不同的厂商可能有不同的行为。

## join()

join() 的作用：让“主线程”等待“子线程”结束之后才能继续运行。

join原理：**是调用者轮询检查线程 alive 状态**

* join 体现的是【保护性暂停】模式

```java
// 等价于下面的代码
synchronized (t1) {
    // 调用者线程进入 t1 的 waitSet 等待, 直到 t1 运行结束
    while (t1.isAlive()) {
    	t1.wait(0);
    }
}
```



## wait() / notify( ) / notifyAll( )

![image-20210223112427312](http://haoimg.hifool.cn/img/image-20210223112427312.png)

等待/通知 机制 API介绍：

* object.wait() **让进入 object 监视器的线程到 waitSet 等待，从而该线程进入到Waiting状态**。
* object.notify() 在 object 上**正在 waitSet 等待的线程中随机挑一个唤醒**
* object.notifyAll() 让 object 上正在 waitSet 等待的线程全部唤醒

举例：

1. Owner：占用当前锁对象的线程
2. WaitSet：当前Owner指向的线程 线程2 发现条件不满足，调用 wait 方法，即可进入 WaitSet 变为 WAITING 状态
    1. wait 方法会释放对象的锁，进入 WaitSet 等待区，从而让其他线程就机会获取对象的锁。无限制等待，直到notify 为止
    2. WAITING 线程会在 Owner 线程调用 notify 或 notifyAll 时唤醒，但唤醒后并不意味者立刻获得锁，仍需进入EntryList 重新竞争
3. EntryList：处于 BLOCKED 状态的线程会在 Owner 线程释放锁时唤醒
4. BLOCKED 和 WAITING 的线程都处于阻塞状态，不占用 CPU 时间片



wait() / notify( ) / notifyAll( ) 的关系：

* 多个线程通过对象O完成交互，而对象上的wait()及notify()方法就像是开关信号一样，完成等待方和通知方的信息交互工作

使用需要注意的地方：

* **想要执行wait方法，必须先获得锁**
* 使用 wait() **挂起期间，线程会释放锁**。
    * 如果没有释放锁，那么其它线程就无法进入对象的同步方法或者同步控制块中，那么就无法执行 notify() 或者 notifyAll() 来唤醒挂起的线程，造成死锁。



wait & notify() 正确的使用方法：

```java
synchronized(lock) {
    while(条件不成立) {
    	lock.wait();
    }
	// 干活
}

//另一个线程
synchronized(lock) {
	lock.notifyAll();
}
```

以上做法的优点：

1. **不用sleep，是因为sleep在TIME_WAITING状态中时，仍会占用锁**
2. 使用notifyAll，是因为当有多个线程都在WaitSet中时， notify只能随机的在WaitSet中唤醒一个线程，可能唤醒的不是正确的线程
3. 用 notifyAll 仅解决某个线程的唤醒问题，但使用 if + wait 判断仅有一次机会，一旦条件不成立，就没有重新判断的机会了。
    1. 如果当前的线程被错误的唤醒，且条件不满足，则会再次的进入到WaitSet中。



示例：轮流打印1 2 3 4

```java
@Slf4j
public class WaitNotify {
    static class PrintNumberTask implements Runnable {
        private byte[] lock;

        // 类共享变量
        private static int x = 1;

        public PrintNumberTask(byte[] lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            while(x < 10) {
                synchronized (lock) {
                    log.info("{}", x++);
                    lock.notify();
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        byte[] lock = new byte[0];
        new Thread(new PrintNumberTask(lock), "t1").start();
        new Thread(new PrintNumberTask(lock), "t2").start();
    }
}
```



## park() / unpark()

**LockSupport 类中的方法，用来暂停当前线程和恢复线程**

### 1 与 wait & notify 相比:

* **wait/notify/notifyAll 必须配合 Object Monitor 一起使用**，而 park，unpark 不必
* **park & unpark 是以线程为单位来【阻塞】和【唤醒】线程**，
    * notify 只能随机唤醒一个等待线程，
    * notifyAll 是唤醒所有等待线程，就不那么【精确】
* **park & unpark 可以先 unpark，而 wait & notify 不能先 notify**

```java
@Slf4j(topic = "c.Park")
public class Park {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                log.info("park");
                LockSupport.park();
                log.info("interrupted = {}", Thread.interrupted());
//                log.info("isInterrupted = {}", Thread.currentThread().isInterrupted());
            }
        }, "t");
        t.start();

        Sleeper.sleep(1);
        t.interrupt();


        for (int i = 0; i < 5; i++) {
            log.info("unpark");
            LockSupport.unpark(t);
            Sleeper.sleep(1);
        }
    }
}
```

### 2 park() / unpack() 原理

每个线程都有一个自己的Parker对象，Parker对象由三部分组成

* _counter
* _cond
* _mutex

1. 运行时调用park()
2. 检查 _counter ，本情况为 0，这时，获得 _mutex 互斥锁
3. 线程进入 _cond 条件变量阻塞
4. 设置 _counter = 0

![image-20210225154823767](http://haoimg.hifool.cn/img/image-20210225154823767.png)

2. 线程阻塞时调用unpark()
    1. 调用 Unsafe.unpark(Thread_0) 方法，设置 _counter 为 1
    2. 唤醒 _cond 条件变量中的 Thread_0
    3. Thread_0 恢复运行
    4. 设置 _counter 为 0

![image-20210225155056643](http://haoimg.hifool.cn/img/image-20210225155056643.png)



3. 线程运行时调用unpack() 再调用park()
    1. 调用 Unsafe.unpark(Thread_0) 方法，设置 _counter 为 1
    2. 线程正常运行，不需要阻塞
    3. ...
    4. 当前线程调用 Unsafe.park() 方法
    5. 检查 _counter ，本情况为 1，这时线程无需阻塞，继续运行
    6. 设置 _counter 为 0

![image-20210225155312983](http://haoimg.hifool.cn/img/image-20210225155312983.png)



---

## sleep()、wait()、yield() 的区别

**作用对象上的不同：**

- sleep和yeild是Thread的静态方法，**作用对象是当前线程。**
- wait是Object的方法，**作用对象是实例。**

**锁资源释放的不同：**

- sleep方法会让当前线程放弃CPU，休眠一段时间，**在此期间线程仍然会持有锁**。
- **wait()执行前，当前线程一定要获取到锁资源， 挂起期间，线程会释放锁**
- **yield()不会释放锁**，只是通知线程调度器自己可以让出cpu时间片，而且也只是建议而已。
- **join() 不会释放锁**。当前线程(parent线程) 等待调用join方法的线程结束，再继续执行。join()有资格释放资源其实是通过调用wait()来实现的

> 哪种方法会释放锁？sleep() / join() / yield / wait
>
> 所谓的释放锁资源实际是通知对象内置的monitor对象进行释放，而只有wait() 方法的持有对象有内置的monitor对象才能实现任何对象的锁资源都可以释放。
>
> 又因为所有类都继承自Object，所以wait() 就成了Object方法，也就是通过wait()来通知对象内置的monitor对象释放，而且事实上因为这涉及对硬件底层的操作，所以wait()方法是native方法，底层是用C写的。  
>
> 其他都是Thread所有，所以其他3个是没有资格释放资源的 

用法不同：

* `wait()` 通常被用于线程间交互/通信，`sleep() `通常被用于暂停执行。

---

## Daemon 守护线程

守护线程是程序运行时在后台提供服务的线程，不属于程序中不可或缺的部分。

**当所有非守护线程结束时，程序也就终止，同时会杀死所有守护线程。**

```java
public class Daemon {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            log.info("t begin");
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
            log.info("t end, and isInterrupted={}", Thread.currentThread().isInterrupted());
        }, "t");
        /**
         * 守护线程是：程序运行时在后台提供服务的线程，不属于程序中不可或缺的部分。
         * 默认情况下，Java进程需要等待所有线程都运行结束，才会结束。
         * 守护线程：只要其它非守护线程运行结束了，即使守护线程的代码没有执行完，也会被强制结束。
         * */
        t.setDaemon(true); // 如果不设置为守护进程，main进程作为非守护进程，会一直等待t执行结束
        t.start();

        Sleeper.sleep(1);
        log.info("main end");
    }
}
```

---

## interrupt() 线程中断

什么是中断？

* 中断标记位：表示一个运行中的线程 是否被其他线程进行了中断操作，其他线程通过调用该线程的interrupt() 方法对其进行中断操作。
    * 线程通过检查自身是否被中断来响应，如果自己被中断掉，可以进行相应的处理工作。
* **中断操作并不会真正的中断一个正在运行的线程，而只是发出中断请求，然后由线程在下一个合适的时刻中断自己（合适的时刻称为取消点）**。
    * 有些方法（wait / sleep / join)会严格的处理这些中断请求，当他们收到中断请求或者在开始执行时，发现已被设置好的中断状态时，将抛出一个异常。

**与中断有关的三个方法**：

```java
public void Thread.interrupt() // 中断线程，Thread实例方法。通知目标线程中断，也就是设置中断标志位为true。中断标志位表示当前线程已经被中断了。
public boolean Thread.isInterrupted() // Thread实例方法。判断当前线程是否有被中断（通过检查中断标志位）。
public static boolean Thread.interrupted() // Thread类静态方法。判断当前线程的中断状态，但同时会清除当前线程的中断标志位状态。
```



**线程中断的异常处理**：

* 与线程相关的一些方法可能会抛出 InterruptedException，因为**异常不能跨线程传播回 main() 中，因此必须在本地进行处理**。
    1. 线程中抛出的其它异常也同样需要在本地进行处理。 
    2. sleep() 等会抛出InterruptedException的方法，**抛出中断异常前会清除线程中断标记位！**然后再抛出异常。

```java
@Slf4j(topic = "c.Interrupt")
public class Interrupt {

    @Test
    public void basicUse() {
        Thread t = new Thread(() -> {
            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    log.info("t is interrupted, exit now");
                    break;
                }
            }
        }, "t");
        t.start();

        Sleeper.sleep(1);
        log.info("begin interrupt()");
        t.interrupt();
        log.info("after interrupt()");
    }

    @Test
    public void InterruptWhileSleeping() throws InterruptedException {
        Thread t = new Thread(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.info("error: {}", e.getMessage());
            }
        }, "t1");
        t.start();

        Sleeper.sleep(1);
        // 不能在线程睡眠的时候打断，否则会抛出 InterruptedException 异常
        t.interrupt();
        log.info("t's isInterrupted={}", t.isInterrupted());
    }
}
```

### 1 两阶段终止

终止线程不推荐的做法（可能会产生很多问题）：

- **使用线程对象的 stop() 方法停止线程**
    - **stop 方法会真正杀死线程，如果这时线程锁住了共享资源，那么当它被杀死后就再也没有机会释放锁**
    - 子线程当前执行的任务可能必须要原子的执行，即其要么成功执行，要么就不执行，直接杀死不满足目标
    - 当前任务队列中还有未执行完的任务，直接终止线程可能导致这些任务被丢弃；
    - 当前线程占用了某些外部资源，比如打开了某个文件，或者使用了某个Socket对象，这些都是无法被垃圾回收的对象，必须由调用方进行清理。
- 使用 System.exit(int) 方法停止线程
    - 目的仅是停止一个线程，但这种做法会让整个程序都停止



**在一个线程 T1 中如何“优雅”终止线程 T2？**

* **这里的优雅指的是给 T2 一个料理后事的机会**。

![image-20210223134745884](http://haoimg.hifool.cn/img/image-20210223134745884.png)



volatile 优化后的版本：

```java
class TwoPhaseTermination2 {
    // 监控线程
    private Thread monitorThread;
    // 停止标记, 停止标记用 volatile 是为了保证该变量在多个线程之间的可见性
    private volatile boolean stop = false;
    // 判断是否执行过start方法
    private volatile boolean starting = false;

    public void start() {
        System.out.println();
        synchronized (this) { // 防止多个线程同时读到start = false，还是会重复创建monitor线程，加锁进行保护
            if (this.starting) { // balking 模式，防止多次调用start方法多次创建monitor线程
                return;
            }
            starting = true;
        }

        this.monitorThread = new Thread(() -> {
            while(true) {
                if (this.stop) {
                    log.info("end:do somethings");
                    break;
                }
                try {
                    Thread.sleep(1000);
                    log.info("monitor...");
                } catch (InterruptedException e) {
                    log.info("---interrupt while sleeping---");
                    // e.printStackTrace();
                }
            }
        }, "monitorThread");
        this.monitorThread.start();
    }

    public void stop() {
        log.info("stop two phase termination");
        this.stop = true;
        this.monitorThread.interrupt();
    }

}
```

---

## 线程的生命周期

![线程状态图](http://haoimg.hifool.cn/img/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BhbmdlMTk5MQ==,size_16,color_FFFFFF,t_70.jpeg)

Java 线程的所有状态都在 Thread 中的 State 枚举中定义

```java
public enum State { 
    NEW, 
    RUNNABLE, 
    BLOCKED, 
    WAITING, 
    TIMED_WAITING, 
    TERMINATED; 
}
```

| 状态名称                        | 说明                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| **NEW**(初始化状态）            | 初始状态，线程刚刚创建，还没调用start方法                    |
| **RUNNABLE**（运行/就绪状态）   | Java线程将操作系统中的就绪状态 和 运行状态 统称为Runnable状态。表示线程正在执行 或 正在等待某种资源（CPU、IO等） |
| **BLOCKED**（阻塞状态）         | 表示线程处于阻塞状态，正在等待锁的释放                       |
| **WAITING**(无时限等待状态)     | 表示线程处于无时限等待状态，正在等待其他线程的特定动作（通知 或 中断）以便唤醒该线程。 |
| **TIMED_WAITING**（有时限等待） | 表示线程处于有时限等待状态，等待其他线程唤醒 或者 超时自己自行唤醒返回（这是区别于waiting的地方） |
| **TERMINATED**（终止状态）      | 线程已经执行完毕                                             |

需要注意的地方：

* RUNNABLE 状态 表示操作系统中的运行和就绪这两种状态
    * RUNNABLE 是jvm虚拟机的概念，对于jvm来讲，只要是可以运行（运行/就绪）的都归类到RUNNABLE中
* BLOCKED 状态表示线程阻塞 在进入Synchronized修饰的方法或代码块时 的等待锁的状态
    * **但阻塞状态在java.concurrent包中Lock接口的线程状态却是等待状态**，因为java.concurrent包中Lock接口对于阻塞的实现均使用了LockSupport类中的相关方法。

> 但阻塞状态在java.concurrent包中Lock接口的线程状态却是等待状态，因为java.concurrent包中Lock接口对于阻塞的实现均使用了LockSupport类中的相关方法。
>
> 这一句话《线程并发艺术》P90这一句话是什么意思？

### 1 NEW 和 RUNNABLE 的状态转换

当调用 t.start() 方法时，由 NEW --> RUNNABLE

### 2 RUNNABLE 与 BLOCKED 的状态转换

当线程 synchronized(obj) 获取对象锁时竞争失败，RUNNABLE --> BLOCKED：

- synchronized 修饰的方法、代码块同一时刻只允许一个线程执行，其他线程只能等待，这种情况下，等待的线程就会从 RUNNABLE 转换到 BLOCKED 状态。

当持 obj 锁线程的同步代码块执行完毕，会唤醒该对象上所有 BLOCKED 的线程重新竞争：

* 如果其中 t 线程竞争成功，从 BLOCKED --> RUNNABLE ，其它失败的线程仍然 BLOCKED

注意！！！

* JVM 层面并不关心操作系统调度相关的状态，因为在 JVM 看来，等待 CPU 使用权（操作系统层面此时处于可执行状态）与等待 I/O（操作系统层面此时处于休眠状态）没有区别，都是在等待某个资源，所以都归入了 RUNNABLE 状态。

> 什么是隐式锁？？

### 3 RUNNABLE 与 WAITING 的状态转换

三种场景会触发这种转换。

1. **获得对象synchronized 隐式锁**的线程
    1. 调用无参数的 Object.wait() 方法，线程从 RUNNABLE --> WAITING。
    2. 调用 obj.notify() ， obj.notifyAll() ， t.interrupt() 时
        1. 竞争锁成功，t 线程从 WAITING --> RUNNABLE
        2. 竞争锁失败，t 线程从 WAITING --> BLOCKED
2. 当前线程**调用 thread.join()** 方法时，当前线程从 RUNNABLE --> WAITING
    1. 注意是当前线程在thread 线程对象的监视器上等待
    2. thread 线程运行结束，或调用了当前线程的 interrupt() 时，当前线程从 WAITING --> RUNNABLE
3. 当前线程**调用 LockSupport.park()** 方法会让当前线程从 RUNNABLE --> WAITING。
    1. 调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，会让目标线程从 WAITING --> RUNNABLE

### 4  RUNNABLE 与 TIMED_WAITING 的状态转换

有五种场景会触发这种转换。
1. 当前线程调用带超时参数 Thread.sleep(long n) ，当前线程从 RUNNABLE --> TIMED_WAITING
    1. 当前线程等待时间超过了 n 毫秒，当前线程从 TIMED_WAITING --> RUNNABLE
2. t 线程用 synchronized(obj) 获取了对象锁后
    1. 调用带超时参数的 obj.wait(long timeout)  方法时，t 线程从 RUNNABLE --> TIMED_WAITING
    2. t 线程等待时间超过了 n 毫秒，或调用 obj.notify() ， obj.notifyAll() ， t.interrupt() 时
        1. 竞争锁成功，t 线程从 TIMED_WAITING --> RUNNABLE
        2. 竞争锁失败，t 线程从 TIMED_WAITING --> BLOCKED
3. 当前线程调用带超时参数的 t.join(long millis) 方法时，当前线程从 RUNNABLE --> TIMED_WAITING
    1. 注意是当前线程在t 线程对象的监视器上等待
    2. 当前线程等待时间超过了 n 毫秒，或t 线程运行结束，或调用了当前线程的 interrupt() 时，当前线程从TIMED_WAITING --> RUNNABLE
4. 调用带超时参数的 LockSupport.parkNanos(Object blocker, long deadline) 方法，当前线程从 RUNNABLE --> TIMED_WAITING；
    1. 调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，或是等待超时，会让目标线程从 TIMED_WAITING--> RUNNABLE
5. 调用带超时参数的 LockSupport.parkUntil(long deadline) 方法，当前线程从 RUNNABLE --> TIMED_WAITING。
    1. 调用 LockSupport.unpark(目标线程) 或调用了线程 的 interrupt() ，或是等待超时，会让目标线程从 TIMED_WAITING--> RUNNABLE



TIMED_WAITING 和 WAITING 状态的区别，仅仅是触发条件多了超时参数。

```java
public class State {
    @Test
    public void lifecycleChange() {
        Thread t = new Thread(() -> {
            log.info("t1 enter sleeping ...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "t1");
        log.info("t1 state: {}", t.getState());
        t.start();
        log.info("t1 state: {}", t.getState());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("t1 state: {}", t.getState());

        log.info("interrupt t1 ...");
        // 在sleep期间打断会报错
        t.interrupt();
        log.info("t1 state: {}", t.getState());
        log.info("t1's isInterrupted: {}", t.isInterrupted()); // 在sleep期间打断会报错，抛出异常之后会重置中断标识位
    }

    @Test
    public void differentState() {
        Thread t1 = new Thread(() -> {
            log.info("t1 running...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            while (true) { // runnable

            }
        }, "t2");
        t2.start();

        Thread t3 = new Thread(() -> {
            log.info("t3 running...");
        }, "t3");
        t3.start();

        Thread t4 = new Thread(() -> {
            synchronized (Thread.State.class) {
                try {
                    Thread.sleep(1000000); // timed_waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t4");
        t4.start();

        Thread t5 = new Thread(() -> {
            try {
                t2.join(); // waiting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");
        t5.start();

        Thread t6 = new Thread(() -> {
            synchronized (State.class) { // blocked
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t6");
        t6.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("t1 state {}", t1.getState());
        log.info("t2 state {}", t2.getState());
        log.info("t3 state {}", t3.getState());
        log.info("t4 state {}", t4.getState());
        log.info("t5 state {}", t5.getState());
        log.info("t6 state {}", t6.getState());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```



----



# 三、CAS指令

CPU 为了解决并发问题，提供了 CAS 指令（CAS，全称是 **Compare And Swap**，即“比较并交换”）。

* CAS 的底层是 lock cmpxchg 指令（X86 架构），在单核 CPU 和多核 CPU 下都能够保证【比较-交换】的原子性。
* 在多核状态下，某个核执行到带 lock 的指令时，CPU 会让总线锁住，当这个核把此指令执行完毕，再开启总线。这个过程中不会被线程的调度机制所打断，保证了多个线程对内存操作的准确性，是原子的。



CAS 指令包含 3 个参数：

1. 共享变量的内存地址 A、
2. 用于比较的值 B 
3. 共享变量的新值 C；

- 只有当内存中 共享变量的地址 A 处的值等于 B 时，才能将内存中地址 A 处的值更新为新值 C。
- **作为一条 CPU 指令，CAS 指令本身是能够保证原子性的。**



**CAS 必须借助volatile才能读取到共享变量的最新值来实现比较并交换的效果**

* CAS保证原子性，配合volatile实现线程安全

## ABA问题

ABA问题是指：

* 假设当前线程针对内存中的某个地址V中的值进行修改，
* 这时另外一个线程也在V上进行修改操作，如果原来的值是A，修改后的值为B，再修改为A。
* 那么**当前线程的CAS操作无法判别地址V中的值是否发生过改动**

解决：

* **每次执行 CAS 操作，附加再更新一个版本号**，只要保证版本号是递增的，那么即便 A 变成 B 之后再变回 A，版本号也不会变回来（版本号递增的）。

* AtomicStampedReference 和AtomicMarkableReference 这两个原子类可以解决 ABA 问题。

    * AtomicStampedReference 实现的 CAS 方法就增加了版本号参数，方法签名如下：

        ```java
        boolean compareAndSet(
            V expectedReference,
            V newReference,
            int expectedStamp,
            int newStamp)
        ```

    * AtomicMarkableReference 的实现机制则更简单，将版本号简化成了一个 Boolean 值，

        ```java
        boolean compareAndSet(
            V expectedReference,
            V newReference,
            boolean expectedMark,
            boolean newMark)
        ```

**所有原子类的方法都是针对一个共享变量的，如果你需要解决多个变量的原子性问题，建议还是使用互斥锁方案**

## CAS 的特点

1. **结合 CAS 和 volatile 可以实现无锁并发，适用于线程数少、多核 CPU 的场景下**。
    1. CAS 体现的是无锁并发、无阻塞并发
2. CAS 是基于**乐观锁**的思想：最乐观的估计，不怕别的线程来修改共享变量，不断的尝试修改。
3. synchronized 是基于**悲观锁**的思想：最悲观的估计，得防着其它线程来修改共享变量。
4. 因为没有使用 synchronized，所以线程不会陷入阻塞，这是效率提升的因素之一
5. 但如果竞争激烈，可以想到重试必然频繁发生，反而效率会受影响



## CAS 缺点

1. **ABA问题**：
2. **循环时间长开销大**：
    1. **自旋CAS** （不成功，就一直循环执行，直到成功）如果长时间不成功，会给CPU带来非常大的执行开销。
    2. **如果JVM 能支持处理器提供的pause 指令那么效率会有一定的提升**， pause 指令有两个作用，
        1. 可以延迟流水线执行指令（ de-pipeline ）,使CPU 不会消耗过多的执行资源，延迟的时间取决于具体实现的版本，在一些处理器上延迟时间是零。
        2. 可以避免在退出循环的时候因内存顺序冲突（ memory order violation ）而引起CPU流水线被清空（ CPU pipeline flush ），从而提高CPU的执行效率。
3. **只能保证一个共享变量的原子操作**： 
    1. 当对一个共享变量执行操作时，我们可以使用循环CAS的方式来保证原子操作，
    2. 但是对多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候就可以用锁，
        1. 或者有一个取巧的办法，就是把多个共享变量合并成一个共享变量来操作。
        3. 从Java 1.5 开始JDK提供了AtomicReference 类来保证引用对象之间的原子性，你可以把多个变量放在一个对象里来进行CAS 操作。

## AtomicInteger 类

方法签名如下：

```java
public final int get() 							//获取当前的值
public final int getAndSet(int newValue)		//获取当前的值，并设置新的值
public final int getAndIncrement()				//获取当前的值，并自增
public final int getAndDecrement() 				//获取当前的值，并自减
public final int getAndAdd(int delta) 			//获取当前的值，并加上预期的值
boolean compareAndSet(int expect, int update) 	//如果输入的数值等于预期值，则以原子方式将该值设置为输入值（update）
public final void lazySet(int newValue)			//最终设置为newValue,使用 lazySet 设置之后可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
```

其中的**关键是 compareAndSet**，原子操作

----

# 四、synchronized

synchronized是java提供的**原子性内置锁**，这种内置的并且使用者看不到的锁也被称为**监视器锁**

* **synchronized依赖操作系统底层互斥锁实现**，他的作用主要就是**实现原子性操作**和**解决共享变量的内存可见性**问题
* 使用synchronized之后，会在编译之后在同步的代码块前后加上**monitorenter**和**monitorexit**字节码指令
    * 执行**monitorenter**指令时会尝试获取对象锁，如果对象没有被锁定或者已经获得了锁，**锁的计数器+1**。此时其他竞争锁的线程则会进入**等待队列**中。
    * 执行**monitorexit**指令时则会把**锁的计数器-1**，当计数器值为0时，则锁释放，处于等待队列中的线程再继续竞争锁。
* 从内存语义来说，
    * **加锁**的过程会**清除工作内存中的共享变量，再从主内存读取**
    * **释放锁**的过程则是将**工作内存中的共享变量写回主内存**。

synchronized是排它锁，当一个线程获得锁之后，其他线程必须等待该线程释放锁后才能获得锁，而且由于Java中的线程和操作系统原生线程是一一对应的，线程被阻塞或者唤醒时时会从用户态切换到内核态，这种转换非常消耗性能。



**synchronized需要注意的地方：**

1. **synchronized 在发生异常时，会自动释放线程占有的锁**，因此不会导致死锁现象发生；
2. 使用synchronized时，等待的线程会一直等待下去，**不能够响应中断**
3. synchronized不知道有没有成功获取锁



ReentrantLock 相比 synchronized 优点：

* **ReentryLock 异常时不会释放锁**
* ReentrantLock **可中断**
* ReentrantLock **可以设置超时时间**
* ReentrantLock  **可以设置为公平锁**
* ReentrantLock **支持多个条件变量**
* **ReentryLock 非阻塞地获取锁**，**如果尝试获取锁失败，并不进入阻塞状态，而是直接返回**

## 4.1 锁粒度

### 1 同步代码块

synchronized 只**作用于同一个对象**，如果调用两个对象上的同步代码块，就不会进行同步。

* synchronized 实际上是用对象锁保证了临界区内代码的原子性。

对于以下代码，使用 ExecutorService 执行了两个线程，由于调用的是同一个对象的同步代码块，因此这两个线程会进行同步，当一个线程进入同步语句块时，另一个线程就必须等待。

```java
public class Synchronize {
    private static int counter = 0;
    private Object lock = new Object();

    public void add() {
        synchronized (this) {
            for (int i = 0; i < 10; i++) {
                System.out.print(i + " ");
            }
        }
    }

    public void testSynchronize() throws InterruptedException {
        Synchronize e1 = new Synchronize();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> e1.add());
        executorService.execute(() -> e1.add());
    }
}

// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9
```



```java
public void testSynchronize() throws InterruptedException {
    Synchronize e1 = new Synchronize();
    Synchronize e2 = new Synchronize();
    ExecutorService executorService = Executors.newCachedThreadPool();
    executorService.execute(() -> e1.add());
    executorService.execute(() -> e2.add());
}
// 0 0 1 1 2 2 3 3 4 4 5 5 6 6 7 7 8 8 9 9
```



### 2 同步方法

它和同步代码块一样，**作用于同一个对象**。

```java
public synchronized void func () {
	// ...
}

// 等价于
public void func () {
	synchronized (this) {
    	// ...   
    }
}
```



### 3 同步类

```java
public void func() {
    synchronized (SynchronizedExample.class) {
    	// ...
    }
}
```

**作用于整个类**，也就是说两个线程调用同一个类的不同对象上的这种同步语句，也会进行同步。

* 类锁是指作用在了类对象上的所

```java
public class Synchronize {
    private static int counter = 0;
    private Object lock = new Object();

    public void add() {
        synchronized (Synchronized.class) {
            for (int i = 0; i < 10; i++) {
                System.out.print(i + " ");
            }
        }
    }

    public void testSynchronize() throws InterruptedException {
        Synchronize e1 = new Synchronize();
        Synchronize e2 = new Synchronize();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> e1.add());
        executorService.execute(() -> e2.add());
    }
}
```



### 4 同步静态方法

**作用于整个类**（获得该类类锁）

```java
public synchronized static void fun() {
	// ...
}

// 等价于
public void func () {
	synchronized (this.class) {
    	// ...   
    }
}
```

### 5 注意点

**类锁和对象锁是两把不同的锁，不互斥！！！**

* 因为静态成员不属于任何一个实例对象，是类成员（ static 表明这是该类的一个静态资源，不属于对象）。
* 所以，如果一个线程 A 调用一个实例对象的非静态 synchronized 方法，而线程 B 需要调用这个实例对象所属类的静态 synchronized 方法，是允许的，不会发生互斥现象，
* 因为访问**静态 synchronized 方法占用的锁是当前类的锁**，而访问**非静态 synchronized 方法占用的锁是当前实例对象锁**。

---

## 4.2 锁优化

JDK1.6 对synchronized 锁的实现引入了大量的优化，如偏向锁、轻量级锁、自旋锁、适应性自旋锁、锁消除、锁粗化等技术来减少锁操作的开销。

Java中的synchronized 有三种，他们会随着竞争的激烈而逐渐升级：

1. **偏向锁(biasable)**：锁只被一个线程持有
2. **轻量级锁(lightweight locked)**：不同线程交替持有锁
3. **重量级锁(inflated)**，多线程竞争锁三种情况

### JVM对象在内存中的结构

*以32位虚拟机为例*

**Java对象在内存中实际包含：**

1. 对象头
2. 实例数据
3. 对齐填充

普通对象的对象头分为了两部分：

1. **Mark Word**：记录了该对象和锁有关的信息，当这个对象被synchronized关键字当成同步锁时，围绕这个锁的一系列操作都和Mark Word有关。
    1. MarkWord里默认数据是存储对象的HashCode等信息，但是在运行期间，**Mark Word里存储的数据会随着锁标志位（对象锁状态）的变化而变化**。
    2. Mark Word 具体的内容包含对象的**hashcode、分代年龄、偏向锁线程ID、偏向锁时间戳、轻量级锁指针、重量级锁指针、GC标记**。
2. **Klass Word**：Java类对象的数据保存在方法区，Klass Word 是一个指向方法区中Class类对象信息的指针。
3. Array Length：只有数组对象保存了这部分数据。

![image-20210222134830464](http://haoimg.hifool.cn/img/image-20210222134830464.png)

数组对象

![image-20210222135139481](http://haoimg.hifool.cn/img/image-20210222135139481.png)

Mark Word 结构：

* Mark Word 里默认数据是存储对象的HashCode等信息，但是在运行期间，Mark Word里存储的数据会随着锁标志位的变化而变化。
* 可以看到对象头中的Mark Word是可以变动的（一共32位）

![image-20210222134854869](http://haoimg.hifool.cn/img/image-20210222134854869.png)

----



### Monitor（锁）

Monitor 被翻译为监视器或管程

* 监视器锁本质**依赖于底层的操作系统的Mutex Lock来实现的**

**每个 Java 对象都可以关联一个 Monitor 对象**

* 如果**使用 synchronized 给对象上锁（重量级）之后，该对象头的Mark Word 中就被设置指向 Monitor 对象的指针。**

**Monitor结构：**

* EntryList：
* WaitList：
* Owner：

![image-20210222152321215](http://haoimg.hifool.cn/img/image-20210222152321215.png)

1. 开始时，Monitor 中 Owner 为 null
2. 当 Thread-2 执行 synchronized(obj) 时，**obj对象的对象头中的Mark Word就会指向该Monitor**，然后**JVM将 Monitor对象中的 Owner 置为 Thread-2**，Monitor中只能有一个 Owner。
3. 在 Thread-2 上锁的过程中，如果 Thread-3，Thread-4，Thread-5 也来执行 synchronized(obj)，这些**线程就会进入EntryList，进入BLOCKED状态**
4. Thread-2 执行完同步代码块的内容，然后**唤醒 EntryList 中等待的线程来竞争锁**，竞争的时是非公平的。
5. 图中 **WaitSet** 中的 Thread-0，Thread-1 是**wait() 之前获得过锁，但条件不满足，调用wait() 方法，进入 WAITING 状态的线程**
    1. 当其他的线程notify()时，Thread-0，Thread-1再重新进入到EntryList中重新进行竞争锁

注意：

* **synchronized 必须是进入同一个对象的 monitor 才有上述的效果**
* **不加 synchronized 的对象不会关联监视器**，不遵从以上规则

---

### synchronize原理 - 字节码角度原理

.java 源码

```java
static final Object lock = new Object();
static int counter = 0;

public static void main(String[] args) {
	synchronized (lock) {
		counter++;
	}
}
```



.class 字节码

```java
public static void main(java.lang.String[]);
	descriptor: ([Ljava/lang/String;)V
	flags: ACC_PUBLIC, ACC_STATIC
    Code:
        stack=2, locals=3, args_size=1
            0: getstatic #2 	// 获取 <- lock引用 （synchronized开始）
            3: dup
            4: astore_1 		// lock引用 （存储）-> slot 1，方便后面使用lock解锁
            5: monitorenter 	// 将 lock对象 MarkWord 置为 Monitor 指针
            6: getstatic #3 	// <- i
            9: iconst_1 		// 准备常数 1
            10: iadd 			// +1
            11: putstatic #3 	// -> i
            14: aload_1 		// 获取 <- lock引用，解锁
            15: monitorexit 	// 将 lock对象 MarkWord 重置, 唤醒 EntryList
            16: goto 24
            19: astore_2 		// 如果有异常，处理异常 e -> slot 2
            20: aload_1 		// <- lock引用
            21: monitorexit 	// 将 lock对象 MarkWord 重置, 唤醒 EntryList
            22: aload_2 		// <- slot 2 (e)
            23: athrow 			// throw e
            24: return
        Exception table:
            from  to target type
                6 16 19 any		// 数字指的是行号
                19 22 19 any
        LineNumberTable:
            line 8: 0
            line 9: 6
            line 10: 14
            line 11: 24
        LocalVariableTable:
          Start Length Slot Name Signature
            0 25 0 args [Ljava/lang/String;
        StackMapTable: number_of_entries = 2
          frame_type = 255 /* full_frame */
            offset_delta = 19
            locals = [ class "[Ljava/lang/String;", class java/lang/Object ]
            stack = [ class java/lang/Throwable ]
          frame_type = 250 /* chop */
            offset_delta = 4
```

---

### 重量级锁

重量级锁时，对象头的Mark Word存放monitor对象指针 。

monitor**调用的是操作系统底层的互斥量(mutex)**。

* **JVM会阻塞未获取到锁的线程，使用互斥量进行同步，阻塞和唤醒操作需要从用户态切换到内核态，开销很大**。

synchronized 同步语句块的情况：

* monitorenter 指令指向同步代码块的开始位置
* monitorexit 指令则指明同步代码块的结束位置。
* 每个对象都有一个monitor监视器，调用monitorenter就是尝试获取这个对象，成功获取到了就将值+1，离开就将值减1。
    * 如果是线程重入，再将值+1，说明monitor对象是支持可重入的。

synchronized 修饰方法的的情况：

* ACC_SYNCHRONIZED 标识，该标识指明了该方法是一个同步方法，JVM 通过该 ACC_SYNCHRONIZED 访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

---

### 轻量级锁

**“轻量级”是相对于使用操作系统互斥量来实现的重量锁而言的**，操作系统实现线程之间的**切换需要从用户态转换到核心态**，这个成本非常高，状态之间的转换需要相对比较长的时间。

* 轻量锁的本意是在**没有多线程竞争的前提下，减少传统的重量级锁使用互斥量产生的性能消耗**。

轻量级锁的优化依据：

* 如果一个对象**虽然被多个线程用来加锁，但加锁的时间是错开的（多个对象之间对锁没有竞争）**，那么可以使用轻量级锁来优化。
* **轻量级锁能提升程序同步性能的依据是“对于绝大部分的锁，在整个同步周期内都是不存在竞争的”这一经验法则。**
    * 如果**多个线程之间对锁没有竞争**，轻量级锁便**通过CAS操作成功避免了使用互斥量的开销**；
    * 但如果确实**存在锁竞争**，除了**互斥量**的本身开销外，还额外发生了**CAS操作**的开销。
    * **因此在有竞争的情况下，轻量级锁反而会比传统的重量级锁更慢。**

**轻量级锁的使用**：

- 轻量级锁对使用者是透明的，即语法仍然是 synchronized
- 轻量级锁的操作也很轻便，它只是简单地**将对象头部作为指针，指向持有锁的线程堆栈的内部锁记录，通过对象头来判断一个线程是否持有对象锁**。
    - 如果线程获得轻量级锁成功，则可以顺利进入临界区。
    - 如果轻量级锁加锁失败，则表示其他线程抢先争夺到了锁，那么当前线程的锁请求就会膨胀为重量级锁。

**轻量级锁的实现过程：**

1. 线程创建锁记录（Lock Record）对象，每个线程都的栈帧都会包含一个锁记录的结构，锁记录内部可以存储锁定对象的Mark Word

    ![image-20210222161709017](http://haoimg.hifool.cn/img/image-20210222161709017.png)

2. 线程锁记录中 Object reference 指向锁对象，并尝试用 CAS指令 替换 Object 的 Mark Word，将 Mark Word 的值存入锁记录

    ![image-20210222161746951](http://haoimg.hifool.cn/img/image-20210222161746951.png)

3. 如果 CAS 替换成功，则锁定对象的对象头中存储了锁记录地址和状态 00 ，表示由该线程给对象加锁

    ![image-20210222161822096](http://haoimg.hifool.cn/img/image-20210222161822096.png)

4. 如果 CAS 失败，有两种情况：

    1. 如果是其它线程已经持有了该 Object 的轻量级锁，这时表明有竞争，进入锁膨胀过程

    2. 如果是该线程执行了 synchronized 锁重入，那么再添加一条 Lock Record 作为重入的计数

        ![image-20210222161851487](http://haoimg.hifool.cn/img/image-20210222161851487.png)

5. 当退出 synchronized 代码块（解锁时），使用CAS操作将对象头当前的Mark Word和线程中锁记录复制的Displaced Mark Word替换回来

    1. 如果锁记录中有取值为 null 的锁记录，表示有重入，这时重置锁记录，表示重入计数减一
    2. 锁记录的值不为 null，这时使用 CAS 将 Mark Word 的值恢复给对象头
        1. 成功，则解锁成功，对象头当前的Mark Word恢复到无锁的状态(01)。
        2. 失败，说明轻量级锁进行了锁膨胀或已经升级为重量级锁，进入重量级锁解锁流程)，那就要在释放锁的同时，唤醒被挂起的线程。

    ![image-20210222162039456](http://haoimg.hifool.cn/img/image-20210222162039456.png)

> 什么是重入？
>
> 同一个线程对同一个对象进行多次加锁（这些加锁没有必要，因为没有涉及到竞争问题）

---

### 偏向锁

**优化：轻量级锁在没有竞争时（就自己这个线程），每次重入仍然需要执行 CAS 操作来替换Mark Word，浪费时间**。

* **核心思想**：
    * 如果一个线程获得了锁，那么锁就进入偏向模式。
    * 当这个线程**再次请求锁时，只需检查锁对象的对象头中的Mark Word 是不是该线程自己的线程ID，无须再做任何同步操作(加锁、解锁）**，节省了大量有关锁申请的操作，从而提高了程序性能。
* **适用场景**：
    * **多个线程很少进行锁竞争的场合**。
    * 偏向锁可以**提高带有同步但无竞争的程序性能**，但它同样是一个带有效益权衡（Trade Off）性质的优化，也就是说它并非总是对程序运行有利。
    * **如果程序中大多数的锁都总是被多个不同的线程访问，那偏向模式就是多余的**

![image-20210222192404367](http://haoimg.hifool.cn/img/image-20210222192404367.png)

![image-20210222192413165](http://haoimg.hifool.cn/img/image-20210222192413165.png)

原理：

1. **当该对象第一次被线程获得锁的时候**，会用CAS指令，将该对象头的 mark word 中的thread id由0改成当前线程Id。

    - 如果成功，则代表获得了偏向锁，继续执行同步块中的代码。

    - 如果失败，将偏向锁撤销，升级为轻量级锁。

2. **当被偏向的线程再次进入同步块时**，发现锁对象偏向的就是当前线程，此时不需要再加锁。

3. **当其他线程进入同步块时**，发现已经有偏向的线程了，则会进入到撤销偏向锁的逻辑里，一般来说，会在safepoint 中去查看偏向的线程是否还存活，

    1. **如果存活且还在同步块中**，则将锁升级为轻量级锁，原偏向的线程继续拥有锁，当前线程则走入到锁升级的逻辑里；
    2. **如果偏向的线程已经不存活或者不在同步块中**，则将对象头的mark word 改为无锁状态（unlocked），之后再升级为轻量级锁。
    3. safe point 在GC中经常会提到，其代表了一个状态，在该状态下所有线程都是暂停的

#### 1 偏向状态

*64位虚拟机中对象头的格式*

![image-20210222192526940](http://haoimg.hifool.cn/img/image-20210222192526940.png)

一个对象创建时：

- 如果开启了偏向锁（默认开启），那么对象创建后，markword 值为 0x05 即最后 3 位为 101，这时它的thread、epoch、age 都为 0
- 偏向锁是默认是延迟的，不会在程序启动时立即生效，而是在程序运行几秒之后生效，如果想避免延迟，可以加 VM 参数 -XX:BiasedLockingStartupDelay=0 来禁用延迟
- 如果没有开启偏向锁，那么对象创建后，markword 值为 0x01 即最后 3 位为 001，这时它的 hashcode、age 都为 0，第一次用到 hashcode 时才会赋值

#### 2 撤销偏向锁 - 哈希码 

在Java语言里面一个对象如果计算过哈希码，哈希码就应该一直保持该值不变（强烈推荐但不强制，因为用户可以重载hashCode()方法按自己的意愿返回哈希码），否则很多依赖对象哈希码的API都可能存在出错风险。

而**作为绝大多数对象哈希码来源的Object::hashCode() 方法，返回的是对象的一致性哈希码**（Identity Hash Code），**这个值是能强制保证不变的**，它**通过在对象头中存储计算结果来保证第一次计算之后，再次调用该方法取到的哈希码值永远不会再发生改变**。

**Normal正常状态下Mark Word中hashcode需要占用31位的空间，如果处于偏向锁Biased状态时，如上图，就没有相应的状态去存储hashcode了**。

* **因此，当一个对象已经计算过一致性哈希码后，它就再也无法进入偏向锁状态了；**
* 而当一个对象当前正处于偏向锁状态，又收到需要计算其**一致性哈希码请求时，它的偏向状态会被立即撤销，并且锁会膨胀为重量级锁。**

在**Heavyweight Locked 重量级锁的状态中**，对象头指向了重量级锁的位置，**代表重量级锁的ObjectMonitor类里有字段可以记录非加锁状态（标志位为“01”）下的Mark Word，其中自然可以存储原来的哈希码。**

#### 3 撤销偏向锁 - 其他线程使用

**当有其它线程使用偏向锁对象时，会将偏向锁升级为轻量级锁**

```java
private static void test2() throws InterruptedException {
    Dog d = new Dog();
    Thread t1 = new Thread(() -> {
        synchronized (d) {
            log.debug(ClassLayout.parseInstance(d).toPrintableSimple(true));
        }
        synchronized (TestBiased.class) {
            TestBiased.class.notify();
        }
    }, "t1");
    t1.start();

    Thread t2 = new Thread(() -> {
        synchronized (TestBiased.class) {
            TestBiased.class.wait();
        }
        log.debug(ClassLayout.parseInstance(d).toPrintableSimple(true));
        synchronized (d) {
            log.debug(ClassLayout.parseInstance(d).toPrintableSimple(true));
        }
        log.debug(ClassLayout.parseInstance(d).toPrintableSimple(true));
    }, "t2");
    t2.start();
}

[t1] - 00000000 00000000 00000000 00000000 00011111 01000001 00010000 00000101
[t2] - 00000000 00000000 00000000 00000000 00011111 01000001 00010000 00000101
[t2] - 00000000 00000000 00000000 00000000 00011111 10110101 11110000 01000000 
[t2] - 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000001 // 撤销 偏向锁
```

#### 4 批量重定向

如果**对象虽然被多个线程访问，升级为了轻量级锁，但没有竞争，这时偏向了线程 T1 的对象仍有机会重新偏向 T2**，重偏向会重置对象的 Thread ID

**当撤销偏向锁阈值超过 20 次后，jvm 会在给这些对象加锁时重新偏向至加锁线程**

**偏向锁重偏向一次之后不可再次重偏向。**

#### 5 批量撤销

在多线程竞争剧烈的情况下，使用偏向锁将会降低效率，于是乎产生了批量撤销机制。

**当撤销偏向锁阈值超过 40 次后**，jvm 会这样觉得，自己确实偏向错了，根本就不该偏向。

**当某个类已经触发批量撤销机制后，JVM会默认当前类产生了严重的问题，剥夺了该类的新实例对象使用偏向锁的权利**

---

### 锁膨胀

如果在**尝试加轻量级锁的过程中，CAS 操作无法成功**

* 原因：**其它线程为此对象加上了轻量级锁（有竞争），这时需要进行锁膨胀**，将轻量级锁变为重量级锁。

```java
static Object obj = new Object();
public static void method1() {
    synchronized( obj ) {
    	// 同步块
    }
}
```

1. 当 Thread-1 进行轻量级加锁时，Thread-0 已经对该对象加了轻量级锁

    ![image-20210222164704119](http://haoimg.hifool.cn/img/image-20210222164704119.png)

2. 这时 Thread-1 加轻量级锁失败，进入锁膨胀流程

    1. 为 Object 对象申请 Monitor 锁，让 Object 指向重量级锁地址

    2. 然后 Thread-1自己进入 Monitor 的 EntryList BLOCKED

        ![image-20210222164716814](http://haoimg.hifool.cn/img/image-20210222164716814.png)

3. 当 Thread-0 退出同步块解锁时，使用 CAS 将 Mark Word 的值恢复给对象头，失败。这时会进入重量级解锁流程，

    1. 按照obj对象头的 Monitor 地址找到 Monitor 对象，
    2. 设置 Owner 为 null，
    3. 唤醒 EntryList 中 BLOCKED 线程

---

### 自旋优化

重量级锁竞争的时候，互斥同步进入阻塞状态的开销都很大，挂起线程和恢复线程的操作都需要转入内核态中完成，这些操作给Java虚拟机的并发性能带来了很大的压力，应该尽量避免。

* 依据：在许多应用中，共享数据的锁定状态只会持续很短的一段时间。
* 思想：**让一个线程在请求一个共享数据的锁时执行忙循环（自旋）一段时间，不断的判断锁资源是否释放，如果在这段时间内能获得锁，就可以避免进入阻塞状态。**

* 单核 CPU 自旋就是浪费，多核 CPU 自旋才能发挥优势。
* 在 Java 6 之后自旋锁是自适应的，比如对象刚刚的一次自旋操作成功过，那么认为这次自旋成功的可能性会高，就多自旋几次；反之，就少自旋甚至不自旋，总之，比较智能。
* Java 7 之后不能控制是否开启自旋功能



**自适应自旋锁**：

* **在 JDK 1.6 中引入了自适应的自旋锁**。自适应意味着自旋的次数不再固定了，而是由前一次在同一个锁上的自旋次数及锁的拥有者的状态来决定。
* 自旋成功了，则下次自旋的次数会增多。反之，如果对某个锁自旋很少成功，那么在以后获取这个锁的时候，自旋的次数会变少。
* 自适应自旋随着程序运行时间的增长及性能监控信息的不断完善，虚拟机对程序锁的状况预测就会越来越精准，虚拟机就会变得越来越“聪明”了。

自旋重试成功的情况：

![image-20210222175345424](http://haoimg.hifool.cn/img/image-20210222175345424.png)



自旋重试失败的情况：

![image-20210222175408536](http://haoimg.hifool.cn/img/image-20210222175408536.png)

---

### 锁消除

锁消除是指**JVM检测到一些同步的代码块不存在数据竞争**，**对于被检测出不可能存在竞争的共享数据的锁进行消除**。

锁消除主要是通过逃逸分析来支持：

1. 如果堆上的共享数据不可能逃逸出去被其它线程访问到，
2. 那么就可以把它们当成私有数据对待，
3. 也就可以将它们的锁进行消除。

对于一些看起来没有加锁的代码，其实隐式的加了很多锁。例如下面的字符串拼接代码就隐式加了锁：

```java
public static String concatString(String s1, String s2, String s3) {
	return s1 + s2 + s3;
}
```

String 是一个不可变的类，编译器会对 String 的拼接自动优化。在 JDK 1.5 之前，会转化为StringBuffer 对象的连续 append() 操作：

```java
public static String concatString(String s1, String s2, String s3) {
    StringBuffer sb = new StringBuffer();
    sb.append(s1);
    sb.append(s2);
    sb.append(s3);
    return sb.toString();
}
```

每个 append() 方法中都有一个同步块。JVM虚拟机观察变量 sb，很快就会发现它的动态作用域被限制在concatString() 方法内部。

* 也就是说，sb 的所有引用永远不会逃逸到concatString() 方法之外，其他线程无法访问到它，因此可以进行消除。



---

### 锁粗化

如果一系列的连续操作都**对同一个对象反复加锁和解锁，导致线程发生多次重入，频繁的加锁操作就会导致性能损耗**。

1. 如果虚拟机探测到由这样的一串零碎的操作都对同一个对象加锁，
2. 虚拟机将会把加锁的范围扩展（粗化）到整个操作序列的外部。
3. 对于上一节的示例代码就是扩展到第一个 append() 操作之前直至最后一个 append() 操作之后，这样只需要加锁一次就可以了。

---

### 不同锁之间的状态转换

锁主要存在4 种状态，级别从低到高依次是：

1. 无锁状态、
2. 偏向锁状态、
3. 轻量级锁状态
4. 重量级锁状态，

这几个状态会**随着竞争的情况逐渐升级**，这几个锁**只有重量级锁是需要使用操作系统底层mutex 互斥原语来实现，其他的锁都是使用对象头来实现的**。

* 需要注意锁可以升级，但是不可以降级。

![img](http://haoimg.hifool.cn/img/1595409-20190605170821057-1401609434.png)



---





### 锁的优缺点对比

| **锁** |                             优点                             |                             缺点                             |                           适用场景                           |
| :----: | :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| 偏向锁 | 加锁和解锁不需要CAS操作，没有额外的性能消耗，和执行非同步方法相比仅存在纳秒级的差距 |         若线程间存在锁竞争，会带来额外的锁撤销的消耗         |              只有一个线程访问同步块或者同步方法              |
| 轻量锁 |           竞争的线程不会阻塞，提高了程序的响应速度           |         若线程长时间竞争不到锁，自旋会消耗 CPU 性能          | 线程交替执行同步块或者同步方法，追求响应时间，锁占用时间很短 |
| 重量锁 |               线程竞争不使用自旋，不会消耗 CPU               | 线程阻塞，响应时间缓慢，在多线程下，频繁的获取释放锁，会带来巨大的性能消耗 |                  追求吞吐量，锁占用时间较长                  |

---

# 五、volatile

JMM 即 Java Memory Model，它定义了**主存**、**工作内存**的抽象概念，底层对应着 CPU 寄存器、缓存、硬件内存、CPU 指令优化等。

CPU的发展带来的一些问题：

* **缓存会导致缓存一致性的问题**，所以加入了缓存一致性协议，同时会导致内存可见性的问题，
    * 随着CPU和内存的发展速度差异的问题，导致CPU的速度远快于内存，所以现在的CPU加入了高速缓存，高速缓存一般可以分为L1、L2、L3三级缓存。
* **编译器和CPU的重排序导致了原子性和有序性的问题**
    * **原子性 - 保证指令不会受到线程上下文切换的影响**（上下文切换导致指令执行到一半就中断了）
    * **有序性 - 保证指令不会受 cpu 指令并行优化的影响**
* JMM内存模型正是对多线程操作下的一系列规范约束，因为不可能让代码去兼容所有的CPU，通过JMM我们才屏蔽了不同硬件和操作系统内存的访问差异，这样保证了Java程序在不同的平台下达到一致的内存访问效果，同时也是保证在高效并发的时候程序能够正确执行。



**volatile的自身特性：**

* **可见性**：对volatile变量的读，在任意一个线程中总是能看到对这个volatile变量最后的写入。
* **原子性**：对任意单个volatile变量的读/写具有原子性，但是对于复合型操作并不具备原子性。



**volatile 必须借助 CAS， 才能读取到共享变量的最新值来实现比较并交换的效果**

* CAS保证原子性，配合volatile实现线程安全



使用：应该使用volatile变量时：

* **对变量的写入不依赖变量的当前值**，或者你能确保只有单个线程更新变量的值。
* 该变量**不会与其他状态变量一起纳入不变性条件**中。
* **在访问变量时不需要加锁**。



volatile 的优点：

* **切换**：相比synchronized的加锁方式来解决共享变量的内存可见性问题，volatile就是更轻量的选择，他**没有上下文切换的额外开销成本**。
* **可见性**：使用volatile声明的变量，可以**确保值被更新的时候对其他线程立刻可见**。
    * **volatile使用内存屏障来保证不会发生指令重排**，解决了内存可见性的问题。



synchronized 关键字和 volatile 关键字的区别

- `synchronized` 关键字和 `volatile` 关键字是两个互补的存在，而不是对立的存在！
- **`volatile` 关键字**是线程同步的**轻量级实现**，所以**`volatile `性能肯定比`synchronized`关键字要好**。
- **`volatile` 关键字只能用于变量而 `synchronized` 关键字可以修饰方法以及代码块**。
- **`volatile` 关键字能保证数据的可见性，但不能保证数据的原子性。`synchronized` 关键字两者都能保证。**
- **`volatile`关键字主要用于解决变量在多个线程之间的可见性，而 `synchronized` 关键字解决的是多个线程之间访问资源的同步性。**

---

## 5.1 可见性

**可见性：保证的是在多个线程之间，一个线程对volatile变量的修改对另一个线程可见**

退不出的循环：main 线程对 run 变量的修改对于 t 线程不可见，导致了 t 线程无法停止：

```java
static boolean run = true;
public static void main(String[] args) throws InterruptedException {
    Thread t = new Thread(()->{
        while(run){
            // ....
        }
    });
    t.start();
    sleep(1);
    run = false; // 线程t不会如预想的停下来
}
```

原因：

1. 初始状态， t 线程刚开始**从主内存读取了 run 的值到工作内存**。

    ![image-20210223144509181](http://haoimg.hifool.cn/img/image-20210223144509181.png)

2. 因为 t 线程要频繁从主内存中读取 run 的值，JIT 编译器会**将 run 的值缓存至自己工作内存中的高速缓存中**，减少对主存中 run 的访问，提高效率

    ![image-20210223144557350](http://haoimg.hifool.cn/img/image-20210223144557350.png)

3. 1 秒之后，**main 线程修改了 run 的值，并同步至主存，而 t 是从自己工作内存中的高速缓存中读取这个变量的值**，结果永远是旧值

    ![image-20210223144621598](http://haoimg.hifool.cn/img/image-20210223144621598.png)



解决：volatile（易变关键字）

* **volatile 可以用来修饰成员变量和静态成员变量，可以避免线程从自己的工作缓存中查找变量的值，必须到主存中获取它的值，**
* **线程操作 volatile 变量都是直接操作主存**

## 5.2 原子性

可见性保证的是在多个线程之间，一个线程对 volatile 变量的修改对另一个线程可见，

* **volatile 变量不能保证语句指令交错执行时，带来的影响**

注意：

* **synchronized 语句块既可以保证代码块的原子性，也同时保证代码块内变量的可见性**。
    * **如果变量的生命周期在synchronized块内，则synchronized也可以保证语句的有序性，但如果不是，则不能保证有序性**
* 但缺点是**synchronized 是属于重量级操作，性能相对更低**

## 5.3 有序性

JVM 会在不影响正确性的前提下，可以调整语句的执行顺序。

### 1 单例模式：

例子：通过双重检查加锁（DCL）的方式来实现单例模式：

```java
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
```

**为什么要在变量singleton之间加上volatile关键字？** - 重拍带来的危害

* 对象的构造过程，实例化一个对象三个步骤：
    1. 分配内存空间。
    2. 初始化对象。
    3. 将内存空间的地址赋值给对应的引用。
* 由于操作系统可以对指令进行重排序，所以实例化一个对象的过程也可能会变成如下过程：
    1. 分配内存空间。
    2. 将内存空间的地址赋值给对应的引用。
    3. 初始化对象。
* 如果是这个流程，多线程环境下就可能将一个未初始化的对象引用暴露出来，从而导致不可预料的结果。
    * 因此，为了防止这个过程的重排序，我们需要将变量设置为volatile类型的变量。



**注上述单例模式的特点**：

* 懒惰实例化：只有首次调用才会实例化，之后不会再进行实例化操作
* 首次使用 getInstance() 才使用 synchronized 加锁，后续使用时无需加锁 

### 2 不使用Volatile

```java
public class Singleton {
    public static Singleton singleton;
    
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
```

**在多线程环境下，上面的代码中，如果变量singleton之间不加上volatile关键字是有问题的**：

* 第一个 if 使用了 INSTANCE 变量，是在同步块之外

不加volatile的字节码：

```
0: getstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
3: ifnonnull 37			// 跳转到37行
6: ldc #3 				// class cn/itCASt/n5/Singleton
8: dup
9: astore_0
10: monitorenter
11: getstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
14: ifnonnull 27
17: new #3 				// class cn/itCASt/n5/Singleton
20: dup
21: invokespecial #4 	// Method "<init>":()V
24: putstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
27: aload_0
28: monitorexit
29: goto 37
32: astore_1
33: aload_0
34: monitorexit
35: aload_1
36: athrow
37: getstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
40: areturn
```

**其中关键的几步**：

* 17 表示创建对象，将对象引用入栈 // new Singleton
* 20 表示复制一份对象引用 // 引用地址
* 21 表示利用一个对象引用，调用构造方法
* 24 表示利用一个对象引用，赋值给 static INSTANCE

**出错点**：也许 jvm 会优化为：先执行 24，再执行 21。

* **如果变量的生命周期在synchronized块内，则synchronized也可以保证语句的有序性，但如果不是，则不能保证有序性**
* **由于instance对象并不完全在synchronized块内，则说明可以进行重排序**



如果两个线程 t1，t2 按如下时间序列执行：

![image-20210223164211407](http://haoimg.hifool.cn/img/image-20210223164211407.png)

1. 关键在于 0: getstatic 这行代码在 monitor 控制之外，它就像之前举例中不守规则的人，可以越过 monitor 读取INSTANCE 变量的值
2. 这时 t1 还未完全将构造方法执行完毕，如果在构造方法中要执行很多初始化操作，那么 t2 拿到的是将是一个未初始化完毕的单例

### 3 使用Volatile

**解决方法**：

* **对 INSTANCE 使用 volatile 修饰即可，可以禁用指令重排**，但要注意在 JDK 5 以上的版本的 volatile 才会真正有效

使用Volatile之后的字节码：

```
						// -------------------------------------> 加入对 INSTANCE 变量的读屏障
0: getstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
3: ifnonnull 37
6: ldc #3 				// class cn/itCASt/n5/Singleton
8: dup
9: astore_0
10: monitorenter 		-----------------------> 保证原子性、可见性
11: getstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
14: ifnonnull 27
17: new #3 				// class cn/itCASt/n5/Singleton
20: dup
21: invokespecial #4 	// Method "<init>":()V
24: putstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
						// -------------------------------------> 加入对 INSTANCE 变量的写屏障
27: aload_0
28: monitorexit 		------------------------> 保证原子性、可见性
29: goto 37
32: astore_1
33: aload_0
34: monitorexit
35: aload_1
36: athrow
37: getstatic #2 		// Field INSTANCE:Lcn/itCASt/n5/Singleton;
40: areturn
```

![image-20210223164629713](http://haoimg.hifool.cn/img/image-20210223164629713.png)



---



## 5.4 Volatile 原理

**volatile 的底层实现原理是内存屏障**，Memory Barrier（Memory Fence）

1. 对 volatile 变量的写指令后会加入写屏障
2. 对 volatile 变量的读指令前会加入读屏障

### 0 什么是内存屏障？

为了提高性能，编译器和处理器进行指令重排。

* volatile的底层就是通过内存屏障来实现禁止指令重排。



内存屏障（英语：Memory barrier），也称内存栅栏，内存栅障，屏障指令等，是一类同步指令，它**使得 CPU 或编译器在对内存进行操作的时候, 严格按照一定的顺序来执行**

* 也就是说在memory barrier 之前的指令和memory barrier 之后的指令**不会由于系统优化等原因而导致乱序**。
* **内存屏障之前的所有写操作都要写入内存**；
* **内存屏障之后的读操作都可以获得同步屏障之前的写操作的结果**。
* 因此，**对于敏感的程序块，写操作之后、读操作之前可以插入内存屏障**。



常见**内存屏障完成一系列的屏障和数据同步功能**：

1. StoreStore屏障：禁止上面的普通写和volatile写重排序
2. StoreLoad屏障：禁止上面的volatile写和下面有可能出现的volatile读写重排序
3. LoadLoad屏障：禁止下面的普通读和上面的volatile读重排序
4. LoadStore屏障：禁止下面的普通写和上面的volatile读重排序



**volatile 采用保守内存屏障策略**：

```
普通读
普通写
StoreStore屏障 // 禁止 上面的普通写 和 下面的volatile写 重排序

volatile 写

StoreLoad屏障  // 禁止 上面的volatile写 与 下面可能有的volatile读/写重排序

volatile 读

LoadLoad屏障   // 禁止 下面的普通读 和 上面的volatile读重排序
LoadStore屏障  // 禁止 下面的普通写 和 上面的volatile读重排序
普通读
普通写
```



### 1 如何保证可见性？

1. **写屏障（store fence）保证在该屏障之前的，对共享变量的改动，都同步到主存当中**

    ```java
    public void actor2(I_Result r) {
        num = 2;
        ready = true; // ready 是 volatile 赋值带写屏障
        // 写屏障
    }
    ```

2. **读屏障（load fence）保证在该屏障之后，对共享变量的读取，加载的是主存中最新数据**

    ```java
    public void actor1(I_Result r) {
        // 读屏障
        // ready 是 volatile 读取值带读屏障
        if(ready) {
        	r.r1 = num + num;
        } else {
        	r.r1 = 1;
        }
    }
    ```

![image-20210223155450854](http://haoimg.hifool.cn/img/image-20210223155450854.png)

### 2 如何保证有序性？

1. **写屏障会确保指令重排序时，不会将写屏障之前的代码排在写屏障之后**

    ```java
    public void actor2(I_Result r) {
        num = 2;
        ready = true; // ready 是 volatile 赋值带写屏障
        // 写屏障
    }
    ```

2. **读屏障会确保指令重排序时，不会将读屏障之后的代码排在读屏障之前**

    ```java
    public void actor1(I_Result r) {
        // 读屏障
        // ready 是 volatile 读取值带读屏障
        if(ready) {
        	r.r1 = num + num;
        } else {
        	r.r1 = 1;
        }
    }
    ```

![image-20210223155450854](http://haoimg.hifool.cn/img/image-20210223155450854.png)



### 3 不能解决指令的交错

![image-20210223155830132](http://haoimg.hifool.cn/img/image-20210223155830132.png)

不能解决指令交错：

1. **写屏障仅仅是保证之后的读能够读到最新的结果，但不能保证读跑到它前面去**
    1. 上图中读操作发生在写屏障之前
2. **有序性的保证也只是保证了本线程内相关代码不被重排序**



---



## 5.5 Happens-before 原则

java 内存模型 JMM(Java Memory Model) 并**不能保证**：**一个线程对共享变量的写操作，对于其他线程对该共享变量的读操作可见**



**什么是Happens-before原则**？

* **规定了对共享变量的写操作 对其他线程的读操作可见**
* **是可见性与有序性**一套规则的总结



Happens-before原则有哪些？

1. **程序顺序性原则**

    在一个线程中，按照程序顺序，前面的操作Happen-Before于后续的任意操作。

2. **synchronized原则**

    对锁对象解锁之前的写操作， 对于接下来对锁对象加锁的其他线程的读操作可见。

3. **volatile原则**

    对一个 volatile 变量的写操作，对于后续其他线程对这个 volatile 变量的读操作可见。

4. **start() 原则**

    主线程 A start() 启动子线程 B 后，子线程 B 能够看到主线程在启动子线程 B 前的操作

5. **线程join() 原则**

    某线程在结束前对共享变量的写操作，对于得知它结束后的其他线程的读操作是可见的。通过调用该线程 的 join() 方法实现。

6. **线程interrupt原则**

    线程A 修改共享变量的写操作，在线程A调用线程B的interrupt() 方法后，对线程B通过Thread.interrupted()方法检测到中断发生后的读操作可见。

7. **默认值修改原则**

    对变量默认值（0，false，null）的写操作，对其他变量对该共享变量的读操作是可见的。

8. **对象终结原则**

    一个对象的初始化完成(构造函数执行结束)先行发生于它的finalize()方法的开始。

9. **传递性原则**

    如果 A Happens-Before B，且 B Happens-Before C，那么 A Happens-Before C。



---

# 六、ThreadLocal

**如何解决每一个线程都有自己的专属本地变量呢？**

* `ThreadLocal`类主要解决的就是让每个线程绑定自己的值，
* 可以将`ThreadLocal`类形象的比喻成存放数据的盒子，盒子中可以存储每个线程的私有数据。
    * 如果你创建了一个`ThreadLocal`变量，那么访问这个变量的每个线程都会有这个变量的本地副本
    * 各个线程可以使用 `get` 和 `set` 方法来获取默认值或将其值更改为当前线程所存的副本的值，从而避免了线程安全问题。

## **ThreadLocal 原理**

`Thread` 类中有一个 `threadLocals` 和 一个 `inheritableThreadLocals` 变量，它们都是 `ThreadLocalMap` 类型的变量

*  可以把 `ThreadLocalMap` 理解为`ThreadLocal` 类实现的定制化的 `HashMap`。
* 默认情况下这两个变量都是 null，只有当前线程调用 `ThreadLocal` 类的 `set`或`get`方法时才创建它们，
* 实际上调用这两个方法的时候，我们调用的是`ThreadLocalMap`类对应的 `get()`、`set()`方法。

```java
public class Thread implements Runnable {
    ......
    //与此线程有关的ThreadLocal值。由ThreadLocal类维护
    ThreadLocal.ThreadLocalMap threadLocals = null;

    //与此线程有关的InheritableThreadLocal值。由InheritableThreadLocal类维护
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
     ......
}
```

`ThreadLocal`类的`set()`方法

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```

**最终的变量是放在了当前线程的 `ThreadLocalMap` 中，并不是存在 `ThreadLocal` 上，`ThreadLocal` 可以理解为只是`ThreadLocalMap`的封装，传递了变量值。**

*  `ThrealLocal` 类中可以通过`Thread.currentThread()`获取到当前线程对象后，直接通过`getMap(Thread t)`可以访问到该线程的`ThreadLocalMap`对象。
* **每个`Thread`中都具备一个`ThreadLocalMap`，而`ThreadLocalMap`可以存储以`ThreadLocal`为 key ，Object 对象为 value 的键值对。**



`ThreadLocalMap`是`ThreadLocal`的静态内部类。

![image-20210315155403596](http://haoimg.hifool.cn/img/image-20210315155403596.png)

同一个线程中声明了两个 `ThreadLocal` 对象的话，会使用 `Thread`内部都是使用仅有那个`ThreadLocalMap` 存放数据的，`ThreadLocalMap`的 key 就是 `ThreadLocal`对象，value 就是 `ThreadLocal` 对象调用`set`方法设置的值。

![ThreadLocal数据结构](http://haoimg.hifool.cn/img/threadlocal%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.png)

## ThreadLocal 内存泄漏问题

`ThreadLocalMap` 中使用的 key 为 `ThreadLocal` 的弱引用,而 value 是强引用。

* 如果 `ThreadLocal` 没有被外部强引用的情况下，在垃圾回收的时候，key 会被清理掉，而 value 不会被清理掉。
* 这样一来，`ThreadLocalMap` 中就会出现 key 为 null 的 Entry。
* 假如我们不做任何措施的话，value 永远无法被 GC 回收，这个时候就可能会产生内存泄露。
* ThreadLocalMap 实现中已经考虑了这种情况，在调用 `set()`、`get()`、`remove()` 方法的时候，会清理掉 key 为 null 的记录。使用完 `ThreadLocal`方法后 最好手动调用`remove()`方法

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
        super(k);
        value = v;
    }
}
```

**弱引用介绍：**

> 如果一个对象只具有弱引用，那就类似于**可有可无的生活用品**。
>
> 弱引用与软引用的区别在于：
>
> * 只具有弱引用的对象拥有更短暂的生命周期。
> * 在垃圾回收器线程扫描它 所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。
> * 不过，由于垃圾回收器是一个优先级很低的线程， 因此不一定会很快发现那些只具有弱引用的对象。
>
> 弱引用可以和一个引用队列（ReferenceQueue）联合使用，如果弱引用所引用的对象被垃圾回收，Java 虚拟机就会把这个弱引用加入到与之关联的引用队列中。



---

# 七、线程池

线程池提供了一种限制和管理资源（包括执行一个任务）。 

* 每个线程池还维护一些基本统计信息，例如已完成任务的数量。



**线程池解决的问题是什么**？

线程池**解决的核心问题就是资源管理问题**。在并发环境下，系统不能够确定在任意时刻中，有多少任务需要执行，有多少资源需要投入。

1. **频繁申请/销毁资源和调度资源**，将带来额外的消耗，可能会非常巨大。
2. 对资源**无限申请缺少抑制手段**，易引发系统资源耗尽的风险。
3. 系统**无法合理管理内部的资源分布**，会降低系统的稳定性。



**线程池的优点**：

* **复用线程，线程生命周期的开销非常高，减少了创建和销毁线程的次数**
    * **降低资源消耗**：通过池化技术重复利用已创建的线程，降低线程创建和销毁造成的损耗。
    * **提高响应速度**：任务到达时，无需等待线程创建即可立即执行。
* **提高线程的可管理性。**
    * 线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会因为线程的不合理分布导致资源调度失衡，降低系统的稳定性。使用线程池可以进行统一的分配、调优和监控。
* **线程池功能可拓展**：
    * 允许开发人员向其中增加更多的功能。比如延时定时线程池ScheduledThreadPoolExecutor，就允许任务延期执行或定期执行。



**新建线程 -> 达到核心数 -> 加入队列 -> 新建线程（救急线程） -> 达到最大数 -> 触发拒绝策略**



池化思想在其他地方上的应用：

1. **内存池**(Memory Pooling)：预先申请内存，提升申请内存速度，减少内存碎片。
2. **连接池**(Connection Pooling)：预先申请数据库连接，提升申请连接的速度，降低系统的开销。
3. **实例池**(Object Pooling)：循环使用对象，减少资源在初始化和释放时的昂贵损耗



## 自定义线程池

![img](http://haoimg.hifool.cn/img/16477f7912b4552a)

线程池的主要组成部分：

1. 线程池管理器（ThreadPool）：用于创建并管理线程池，包括 创建线程池，销毁线程池，添加新任务；
2. 工作线程（WorkThread）：线程池中线程，在没有任务时处于等待状态，可以循环的执行任务；
3. 任务接口（Task）：每个任务必须实现的接口，以供工作线程调度任务的执行，它主要规定了任务的入口，任务执行完后的收尾工作，任务的执行状态等；
4. 任务队列（taskQueue）：用于存放没有处理的任务。提供一种缓冲机制。

> 看一下自己写的线程池部分的代码



## ThreadPoolExecutor

![ThreadPoolExecutor UML类图](http://haoimg.hifool.cn/img/912883e51327e0c7a9d753d11896326511272.png)

* Executor：**将任务提交和任务执行进行解耦**。用户无需关注如何创建线程，如何调度线程来执行任务，用户只需提供Runnable对象，将任务的运行逻辑提交到执行器(Executor)中，由Executor框架完成线程的调配和任务的执行部分。
* ExecutorService：
    1. 扩充执行任务的能力，补充可以为一个或一批异步任务生成Future的方法
    2. 提供了管控线程池的方法，比如停止线程池的运行。
* AbstractExecutorService：将执行任务的流程串联了起来，保证下层的实现只需关注一个执行任务的方法即可。
* ThreadPoolExecutor：
    1. 维护自身线程池的生命周期
    2. 管理线程和任务



ThreadPoolExecutor总体设计：

![ThreadPoolExecutor运行流程](http://haoimg.hifool.cn/img/77441586f6b312a54264e3fcf5eebe2663494.png)

线程池在内部实际上构建了一个生产者消费者模型，将线程和任务两者解耦，并不直接关联，从而良好的缓冲任务，复用线程。

线程池的运行主要分成两部分：

* 任务管理：充当生产者的角色，当任务提交后，线程池会判断该任务后续的流转：
    1. 直接申请线程执行该任务
    2. 缓冲到队列中等待线程执行
    3. 拒绝该任务。
* 线程管理：是消费者，它们被统一维护在线程池内，根据任务请求进行线程的分配，当线程执行完任务后则会继续获取新的任务去执行，最终当线程获取不到任务的时候，线程就会被回收。



工作方式：**新建线程 -> 达到核心数 -> 加入队列 -> 新建线程（救急线程） -> 达到最大数 -> 触发拒绝策略**

```mermaid
graph LR

subgraph 阻塞队列
size=2
t3(任务3)
t4(任务4)
end

subgraph 线程池coreSize=2,maxSize=3
ct1(核心线程1)
ct2(核心线程2)
mt1(救急线程1)
ct1 --> t1(任务1)
ct2 --> t2(任务2)
end

style ct1 fill:#ccf,stroke:#f66,stroke-width:2px
style ct2 fill:#ccf,stroke:#f66,stroke-width:2px
style mt1 fill:#ccf,stroke:#f66,stroke-width:2px,stroke-dasharray: 5, 5
```

1. 线程池中刚开始没有线程，当一个任务提交给线程池后，线程池会创建一个新线程来执行任务。
2. 当线程数达到 corePoolSize 并没有线程空闲，这时再加入任务，新加的任务会被加入workQueue 队列排队，直到有空闲的线程。
3. 如果队列选择了有界队列，那么任务超过了队列大小时，会创建 (maximumPoolSize - corePoolSize) 数目的救急线程来救急。
4. 如果线程到达 maximumPoolSize 仍然有新任务时，这时会执行拒绝策略。
5. 当高峰过去后，超过corePoolSize 的救急线程如果一段时间没有任务做，需要结束节省资源，这个时间由keepAliveTime 和 unit 来控制。



###  1 线程池状态

线程池的状态分为两部分：

1. 运行状态 runState
2. 线程数量 workerCount

在实现的过程中，线程池的状态统一的由ctl这个AtomicInteger类型的变量进行维护，高 3 位来表示线程池状态，低 29 位表示线程数量。

* 用一个变量去存储两个值，可以用一次 CAS 原子操作对两个状态进行赋值，可避免在做相关决策时，出现不一致的情况，不必为了维护两者的一致，而占用锁资源。

```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
```

ctl 变量内部判断线程池运行状态和线程数量是基于位运算的方法进行判断的，速度会快很多。

```java
CAPACITY // 线程池内部线程的数量
private static int runStateOf(int c)     { return c & ~CAPACITY; } //计算当前运行状态
private static int workerCountOf(int c)  { return c & CAPACITY; }  //计算当前线程数量
private static int ctlOf(int rs, int wc) { return rs | wc; }   //通过状态和线程数生成ctl
```

![image-20210223223853774](http://haoimg.hifool.cn/img/image-20210223223853774.png)

从数字上比较，TERMINATED > TIDYING > STOP > SHUTDOWN > RUNNING



![img](http://haoimg.hifool.cn/img/170f73cc0dad76a5)

1. 线程池的初始化状态是**RUNNING**，能够**接收新任务**，以及对已添加的任务进行处理。
2. 线程池处在**SHUTDOWN**状态时，**不接收新任务，但能处理已添加的任务**。  调用线程池的shutdown()接口时，线程池由RUNNING -> SHUTDOWN。
3. 线程池处在**STOP**状态时，**不接收新任务，不处理已添加的任务，并且会中断正在处理的任务**。 调用线程池的shutdownNow()接口时，线程池由(RUNNING or SHUTDOWN ) -> STOP。
4. **当所有的任务已终止**，ctl记录的”任务数量”为0，线程池会变为**TIDYING**状态。
    - 当线程池在SHUTDOWN状态下，阻塞队列为空并且线程池中执行的任务也为空时，就会由 SHUTDOWN -> TIDYING。
    - 当线程池在STOP状态下，线程池中执行的任务为空时，就会由STOP -> TIDYING。 线程池彻底终止，就变成TERMINATED状态。
    - 当线程池变为TIDYING状态时，会执行钩子函数terminated()。terminated()在ThreadPoolExecutor类中是空的，若用户想在线程池变为TIDYING时，进行相应的处理；可以通过重载terminated()函数来实现。
5. 线程池处在TIDYING状态时，执行完terminated()之后，就会由 TIDYING -> TERMINATED。





---



### 2 任务管理

#### 1 任务调度

任务调度是线程池的主要入口，当用户提交了一个任务，决定该任务如何执行。

所有任务的调度都是由execute() 方法完成的：

1. 检查现在线程池的运行状态、运行线程数、运行策略，
2. 决定接下来执行的流程，是直接申请线程执行，或是缓冲到队列中执行，亦或是直接拒绝该任务。



execute() 执行过程如下：

![任务调度流程](http://haoimg.hifool.cn/img/31bad766983e212431077ca8da92762050214.png)

#### 2 任务缓冲

线程池中是以生产者消费者模式，通过一个阻塞队列来实现缓存任务的，工作线程从阻塞队列中获取任务。

阻塞队列(BlockingQueue)是一个支持两个附加操作的队列。

1. 在队列为空时，获取元素的线程会等待队列变为非空。
2. 当队列满时，存储元素的线程会等待队列可用。

使用不同的队列可以实现不一样的任务存取策略：

![img](http://haoimg.hifool.cn/img/725a3db5114d95675f2098c12dc331c3316963.png)



#### 3 任务申请

任务申请模块中，线程需要从任务缓存模块的阻塞队列中不断地取任务执行，实现线程管理模块和任务管理模块之间的通信。

任务的执行有两种可能：

1. 一种是任务直接由新创建的线程执行，仅出现在线程初始创建的时候。
2. 大多数的情况下，线程从任务队列中获取任务然后执行，执行完任务的空闲线程会再次去从队列中申请任务再去执行。



任务申请部分策略由getTask方法实现，getTask部分会进行多次判断，为的是控制线程的数量，使其符合线程池的状态。

* 如果线程池现在不应该持有那么多线程，则会返回null值。
* 工作线程Worker会不断接收新任务去执行，而当工作线程Worker接收不到任务的时候，就会开始被回收。

![获取任务流程图](http://haoimg.hifool.cn/img/49d8041f8480aba5ef59079fcc7143b996706.png)

#### 4 任务拒绝

任务拒绝模块是线程池的保护部分，线程池有一个最大的容量，当线程池的任务缓存队列已满，并且线程池中的线程数目达到maximumPoolSize时，就需要拒绝掉该任务，采取任务拒绝策略，保护线程池。

拒绝策略是一个接口，其设计如下：

```Java
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
```

用户可以通过实现这个接口去定制拒绝策略，拒绝策略 jdk 提供了 4 种实现

1. AbortPolicy（默认策略）：抛RejectedExecutionException 异常，并拒绝新任务的处理。在系统不能承载较大并发量时，能够及时的返回程序的运行状态，并能够通过异常发现。
2. DiscardPolicy：不处理新任务，直接丢弃掉但不抛出异常。
3. DiscardOldestPolicy：放弃队列中最早的任务，提交当前任务。
4. CallerRunsPolicy：把任务交给调用线程（提交任务的线程）处理。
    1. 也就是在调用execute()方法的线程中运行( run )被拒绝的任务，如果执行程序已关闭，则会丢弃该任务。因此这种策略会降低对于新任务提交速度，影响程序的整体性能。
    2. 这个策略喜欢增加队列容量。如果应用程序可以承受此延迟，并且不能丢弃任何一个任务请求的话，可以选择这个策略。

其它著名框架也提供了实现：

1. Dubbo 的实现，在抛出 RejectedExecutionException 异常之前会记录日志，并 dump 线程栈信息，方便定位问题
2. Netty 的实现，是创建一个新线程来执行任务
3. ActiveMQ 的实现，带超时等待（60s）尝试放入队列，类似我们之前自定义的拒绝策略
4. PinPoint 的实现，它使用了一个拒绝策略链，会逐一尝试策略链中每种拒绝策略



### 3 线程管理

#### 1 Worker线程

ThreadPoolExecutor线程池为了掌握线程的状态并维护线程的生命周期，设计了线程池内的工作线程Worker。



```Java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable{
    final Thread thread;//Worker持有的线程
    Runnable firstTask;//初始化的任务，可以为null
}
```

Worker

1. 实现了Runnable接口：
2. 持有一个线程thread：thread是在调用构造方法时通过ThreadFactory来创建的线程，可以用来执行任务
3. 持有一个初始化的任务firstTask：firstTask用它来保存传入的第一个任务，这个任务可以有也可以为null。
    1. 如果这个值是非空的，那么线程就会在启动初期立即执行这个任务，也就对应核心线程创建时的情况；
    2. 如果这个值是null，那么就需要创建一个线程去执行任务列表（workQueue）中的任务，也就是非核心线程的创建。

Worker执行任务的模型如下图所示：

![Worker执行任务](http://haoimg.hifool.cn/img/03268b9dc49bd30bb63064421bb036bf90315.png)

线程池需要管理线程的生命周期，需要在线程长时间不运行的时候进行回收。

* 线程池使用一张Hash表去持有线程的引用，这样可以通过添加引用、移除引用这样的操作来控制线程的生命周期。

    

如何判断线程是否在运行？

* Worker是通过继承AQS，使用AQS来实现独占锁这个功能。没有使用可重入锁ReentrantLock，而是使用AQS，为的就是实现不可重入的特性去反应线程现在的执行状态。

1. lock方法一旦获取了独占锁，表示当前线程正在执行任务中。 
2. 如果正在执行任务，则不应该中断线程。 
3. 如果该线程现在不是独占锁的状态，也就是空闲的状态，说明它没有在处理任务，这时可以对该线程进行中断。 
4. 线程池在执行shutdown方法或tryTerminate方法时会调用interruptIdleWorkers方法来中断空闲的线程，interruptIdleWorkers方法会使用tryLock方法来判断线程池中的线程是否是空闲状态；如果线程是空闲状态则可以安全回收。

#### 2 Worker线程增加

增加线程是通过线程池中的addWorker方法增加一个线程，这个分配线程的策略是在上个步骤完成的，该步骤仅仅完成增加线程，并使它运行，最后返回是否成功这个结果。

addWorker方法有两个参数：

1. firstTask：用于指定新增的线程执行的第一个任务，该参数可以为空；
2. core：
    1. core参数为true表示在新增线程时会判断当前活动线程数是否少于corePoolSize
    2. false表示新增线程前需要判断当前活动线程数是否少于maximumPoolSize

![申请线程执行流程图](http://haoimg.hifool.cn/img/49527b1bb385f0f43529e57b614f59ae145454.png)

#### 3 Worker线程回收

![线程池回收过程](http://haoimg.hifool.cn/img/9d8dc9cebe59122127460f81a98894bb34085.png)

线程池中线程的销毁依赖JVM自动的回收

* 线程池做的工作是根据当前线程池的状态维护一定数量的线程引用，防止这部分线程被JVM回收，
* 当线程池决定哪些线程需要回收时，只需要将其引用消除即可。
* Worker被创建出来后，就会不断地进行轮询，然后获取任务去执行，核心线程可以无限等待获取任务，非核心线程要限时获取任务。
* 当Worker无法获取到任务，也就是获取的任务为空时，循环会结束，Worker会主动消除自身在线程池内的引用。

```Java
try {
  	while (task != null || (task = getTask()) != null) {
    	//执行任务
  	}
} finally {
  	processWorkerExit(w, completedAbruptly);//获取不到任务时，主动回收自己
}
```

线程回收的工作是在processWorkerExit方法完成的。

![线程销毁流程](http://haoimg.hifool.cn/img/90ea093549782945f2c968403fdc39d415386.png)

事实上，在这个方法中，将线程引用移出线程池就已经结束了线程销毁的部分。但由于引起线程销毁的可能性有很多，线程池还要判断是什么引发了这次销毁，是否要改变线程池的现阶段状态，是否要根据新状态，重新分配线程。

#### 4 Worker线程执行任务

在Worker类中的run方法调用了runWorker方法来执行任务，runWorker方法的执行过程如下：

1. while循环不断地通过getTask()方法获取任务。 
2. getTask()方法从阻塞队列中取任务。 
3. 如果线程池正在停止，那么要保证当前线程是中断状态，否则要保证当前线程不是中断状态。 
4. 执行任务。 
5. 如果getTask结果为null则跳出循环，执行processWorkerExit()方法，销毁线程。

执行流程如下图所示：

![执行任务流程](http://haoimg.hifool.cn/img/879edb4f06043d76cea27a3ff358cb1d45243.png)



---



### 4 构造方法 & 参数介绍

```JAVA
public ThreadPoolExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler)
```

* corePoolSize：线程池核心线程个数 (最多保留的线程数)。当提交一个任务到线程池时，线程池会创建一个线程来执行任务，即使其他空闲的基本线程能够执行新任务也会创建线程，等到需要执行的任务数大于线程池基本大小时就不再创建。如果调用了线程池的prestartAllCoreThreads方法，线程池会提前创建并启动所有基本线程。
* maximumPoolSize：线程池最大线程数量。
* keepAliveTime 生存时间 - 针对救急线程。如果当前线程池中的线程数量比核心线程数量多（多出来的这些线程是救急线程），并且是闲置状态，则这些闲置的线程能存活的最大时间。救急线程最多有=(maximumPoolSize - corePoolSize) 个。
* unit：存活时间的时间单位 - 针对救急线程
* workQueue：用于保存等待执行的任务的阻塞队列，比如基于数组的有界ArrayBlockingQueue 、基于链表的无界LinkedBlockingQueue 、最多只有一个元素的同步队列SynchronousQueue 及优先级队列PriorityBlockingQueue 等。
* threadFactory ：创建线程的工厂 - 可以在线程创建时起名字
* RejectedExecutionHandler：拒绝策略， 当队列满并且线程个数达到maximunPoolSize 后采取的策略， 比如AbortPolicy （抛出异常〉、CallerRunsPolicy （使用调用者所在线程来运行任务） 、DiscardOldestPolicy （调用poll 丢弃一个任务，执行当前任务）及DiscardPolicy （默默丢弃，不抛出异常〉



![image-20210223225257837](http://haoimg.hifool.cn/img/image-20210223225257837.png)

根据这个构造方法，JDK Executors 类中提供了众多工厂方法来创建各种用途的线程池



---



### 5 不同场景的线程池

#### 1 newFixedThreadPool（任务队列无界）

创建一个核心线程个数和最大线程个数都为nThreads 的线程池，并且阻塞队列长度为 Integer.MAX_VALUE。 keepAliveTime=0 说明只要线程个数比核心线程个数多并且当前空闲则回收。

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
```

```java
public static void main(String[] args) {
    // ExecutorService threadPool = Executors.newFixedThreadPool(2);
    // 重新命名
    ExecutorService threadPool = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private AtomicInteger threadCount = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myPool_t_" + threadCount.getAndIncrement());
        }
    });
    for(int i = 0; i < 3; i++) {
        int index = i;
        threadPool.execute(() -> {
            log.info("{}", index);
        });
    }
}
```

特点:

* **核心线程数 == 最大线程数（没有救急线程被创建），因此也无需超时时间**
* **阻塞队列是无界的，可以放任意数量的任务**

评价：

* **适用于任务量已知，相对耗时的任务**

#### 2 newCachedThreadPool（线程数无界）

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```

特点：

* 核心线程数是 0， 最大线程数是 Integer.MAX_VALUE，救急线程的空闲生存时间是 60s，
    * **全部都是救急线程（60s 后可以回收）**
    * **救急线程可以无限创建**
* 队列采用了 **SynchronousQueue** 
    * 实现特点是，它没有容量，**没有线程来取是放不进去的**（一手交钱、一手交货）

评价：

* 整个线程池表现为线程数会根据任务量不断增长，没有上限，当任务执行完毕，空闲 1分钟后释放线程。
* **适合任务数比较密集，但每个任务执行时间较短的情况**

#### 3 newSingleThreadExecutor（任务队列无界）

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

使用场景：

* 希望多个任务排队执行。**线程数固定为 1，任务数多于 1 时，会放入无界队列排队**，任务执行完毕，这唯一的线程也不会被释放。

区别：

* **自己创建一个单线程串行执行任务，如果任务执行失败而终止那么没有任何补救措施，而线程池还会新建一个线程，保证池的正常工作**
* 与Executors.newFixedThreadPool(1)的区别？ 
    * Executors.newSingleThreadExecutor() **线程个数始终为1，不能修改**
        * FinalizableDelegatedExecutorService 应用的是装饰器模式，只对外暴露了 ExecutorService 接口，因此不能调用 ThreadPoolExecutor 中特有的方法
    * Executors.newFixedThreadPool(1) **初始时为1，以后还可以修改**
        * 对外暴露的是 ThreadPoolExecutor 对象，可以强转后调用 setCorePoolSize 等方法进行修改

#### 4 Timer

```java
/**
 * Timer 的优点在于简单易用，但由于所有任务都是由同一个线程来调度，因此所有任务都是串行执行的，
 * 同一时间只能有一个任务在执行，前一个任务的延迟或异常都将会影响到之后的任务。
 * 其中一个线程有错误也会耽误其他的线程
 */
public static void testTimer() {
    Timer timer = new Timer();
    TimerTask task1 = new TimerTask() {
        @Override
        public void run() {
            log.debug("task 1");
            sleep(2);
        }
    };
    TimerTask task2 = new TimerTask() {
        @Override
        public void run() {
            log.debug("task 2");
        }
    };
    // 使用 timer 添加两个任务，希望它们都在 1s 后执行
    // 但由于 timer 内只有一个线程来顺序执行队列中的任务，因此『任务1』的延时，影响了『任务2』的执行
    timer.schedule(task1, 1000);
    timer.schedule(task2, 1000);
}
```

---



#### 5 ScheduledExecutorService

```java
/**
 * 整个线程池表现为：
 * 线程数固定，任务数多于线程数时，会放入无界队列排队。
 * 任务执行完毕，这些线程也不会被释放, 用来执行延迟或反复执行的任务
 */
public static void testScheduled() {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    // 添加两个任务，它们都在 1s 后执行
    executor.schedule(() -> {
        System.out.println("任务1，执行时间：" + new Date());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }, 1000, TimeUnit.MILLISECONDS);

    executor.schedule(() -> {
        System.out.println("任务2，执行时间：" + new Date());
    }, 1000, TimeUnit.MILLISECONDS);
}

public static void testScheduled2() {
    ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    log.debug("start...");
    // pool.scheduleAtFixedRate(() -> {
    //     log.debug("running...");
    // }, 1, 1, TimeUnit.SECONDS);

    // 一开始，延时 1s，接下来，由于任务执行时间 > 间隔时间，间隔被『撑』到了 2s
    pool.scheduleAtFixedRate(() -> {
        log.debug("running...");
        sleep(2);
    }, 1, 1, TimeUnit.SECONDS);

    // 一开始，延时 1s，scheduleWithFixedDelay 的间隔是 上一个任务结束 <-> 延时 <-> 下一个任务开始 所以间隔都是 3s
    pool.scheduleWithFixedDelay(()-> {
        log.debug("running...");
        sleep(2);
    }, 1, 1, TimeUnit.SECONDS);
}
```

---

#### 6 ForkJoinPool

Fork/Join 是 JDK 1.7 加入的新的线程池实现，它体现的是一种**分治思想，适用于能够进行任务拆分的 cpu 密集型运算**

* 所谓的任务拆分，是将一个大任务拆分为算法上相同的小任务，直至不能拆分可以直接求解。
* 跟递归相关的一些计算，如归并排序、斐波那契数列、都可以用分治思想进行求解

**Fork/Join 在分治的基础上加入了多线程，可以把每个任务的分解和合并交给不同的线程来完成**，进一步提升了运算效率

* Fork/Join 默认会创建与 cpu 核心数大小相同的线程池

提交给 Fork/Join 线程池的任务，需要继承 ：

* RecursiveTask（有返回值）
* RecursiveAction（没有返回值）

```java
// 面定义了一个对 1~n 之间的整数求和的任务
public class ForkJoin {
    public static void main(String[] args) {
        // TODO: 想一想只启动一个线程去执行会不会饥饿？
        //  为什么不会饥饿？
        ForkJoinPool pool = new ForkJoinPool(2);
        System.out.println(pool.invoke(new AddTask2(1, 4)));
    }
}

@Slf4j
class AddTask2 extends RecursiveTask<Integer> {
    int begin, end;

    public AddTask2(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if(begin == end) return begin;

        int mid = begin + end >> 1;
        AddTask2 t1 = new AddTask2(begin, mid);
        t1.fork();
        AddTask2 t2 = new AddTask2(mid + 1, end);
        t2.fork();
        log.debug("fork() {} + {} = ?", t1, t2);

        int res = t1.join() + t2.join();
        log.debug("join() {} + {} = {}", t1, t2, res);
        return res;
    }

    @Override
    public String toString() {
        return "{" + begin + ", " + end + "}";
    }
}
```

---



### 6 向线程池中提交任务的方法

```java
// 执行任务
void execute(Runnable command);

// 提交任务 task，用返回值 Future 获得任务执行结果，callable接口有返回值
<T> Future<T> submit(Callable<T> task);

// 提交 tasks 中所有任务
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
throws InterruptedException;

// 提交 tasks 中所有任务，带超时时间
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
long timeout, TimeUnit unit)
throws InterruptedException;

// 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消
<T> T invokeAny(Collection<? extends Callable<T>> tasks)
throws InterruptedException, ExecutionException;
```

1. **`execute()`方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功与否；**
2. **`submit()`方法用于提交需要返回值的任务。
    1. **线程池会返回一个 `Future` 类型的对象，通过这个 `Future` 对象可以判断任务是否执行成功**，
    2. 并且可以通过 `Future` 的 `get()`方法来获取返回值，`get()`方法会阻塞当前线程直到任务完成，而使用 `get（long timeout，TimeUnit unit）`方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。



---



### 7 关闭线程池的方法

#### 1 shutdown

```Java
/*
线程池状态变为 SHUTDOWN
- 不会接收新任务
- 但已提交任务会执行完
- 此方法不会阻塞调用线程的执行
*/
void shutdown();
```

```Java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        // 修改线程池状态
        advanceRunState(SHUTDOWN);
        // 仅会打断空闲线程
        interruptIdleWorkers();
        onShutdown(); // 扩展点 ScheduledThreadPoolExecutor
     } finally {
    	mainLock.unlock();
     }
    // 尝试终结(没有运行的线程可以立刻终结，如果还有运行的线程也不会等)
    tryTerminate();
}
```

#### 2 shutdownNow

```Java
/*
线程池状态变为 STOP
- 不会接收新任务
- 会将队列中的任务返回
- 并用 interrupt 的方式中断正在执行的任务
*/
List<Runnable> shutdownNow();
```

```Java
public void shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        // 修改线程池状态
        advanceRunState(STOP);
        // 打断所有线程
        interruptWorkers();
        // 获取队列中剩余任务
        tasks = drainQueue();
     } finally {
        mainLock.unlock();
     }
    // 尝试终结
    tryTerminate();
    return tasks; 
}
```



#### 3 其他方法关闭线程

```java
// 不在 RUNNING 状态的线程池，此方法就返回 true
boolean isShutdown();
// 线程池状态是否是 TERMINATED
boolean isTerminated();
// 调用 shutdown 后，由于调用线程并不会等待所有任务运行结束，因此如果它想在线程池 TERMINATED 后做些事
情，可以利用此方法等待
boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
```



---



### 8 线程中抛出异常

```java
public static void testException() {
    ExecutorService pool = Executors.newFixedThreadPool(1);

    // 方法1：主动捉异常
    pool.submit(() -> {
        try {
            log.debug("task1");
            int i = 1 / 0;
        } catch (Exception e) {
            log.error("error:", e);
        }
    });

    // 方法2：使用 Future
    Future<Boolean> f = pool.submit(() -> {
        log.debug("task1");
        int i = 1 / 0;
        return true;
    });
    log.debug("result:{}", f.get());
}
```



### 9 线程池合适的线程大小

大小 不合适：

1. 过小会导致程序不能充分地利用系统资源、容易导致饥饿
2. 过大会导致更多的线程上下文切换，占用更多内存

**CPU 密集型运算**

* 通常采用 cpu 核数 + 1 能够实现最优的 CPU 利用率，+1 是保证当线程由于页缺失故障（操作系统）或其它原因导致暂停时，额外的这个线程就能顶上去，保证 CPU 时钟周期不被浪费

**I/O密集型运算**

* CPU 不总是处于繁忙状态，例如，当你执行业务计算时，这时候会使用 CPU 资源，但当你执行 I/O 操作时、远程RPC 调用时，包括进行数据库操作时，这时候 CPU 就闲下来了，你可以利用多线程提高它的利用率。

* 经验公式如下：

    * 线程数 = 核数 * 期望 CPU 利用率 * 总时间(CPU计算时间+等待时间) / CPU 计算时间

    * 例如 4 核 CPU 计算时间是 50% ，其它等待时间是 50%，期望 cpu 被 100% 利用，

        套用公式 4 * 100% * 100% / 50% = 8

    * 例如 4 核 CPU 计算时间是 10% ，其它等待时间是 90%，期望 cpu 被 100% 利用

        套用公式 4 * 100% * 100% / 10% = 40

        

---

# 七、JUC

## AQS 

AQS( AbstractQueuedSynchronizer ) 定义了一套**多线程访问共享资源**的同步器框架

AQS 核心思想

* 如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。
* 如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，
    * 这个机制 AQS 是用 CLH 队列锁实现的，即将暂时获取不到锁的线程加入到队列中。
    * CLH(Craig,Landin,and Hagersten)队列是一个虚拟的双向队列（虚拟的双向队列即不存在队列实例，仅存在结点之间的关联关系）。
    * AQS 是将每条请求共享资源的线程封装成一个 CLH 锁队列的一个结点（Node）来实现锁的分配。



主要用到AQS的并发工具类：

![image-20210225142916942](http://haoimg.hifool.cn/img/image-20210225142916942.png)



![image-20210315160452481](http://haoimg.hifool.cn/img/image-20210315160452481.png)

AQS特点：

* **用 state(volatile) 属性来表示需要被保护的资源的状态**（分独占模式和共享模式），子类需要定义如何维护这个状态，控制如何获取锁和释放锁
    1. **AQS的state状态值表示线程获取该锁的可重入次数**，某线程尝试加锁的时候通过CAS(CompareAndSwap)修改state值，如果成功设置为1，并且把当前线程ID赋值，则代表加锁成功，一旦获取到锁，其他的线程将会被阻塞进入阻塞队列自旋，获得锁的线程释放锁的时候将会唤醒阻塞队列中的线程，释放锁的时候则会把state重新置为0，同时当前线程ID置为空。
    2. getState - 获取 state 状态，
        1. state=0：表示当前锁没有被任何线程持有
        2. state=1：当一个线程第一次获取该锁时会尝试使用CAS设置state 的值为1，如果CAS 成功则当前线程获取了该锁，然后记录该锁的持有者为当前线程
            * 当state=1时，其他线程来加锁时则会失败，加锁失败的线程会被放`FIFO`的等待队列中，
            * 然后会被`UNSAFE.park()`操作挂起，等待其他获取锁的线程释放锁才能够被唤醒。
        3. state=2：在该线程没有释放锁的情况下第二次获取该锁后，状态值被设置为2 ， 这就是可重入次数。
    3. setState - 设置 state 状态
    4. compareAndSetState - CAS 机制设置 state 状态
    5. **独占模式是只有一个线程能够访问资源，而共享模式可以允许多个线程访问资源**
        1. **Exclusive（独占）**：只有一个线程能执行，如 ReentrantLock 。
            - 公平锁：按照线程在队列中的排队顺序，先到者先拿到锁
            - 非公平锁：当线程要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的
        2. **Share（共享）**：多个线程可同时执行，如` CountDownLatch`、`Semaphore`、`CountDownLatch`、 `CyclicBarrier`、`ReadWriteLock` 。
* **AQS提供了基于 FIFO 的等待队列**（内置同步队列称为“CLH”队列），多线程争用资源被阻塞时会进入此队列，类似于 Monitor 的 EntryList，来控制多个线程对共享变量的访问
    * 该队列由一个一个的Node结点组成，Node是对等待线程的封装，每个Node结点维护一个prev引用和next引用，分别指向自己的前驱和后继结点。
    * AQS维护两个指针，分别指向队列头部head和尾部tail。
    * **Node节点状态**：
        * **CANCELLED**(1)：表示当前结点已取消调度。当timeout或被中断（响应中断的情况下），会触发变更为此状态，进入该状态后的结点将不会再变化。
        * **SIGNAL**(-1)：表示后继结点在等待当前结点唤醒。后继结点入队时，会将前继结点的状态更新为SIGNAL。
        * **CONDITION**(-2)：表示结点等待在Condition上，当其他线程调用了Condition的signal()方法后，CONDITION状态的结点将**从等待队列转移到同步队列中**，等待获取同步锁。
        * **PROPAGATE**(-3)：共享模式下，前继结点不仅会唤醒其后继结点，同时也可能会唤醒后继的后继结点。
        * **0**：新结点入队时的默认状态。
        * **负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常。**
* AQS的条件变量来实现等待、唤醒机制，支持多个条件变量，类似于 Monitor 的 WaitSet

### AQS子类需要实现的主要方法

（默认抛出 UnsupportedOperationException）

```java
// 独占式获取同步状态，试着获取，成功返回true，反之为false 
protected boolean tryAcquire(int arg); 

// 独占式释放同步状态，等待中的其他线程此时将有机会获取到同步状态 
protected boolean tryRelease(int arg); 

// 共享式获取同步状态，返回值大于等于0，代表获取成功；反之获取失败 
protected int tryAcquireShared(int arg); 

// 共享式释放同步状态，成功为true，失败为false 
protected boolean tryReleaseShared(int arg); 

// 是否在独占模式下被线程占用 
protected boolean isHeldExclusively();
```



### 不可重入锁实现

#### 1 自定义同步器

对于使用者来讲，我们无需关心获取资源失败，线程排队，线程阻塞/唤醒等一系列复杂的实现，这些都在AQS中为我们处理好了。

* 我们只需要负责好自己的那个环节就好，也就是获取/释放共享资源state的姿势。
* 很经典的模板方法设计模式的应用，AQS为我们定义好顶级逻辑的骨架，并提取出公用的线程入队列/出队列，阻塞/唤醒等一系列复杂逻辑的实现，将部分简单的可由使用者决定的操作逻辑延迟到子类中去实现即可。

```java
public class MySynchronizer extends AbstractQueuedSynchronizer {
    @Override
    protected boolean tryAcquire(int acquires) {
        if (acquires == 1) {
            if (compareAndSetState(0, 1)) { // 通过CAS方式进行修改，这里修改的是state吗？
                setExclusiveOwnerThread(Thread.currentThread()); // 设置为Owner
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean tryRelease(int acquires) {
        if (acquires == 1) {
            if(getState() == 0) { // state 是 volatile 类型的变量;
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            // TODO：这里有一个细节，state是volatile类型的变量，
            //  这里对volatile变量进行修改后，会有一个写屏障，前面所有对共享变量对修改都会写入到内存区
            setState(0);
            return true;
        }
        return false;
    }

    // 判断是否是独占锁
    @Override
    protected boolean isHeldExclusively() {
        return getState() == 1;
    }

    protected Condition newCondition() {
        return new ConditionObject();
    }
}
```

#### 2 自定义不可重入锁

```java
class MyLock implements Lock {
    static MySynchronizer sync = new MySynchronizer();

    @Override
    // 尝试，不成功，进入等待队列
    public void lock() {
        sync.acquire(1);
    }

    @Override
    // 尝试，不成功，进入等待队列，可打断
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    // 尝试一次，不成功返回，不进入队列
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    // 尝试，不成功，进入等待队列，有时限
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    // 释放锁
    public void unlock() {
        sync.release(1);
    }

    @Override
    // 生成条件变量
    public Condition newCondition() {
        return sync.newCondition();
    }
}
```

#### 3 测试

```java
public class AQSLockExample {
    public static void main(String[] args) {
        MyLock lock = new MyLock();
        new Thread(() -> {
            lock.lock();
            lock.lock();
            try {
                log.debug("locking...");
                sleep(1);
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        },"t1").start();
        new Thread(() -> {
            lock.lock();
            try {
                log.debug("locking...");
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        },"t2").start();
    }
}
```

### 互斥(独占)模式

#### 1 acquire()

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

![image-20210225143050326](http://haoimg.hifool.cn/img/image-20210225143050326.png)

  1. 调用自定义同步器的tryAcquire()尝试直接去获取资源，
       1. 如果成功则直接返回；
 2. 如果没成功，则addWaiter()将该线程加入等待队列的尾部，并标记为独占模式；
 3. acquireQueued()使线程在等待队列中休息，有机会时（轮到自己，会被unpark()）会去尝试获取资源。获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。 
 4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。

#### 2 release()

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

1. 调用自定义同步器的tryRelease()尝试释放资源
    1. 如果释放失败直接返回false
    2. 如果释放资源成功，根据等待队列中的 node节点的waitStatus，调用unparkSuccessor()方法，唤醒等待队列中下一个需要唤醒的线程。

### 共享模式

共享式地获取同步状态。

* 对于独占式同步组件来讲，同一时刻只有一个线程能获取到同步状态，其他线程都得去排队等待，其待重写的尝试获取同步状态的方法tryAcquire返回值为boolean，这很容易理解；
* 对于共享式同步组件来讲，同一时刻可以有多个线程同时获取到同步状态，这也是“共享”的意义所在。其待重写的尝试获取同步状态的方法tryAcquireShared返回值为int。
    * 当返回值大于0时，表示获取同步状态成功，同时还有剩余同步状态可供其他线程获取；
    * 当返回值等于0时，表示获取同步状态成功，但没有可用同步状态了；
    * 当返回值小于0时，表示获取同步状态失败。

#### 1 acquireShared()

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```

```java
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

1. 调用自定义同步器的tryAcquireShared()方法获取资源。
    1. tryAcquireShared() 返回值小于0，获取资源失败，进入排队。
2. addWaiter()将该线程加入等待队列的尾部，并标记为SHARED模式
3. 然后独占式的acquireQueued差别不大，区别在于排队中的老二获取到同步状态时，如果有可用的资源，会继续传播下去。



#### 2 releaseShared()

```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
```

```java
private void doReleaseShared() {
    /*
     * Ensure that a release propagates, even if there are other
     * in-progress acquires/releases.  This proceeds in the usual
     * way of trying to unparkSuccessor of head if it needs
     * signal. But if it does not, status is set to PROPAGATE to
     * ensure that upon release, propagation continues.
     * Additionally, we must loop in case a new node is added
     * while we are doing this. Also, unlike other uses of
     * unparkSuccessor, we need to know if CAS to reset status
     * fails, if so rechecking.
     */
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;            // loop to recheck cases
                unparkSuccessor(h);
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        if (h == head)                   // loop if head changed
            break;
    }
}
```

共享模式，释放同步状态也是多线程的，此处采用了CAS自旋来保证。



## ReentrantLock可重入锁

ReentrantLock 与 synchronized 一样，都支持可重入

* 可重入锁：同一个线程如果首次获得了这把锁，那么因为它是这把锁的拥有者，因此有权利再次获取这把锁
* 不可重入锁：同一个线程在第二次尝试获得锁时，该线程自己也会被锁挡住

```java
// 获取锁
reentrantLock.lock();
    try {
    	// 临界区
    } finally {
    	// 释放锁
    	reentrantLock.unlock();
}
```

### Lock 接口

```java
// 获取锁
void lock();

// 支持中断的API，通过这个方法去获取锁时，如果线程正在等待获取锁，则这个线程能够响应中断，即中断线程的等待状态。
void lockInterruptibly() throws InterruptedException;

// 支持超时的API,在等待时间内获取到锁返回true，超时返回false
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

// 支持非阻塞获取锁的API
boolean tryLock();

// 释放锁，一定要在finally块中释放
void unlock();

Condition newCondition();
```



#### 1 Lock 和 synchronized 区别

* **Lock是一个接口**，而**synchronized是Java中的关键字**，synchronized是内置的语言实现；
* **synchronized 在发生异常时，会自动释放线程占有的锁**，因此不会导致死锁现象发生；
    * 而**Lock**在发生异常时，**如果没有主动通过unLock() 去释放锁，则很可能造成死锁现象**，因此使用Lock时需要在finally块中释放锁；
* **Lock可以让等待锁的线程响应中断**，而**synchronized却不响应中断**，
    * 使用synchronized时，等待的线程会一直等待下去，不能够响应中断；
* **通过Lock可以知道有没有成功获取锁**，而synchronized却无法办到。
    * 得知没有成功获取到锁后，可以直接放回，而不进入阻塞状态，返回后线程有机会释放曾经持有的锁，也能破坏不可抢占条件。
* **Lock可以提高多个线程进行读操作的效率**。（可以通过readwritelock 实现读写分离）
* 性能上来说，**在资源竞争不激烈的情形下，Lock性能稍微比synchronized差点**（编译程序通常会尽可能的进行优化synchronized）。
    * 但是**当同步非常激烈的时候，synchronized的性能一下子能下降好几十倍**。而ReentrantLock 确还能维持常态。



#### 2 Lock 相比 synchronized 的优点

1. **支持中断**
    * synchronized 的问题是，持有锁 A 后，如果尝试获取锁 B 失败，那么线程就进入阻塞状态，一旦发生死锁，就没有任何机会来唤醒阻塞的线程。
    * 但如果阻塞状态的线程能够响应中断信号，也就是说当我们给阻塞的线程发送中断信号的时候，能够唤醒它，那它就有机会释放曾经持有的锁 A。这样就破坏了不可抢占条件了
2. **支持超时**
    * 如果线程在一段时间之内没有获取到锁，不是进入阻塞状态，而是返回一个错误，那这个线程也有机会释放曾经持有的锁。这样也能破坏不可抢占条件。
3. **非阻塞地获取锁**
    * **如果尝试获取锁失败，并不进入阻塞状态，而是直接返回**，那这个线程也有机会释放曾经持有的锁。这样也**能破坏不可抢占条件**。



### ReentrantLock 相比 synchronized 优点：

* ReentrantLock **可中断**
    * synchronized 的问题是，持有锁 A 后，如果尝试获取锁 B 失败，那么线程就进入阻塞状态，一旦发生死锁，就没有任何机会来唤醒阻塞的线程。
    * reentrantLock.lockInterruptibly(); 在等待锁的过程中，该锁会被中断，并不会一直处于等待状态
    * 注意如果是不可中断模式，那么即使使用了 interrupt 也不会让等待中断
* ReentrantLock **可以设置超时时间**
    * 如果线程在一段时间之内没有获取到锁，不是进入阻塞状态，而是返回一个错误，那这个线程也有机会释放曾经持有的锁。这样也能破坏不可抢占条件。
* ReentrantLock  **可以设置为公平锁**
    * 每个等待锁的线程会进入一个FIFO队列，都有机会获取到该锁
    * ReentrantLock 默认是不公平的，但公平锁的并发性很低，不公平锁可以通过tryLock() 失败时返回结果的方式，让每个等待锁的线程都可以获得到锁
* ReentrantLock **支持多个条件变量**
    * synchronized只支持单个条件变量，不满足条件的线程都在一间休息室等消息 
    * ReentrantLock 支持多间休息室，有专门等烟的休息室、专门等早餐的休息室、唤醒时也是按休息室来唤醒
    * 使用要点：
        * await 前需要获得锁
        * await 执行后，会释放锁，进入 conditionObject 等待
        * await 的线程被唤醒（或打断、或超时）取重新竞争 lock 锁
        * 竞争 lock 锁成功后，从 await 后继续执行

### 非公平锁原理

#### 1 加锁&解锁流程

[源码解析](https://mp.weixin.qq.com/s/trsjgUFRrz40Simq2VKxTA)

#### 2 加锁源码

#### 3 解锁源码

### 可重入原理

### 可打断原理

#### 1 可打断模式

#### 2 不可打断模式

### 公平锁原理



### 条件变量实现原理

#### 1 await 流程

#### 2 signal 流程

## ReentrantReadWriteLock 读写锁

当读操作远远高于写操作时，这时候使用 读写锁 让 读-读 可以并发，提高性能。 类似于数据库中的 select ...from ... lock in share mode

* 提供一个 数据容器类 内部分别使用，读锁保护数据的 read() 方法，写锁保护数据的 write() 方法
* 读锁-读锁 可以并发
* 读锁-写锁 相互阻塞
* 写锁-写锁 也是相互阻塞的

注：

* 读锁不支持条件变量
* 重入时：
    * 重入时升级不支持：持有读锁的情况下去获取写锁，会导致获取写锁永久等待
    * 重入时降级支持：即持有写锁的情况下去获取读锁

```java
class CachedData {
    Object data;
    // 是否有效，如果失效，需要重新计算 data
    volatile boolean cacheValid;
    final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    void processCachedData() {
        rwl.readLock().lock();
        if (!cacheValid) {
            // 获取写锁前必须释放读锁
            rwl.readLock().unlock();
            // 释放完读锁，才能释放写锁
            rwl.writeLock().lock();
            try {
                // 判断是否有其它线程已经获取了写锁、更新了缓存, 避免重复更新
                if (!cacheValid) {
                    // data = ...;
                    cacheValid = true;
                }
                // 获取写锁后，可以获取读锁
                // 降级为读锁, 释放写锁, 这样能够让其它线程读取缓存
                rwl.readLock().lock();
            } finally {
                rwl.writeLock().unlock();
            }
        }
        // 自己用完数据, 释放读锁
        try {
            // use(data);
        } finally {
            rwl.readLock().unlock();
        }
    }
}
```



### 应用：一致性缓存

任务：将数据库中的数据存到缓存中，下次查询不用绕道去数据库中查询了，直接在缓存中查询就好了

缓存更新策略：

* 不加锁的情况下对缓存进行操作，无论是先更新数据库，还是先清理缓存，怎么着都会有问题



1. 先清理缓存，再更新数据库：

![image-20210227185103618](http://haoimg.hifool.cn/img/image-20210227185103618.png)



2. 先更新数据库，再清理缓存

![image-20210227185243215](http://haoimg.hifool.cn/img/image-20210227185243215.png)



![image-20210227185254387](http://haoimg.hifool.cn/img/image-20210227185254387.png)



必须使用读写锁，完成实现缓存一致性的需求

以下实现体现的是读写锁的应用，保证缓存和数据库的一致性，但有下面的问题没有考虑

* 适合读多写少，如果写操作比较频繁，以上实现性能低
* 没有考虑缓存容量
* 没有考虑缓存过期
* 只适合单机
* 并发性还是低，目前只会用一把锁
* 更新方法太过简单粗暴，清空了所有 key（考虑按类型分区或重新设计 key）
* 乐观锁实现：用 CAS 去更新

```java
class GenericCachedDao<T> {
    // HashMap 作为缓存非线程安全, 需要保护
    HashMap<SqlPair, T> map = new HashMap<>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    GenericDao genericDao = new GenericDao();

    public int update(String sql, Object... params) {
        SqlPair key = new SqlPair(sql, params);
        // 加写锁, 防止其它线程对缓存读取和更改
        lock.writeLock().lock();
        try {
            int rows = genericDao.update(sql, params);
            map.clear();
            return rows;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T queryOne(Class<T> beanClass, String sql, Object... params) {
        SqlPair key = new SqlPair(sql, params);
        // 加读锁, 防止其它线程对缓存更改
        lock.readLock().lock();
        try {
            T value = map.get(key);
            if (value != null) {
                return value;
            }
        } finally {
            lock.readLock().unlock();
        }

        // 加写锁, 防止其它线程对缓存读取和更改
        lock.writeLock().lock();
        try {
            // 注意！！！get 方法上面部分是可能多个线程进来的, 可能已经向缓存填充了数据
            // 为防止重复查询数据库, 再次验证
            T value = map.get(key);
            if (value == null) {
                // 如果没有, 查询数据库
                value = (T) genericDao.queryOne(beanClass, sql, params);
                map.put(key, value);
            }
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

### 原理：



### StampleLock

该类自 JDK 8 加入，是为了进一步优化读性能，它的特点是在使用读锁、写锁时都必须配合【戳】使用

1. 加、解读锁

```java
long stamp = lock.readLock();
lock.unlockRead(stamp);	
```

2. 加、解写锁

```java
long stamp = lock.writeLock();
lock.unlockWrite(stamp);
```



乐观读，StampedLock 支持 tryOptimisticRead() 方法（乐观读），读取完毕后需要做一次 戳校验 如果校验通过，表示这期间确实没有写操作，数据可以安全使用，如果校验没通过，需要重新获取读锁，保证数据安全。

```java
long stamp = lock.tryOptimisticRead();

// 验戳
if(!lock.validate(stamp)){
	// 锁升级
}
```



案例：提供一个 数据容器类 内部分别使用读锁保护数据的 read() 方法，写锁保护数据的 write() 方法

```java
class DataContainerStamped {
    private int data;
    private final StampedLock lock = new StampedLock();

    public DataContainerStamped(int data) {
        this.data = data;
    }

    public int read(int readTime) {
        long stamp = lock.tryOptimisticRead();
        log.debug("optimistic read locking...{}", stamp);
        sleep(readTime);
        if (lock.validate(stamp)) {
            log.debug("read finish...{}, data:{}", stamp, data);
            return data;
        }
        // 锁升级 - 读锁
        log.debug("updating to read lock... {}", stamp);
        try {
            stamp = lock.readLock();
            log.debug("read lock {}", stamp);
            sleep(readTime);
            log.debug("read finish...{}, data:{}", stamp, data);
            return data;
        } finally {
            log.debug("read unlock {}", stamp);
            lock.unlockRead(stamp);
        }
    }
    
    public void write(int newData) {
        long stamp = lock.writeLock();
        log.debug("write lock {}", stamp);
        try {
            sleep(2);
            this.data = newData;
        } finally {
            log.debug("write unlock {}", stamp);
            lock.unlockWrite(stamp);
        }
    }
}
```



---



## Semaphore

* **Semaphore 类似于操作系统中的信号量，可以控制对互斥共享资源的访问线程数**。
    * 使用 Semaphore 限流，在访问高峰期时，让请求线程阻塞，高峰期过去再释放许可，
    * 当然它只适合限制单机线程数量，并且仅是限制线程数，而不是限制资源数（例如连接数，请对比 Tomcat LimitLatch 的实现）
* 用 Semaphore 实现简单连接池，对比『享元模式』下的实现（用wait notify），性能和可读性显然更好，注意下面的实现中线程数和数据库连接数是相等的



![image-20210227201616410](http://haoimg.hifool.cn/img/image-20210227201616410.png)



信号中包括一个整形变量，和两个原子操作 P 和 V。其原子性由操作系统保证，这个整形变量只能通过P 操作和 V 操作改变。

* P：信号量值减 1，如果信号量值小于 0，则说明资源不够用的，把进程加入等待队列
* V：信号量值加 1，如果信号量值小于等于 0，则说明等待队列里有进程，那么唤醒一个等待进程。

用信号量实现阻塞队列如下(Java Semaphore模拟）：

1. 任何时刻只能有一个线程操作缓存区：互斥访问，使用二进制信号量 mutex，其信号初始值为 1。
2. 缓存区空时，消费者必须等待生产者：条件同步，使用资源信号量 notEmpty，其信号初始值为0。
3. 缓存区满时，生产者必须等待消费者：条件同步，使用资源信号量 notFull，其信号初始值为 n。

```java
public class SemaphoreExample {
    private int n = 100;
    private Semaphore mutex = new Semaphore(1);
    private Semaphore notFull = new Semaphore(n);
    private Semaphore notEmpty = new Semaphore(0);

    public void product() throws InterruptedException {
        notFull.acquire(); // 缓冲区满时，生产者线程必须等待
        mutex.acquire();
        // ...
        mutex.release();
        notEmpty.release(); // 唤醒等待的消费者线程
    }

    public void consume() throws InterruptedException {
        notEmpty.acquire(); // 缓冲区空时，消费都线程等待
        mutex.acquire();
        // ...
        mutex.release();
        notFull.release(); // 唤醒等待的生产者线程
    }
}
```



### 管程（Monitor）

![image-20210227202440157](http://haoimg.hifool.cn/img/image-20210227202440157.png)



Monitor 直译过来就是 "监视器"，操作系统领域一般都翻译成 "管程"。

* 所谓管程，指的是**管理共享变量以及对共享变量的操作过程，让他们支持并发**。

在管程的发展史上，先后出现过三种不同的管程模型，分别是：Hasen 模型、Hoare 模型和 MESA 模型。其中，现在广泛应用的是 MESA 模型

在上述用信号量实现生产者-消费者模式的代码中，为了实现阻塞队列的功能，即等待-通知(waitnotify)，除了使用互斥锁 mutex 外，还需要两个判断队满和队空的资源信号量 notfull 和 notEmpty，使用起来不仅复杂，还容易出错。

管程实现阻塞队列如下：

1. 对于入队操作，如果队列已满，就需要等待直到队列不满，即 notFull.await();。
2. 对于出队操作，如果队列为空，就需要等待直到队列不空，即 notEmpty.await();。
3. 如果入队成功，那么队列就不空了，就需要通知条件变量：队列不空 notEmpty 对应的等待队列。
4. 如果出队成功，那就队列就不满了，就需要通知条件变量：队列不满 notFull 对应的等待队列。

```java
class ConditionTest<T> {
    final Lock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    void enq(T x) {
        lock.lock();
        try {
            while (队列已满) {
                notFull.await();
            }
            //入队后，通知可出队
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    void deq() {
        lock.lock();
        try {
            while (队列已空) {
                notEmpty.await();
            }
            // 出队后，通知可入队
            notFull.signal();
        } finally {
            lock.unlock();
        }
    
    }
}
```



### 应用：连接池

```java
public class ConnectionPool_Semaphore {

    // 1. 连接池大小
    private final int poolSize;

    // 2. 连接对象数组
    private Connection[] connections;

    // 3. 连接状态数组 0 表示空闲， 1 表示繁忙
    private AtomicIntegerArray states;

    private Semaphore semaphore;

    // 4. 构造方法初始化
    public ConnectionPool_Semaphore(int poolSize) {
        this.poolSize = poolSize;
		// 让许可数与资源数一致
        this.semaphore = new Semaphore(poolSize);
        this.connections = new Connection[poolSize];
        this.states = new AtomicIntegerArray(new int[poolSize]);
        for (int i = 0; i < poolSize; i++) {
            connections[i] = new MockConnection("连接" + (i + 1));
        }
    }

    // 5. 借连接
    public Connection borrow() {// t1, t2, t3
        // 获取许可
        try {
            semaphore.acquire(); // 没有许可的线程，在此等待
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < poolSize; i++) {
            // 获取空闲连接
            if (states.get(i) == 0) {
                if (states.compareAndSet(i, 0, 1)) {
                    log.debug("borrow {}", connections[i]);
                    return connections[i];
                }
            }
        }
        // 不会执行到这里
        return null;
    }

    // 6. 归还连接
    public void free(Connection conn) {
        for (int i = 0; i < poolSize; i++) {
            if (connections[i] == conn) {
                states.set(i, 0);
                log.debug("free {}", conn);
                semaphore.release();
                break;
            }
        }
    }
}
```

以上实现没有考虑：

* 连接的动态增长与收缩
* 连接保活（可用性检测）
* 等待超时处理
* 分布式 hash
* 对于关系型数据库，有比较成熟的连接池实现，例如c3p0, druid等 对于更通用的对象池，可以考虑使用apachecommons pool，例如redis连接池可以参考jedis中关于连接池的实现



### 原理：



## CountdownLatch

**用来进行线程同步协作，等待所有线程完成倒计时。**

维护了一个计数器 cnt，其中构造参数用来初始化等待计数值，

* 每次调用 countDown() 方法会让计数器的值减 1，
* 减到 0 的时候，那些因为调用 await() 方法而在等待的线程就会被唤醒。



### 1 CountDownLatch 的缺点：

1. 没有返回值

    1. 需要有返回值的时候需要用 Future

        ```
        Future<T> f1 = service.submit(() -> {return "task1"});
        Future<T> f2 = service.submit(() -> {return "task2"});
        f1.get();
        f2.get();
        ```

2. 不能重用

    1. 需要重用需要用CyclicBarrier



### 2 CountDownLatch 的原理

CountDownLatch 内部持有一个Sync对象。

* Sync类实现AQS类， state（volatile） 值即为计数器的计数值。
* countDown() 方法把state减1
* await() 方法判断state值是否为0，否则阻塞。

```java

public class CountDownLatch {
    
    private static final class Sync extends AbstractQueuedSynchronizer {
    }

    private final Sync sync;

    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        sync.releaseShared(1);
    }

    public long getCount() {
        return sync.getCount();
    }
}

```





应用：10个人等待游戏开始

```java
public class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger num = new AtomicInteger(1);
        ExecutorService service = Executors.newFixedThreadPool(10, (r) -> {
            return new Thread(r, "t" + num.getAndIncrement());
        });

        CountDownLatch latch = new CountDownLatch(10);
        String[] all = new String[10];
        Random r = new Random();
        for (int j = 0; j < 10; j++) {
            int x = j;
            service.submit(() -> {
                for (int i = 0; i <= 100; i++) {
                    try {
                        Thread.sleep(r.nextInt(100));
                    } catch (InterruptedException e) {
                    }
                    all[x] = Thread.currentThread().getName() + "(" + (i + "%") + ")";
                    System.out.print("\r" + Arrays.toString(all));
                }
                latch.countDown();
            });
        }
        latch.await();
        System.out.println("\n游戏开始...");
        service.shutdown();
    }
}
```



## CyclicBarrier

**循环栅栏，用来控制多个线程互相等待，只有当多个线程都到达屏障时，这些线程才会继续执行。**

* 和 CountdownLatch 相似，都是通过维护计数器来实现的。
    * 线程执行 await( ) 方法之后计数器会减 1，并进行等待，
    * **直到计数器为 0，所有调用 await() 方法而在等待的线程才能继续执行**。
* 构造时设置『计数个数』，每个线程执行到某个需要“同步”的时刻调用 await() 方法进行等待，当等待的线程数满足『计数个数』时，继续执行。
* CyclicBarrier 与 CountDownLatch 的主要区别在于
    * **CountDownLatch 是一个线程等待其他线程， CyclicBarrier 是多个线程互相等待。**
    * **CyclicBarrier 的计数器通过调用 reset() 方法可以循环使用**，所以它才叫做循环屏障。
* **CyclicBarrier 与线程池的大小要设置成一样的，否则容易出现错误**



CyclicBarrier 有两个构造函数

* 其中 parties 指示计数器的初始值
* barrierAction 在所有线程都到达屏障的时候会执行一次

```java
public CyclicBarrier(int parties) ;
public CyclicBarrier(int parties, Runnable barrierAction);
```





应用：

```java
public class CyclicBarrierExample {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2, () -> {
            log.info("---------task1 task2 end-------");
        }); // 个数为2时才会继续执行, 最后的函数是结束时执行
        
        for (int i = 0; i < 3; i++) {
            executorService.submit( () -> {
                log.info("线程1开始..");
                try {
                    cyclicBarrier.await(); // 当个数不足时，等待
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });

            executorService.submit( ()->{
                log.info("线程2开始..");
                try { Thread.sleep(2000); } catch (InterruptedException e) { }
                try {
                    cyclicBarrier.await(); // 2 秒后，线程个数够2，继续运行
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
    }
}
```



## 线程安全类集合

![image-20210227220948070](http://haoimg.hifool.cn/img/image-20210227220948070.png)



线程安全集合类可以分为三大类：

* **遗留的线程安全集合**如 Hashtable ， Vector
* **使用 Collections 装饰的线程安全集合**，如：
    * Collections.synchronizedCollection
    * Collections.synchronizedList
    * Collections.synchronizedMap
    * Collections.synchronizedSet
    * Collections.synchronizedNavigableMap
    * Collections.synchronizedNavigableSet
    * Collections.synchronizedSortedMap
    * Collections.synchronizedSortedSet
* **重点：java.util.concurrent**.*
    * 三类关键词：Blocking、CopyOnWrite、Concurrent
    * **Blocking** 大部分实现基于锁，并提供用来阻塞的方法
    * **CopyOnWrite** 之类容器，适用于读多写少的情况，他的修改开销相对较重
    * **Concurrent 类型的容器（建议使用**）
        * **内部很多操作使用 cas 优化**，**一般可以提供较高吞吐量**
        * **弱一致性 （他们的普遍缺点）**
            * **遍历时弱一致性**，例如，当利用迭代器遍历时，如果容器发生修改，迭代器仍然可以继续进行遍历，这时内容是旧的
            * **求大小弱一致性**，size 操作未必是 100% 准确
            * **读取弱一致性**
            * 遍历时如果发生了修改，**对于非安全容器来讲，使用 fail-fast 机制也就是让遍历立刻失败**，抛出ConcurrentModificationException，不再继续遍历

 

## ConcurrentHashMap

### 1 重要属性和内部类

```java
// 默认为 0
// 当初始化时, 为 -1
// 当扩容时, 为 -(1 + 扩容线程数)
// 当初始化或扩容完成后，为 下一次的扩容的阈值大小
private transient volatile int sizeCtl;

// 整个 ConcurrentHashMap 就是一个 Node[]
static class Node<K,V> implements Map.Entry<K,V> {}

// hash 表
transient volatile Node<K,V>[] table;

// 扩容时的 新 hash 表
private transient volatile Node<K,V>[] nextTable;

// 扩容时如果某个 bin 迁移完毕, 用 ForwardingNode 作为旧 table bin 的头结点
// 	1 ConcurrentHashMap 在扩容时是线程安全的，当一个线程对一个Node完成了复制工作，其他线程来了看到这个标志就不用再进行重复的复制了
// 	2 ConcurrentHashMap 在get时，在oldtable上发现该Node时ForwardingNode，则取nextTable上去找
static final class ForwardingNode<K,V> extends Node<K,V> {}

// 用在 compute 以及 computeIfAbsent 时, 用来占位, 计算完成后替换为普通 Node
static final class ReservationNode<K,V> extends Node<K,V> {}

// 红黑树
// 作为 treebin 的头节点, 存储 root 和 first
static final class TreeBin<K,V> extends Node<K,V> {}

// 作为 treebin 的节点, 存储 parent, left, right
static final class TreeNode<K,V> extends Node<K,V> {}
```



### 2 重要方法

```java
// 获取 Node[] 中第 i 个 Node
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i)

// cas 修改 Node[] 中第 i 个 Node 的值, c 为旧值, v 为新值
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i, Node<K,V> c, Node<K,V> v)

// 直接修改 Node[] 中第 i 个 Node 的值, v 为新值
static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v)
```



###  3 构造器分析

可以看到实现了懒惰初始化，在构造方法中仅仅计算了 table 的大小，以后在第一次使用时才会真正创建

```java
public ConcurrentHashMap(int initialCapacity,
                         float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
        throw new IllegalArgumentException();
    if (initialCapacity < concurrencyLevel)   // Use at least as many bins
        initialCapacity = concurrencyLevel;   // as estimated threads
    long size = (long)(1.0 + (long)initialCapacity / loadFactor);
    // tableSizeFor 仍然是保证计算的大小是 2^n, 即 16,32,64 ...
    int cap = (size >= (long)MAXIMUM_CAPACITY) ?
        MAXIMUM_CAPACITY : tableSizeFor((int)size);
    this.sizeCtl = cap;
}
```



### 4 get

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    // spread 方法能确保返回结果是正数
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (e = tabAt(tab, (n - 1) & h)) != null) {
        // 如果头结点已经是要查找的 key
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;
        }
        else if (eh < 0) // hash 为负数表示该 bin 在扩容中 或 是 treebin, 这时调用 find 方法来查找
            return (p = e.find(h, key)) != null ? p.val : null;
        // 正常遍历链表, 用 equals 比较
        while ((e = e.next) != null) {
            if (e.hash == h &&
                ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```



### 5 putVal

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}
```

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    // 其中 spread 方法会综合高位低位, 具有更好的 hash 性
    int hash = spread(key.hashCode());
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        // f 是链表头节点
		// fh 是链表头结点的 hash
		// i 是链表在 table 中的下标
        Node<K,V> f; int n, i, fh;
        // 懒惰式初始化，要创建 table
        if (tab == null || (n = tab.length) == 0)
            // 初始化 table 使用了 cas, 无需 synchronized 创建成功, 进入下一轮循环
            tab = initTable();
        // 要创建链表头节点
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // 添加链表头使用了 cas, 无需 synchronized
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        // 处在扩容状态中，需要帮忙扩容
        else if ((fh = f.hash) == MOVED)
            // 帮忙之后, 进入下一轮循环
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            // 锁住链表头节点
            synchronized (f) {
                // 双重检查，再次确认链表头节点没有被移动
                if (tabAt(tab, i) == f) {
                    // 链表
                    if (fh >= 0) {
                        binCount = 1;
                        // 遍历链表
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 找到相同的 key
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                // 更新
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            // 已经是最后的节点了, 新增 Node, 追加至链表尾
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    // 红黑树
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        // putTreeVal 会看 key 是否已经在树中, 是, 则返回对应的 TreeNode
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
                // 释放链表头节点的锁
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    // 如果链表长度 >= 树化阈值(8), 进行链表转为红黑树
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 增加 size 计数，这里也涉及到了多线程的问题
    addCount(1L, binCount);
    return null;
}
```



### 6 initTable

```java
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        if ((sc = sizeCtl) < 0)
            Thread.yield(); // lost initialization race; just spin
        // 尝试将 sizeCtl 设置为 -1（表示初始化 table）
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            // 获得锁, 创建 table, 这时其它线程会在 while() 循环中 yield 直至 table 创建
            try {
                if ((tab = table) == null || tab.length == 0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    sc = n - (n >>> 2);
                }
            } finally {
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```





























