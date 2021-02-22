# 背景

## 1 什么是进程和线程？

### 1 什么是进程？

* 程序：一段静态的代码，一组指令的有序集合，它本身没有任何运行的含义。
* 进程：是程序的一次执行过程，是系统运行程序时进行资源分配和调度的基本单位，因此进程是动态的。
    * 进程对应着从代码加载，执行至执行完毕的一个完整的过程，是一个动态的实体，它有自己的生命周期。它因创建而产生，因调度而运行，因等待资源或事件而被处于等待状态，因完成任务而被撤消。
    * 进程反映了一个程序在一定的数据集上运行的全部动态过程。通过进程控制块(PCB)唯一的标识某个进程。同时进程占据着相应的资源（例如包括cpu的使用 ，轮转时间以及一些其它设备的权限）。
* 例：在 Java 中，当启动 main 函数时其实就是启动了一个 JVM 的进程，而 main 函数所在的线程就是这个进程中的一个线程，也称主线程。

### 2 什么是线程？

* 线程与进程相似，但线程是一个比进程更小的执行单位。一个进程在其执行的过程中可以产生多个线程。
    * 与进程不同的是，同进程下的多个线程共享进程的**堆**和**方法区**等内存空间资源，但每个线程有自己的**程序计数器**、**虚拟机栈**和**本地方法栈**，所以系统在产生一个线程，或是在各个线程之间作切换工作时，负担要比进程小得多，也正因为如此，线程也被称为轻量级进程。

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

## 2 进程 和 线程的联系、区别？

**根本区别**：

* 进程是操作系统**资源分配**的基本单位，
* 而线程是**处理器任务调度和执行**的基本单位

**资源开销**：

* 每个进程都有独立的代码和数据空间（程序上下文），程序之间的切换会有较大的开销；
* 线程可以看做轻量级的进程，同一类线程共享代码和数据空间，每个线程都有自己独立的运行栈和程序计数器（PC），线程之间切换的开销小。

**包含关系**：

* 如果一个进程内有多个线程，则执行过程不是一条线的，而是多条线（线程）共同完成的；
* 线程是进程的一部分，所以线程也被称为轻权进程或者轻量级进程。

**内存分配**：

* 同一进程的线程共享本进程的地址空间和资源
* 进程之间的地址空间和资源是相互独立的

**影响关系**：

* 多进程为什么比多线程健壮？
* 一个进程崩溃后，在保护模式下不会对其他进程产生影响
* 一个线程崩溃整个进程都死掉。

**执行过程**：

* 每个独立的进程有程序运行的入口、顺序执行序列和程序出口。
* 但是线程不能独立执行，必须依存在应用程序中，由应用程序提供多个线程执行控制，两者均可并发执行

注：以下*从 JVM 角度说进程和线程之间的关系*

![img](http://haoimg.hifool.cn/img/68747470733a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f323031392d332f4a564de8bf90e8a18ce697b6e695b0e68daee58cbae59f9f2e706e67.png)

关系：

* 一个进程中可以有多个线程，线程是进程划分成的更小的运行单位。
* 多个线程共享进程的**堆**和**方法区 (JDK1.8 之后的元空间)\**资源，但是每个线程有自己的\**程序计数器**、**虚拟机栈** 和 **本地方法栈**。

总结： 

* 线程和进程最大的不同在于基本上各进程是独立的，而各线程则不一定，因为同一进程中的线程极有可能会相互影响。
* 线程执行开销小，但不利于资源的管理和保护；而进程正相反。

> 为什么**程序计数器**、**虚拟机栈**和**本地方法栈**是线程私有的呢？
>
> 为什么堆和方法区是线程共享的呢？

### 1 为什么**程序计数器**是线程私有的？

* 程序计数器私有主要是**为了线程切换后能恢复到正确的执行位置**。
* **程序计数器**主要有下面两个作用：
    1. **字节码解释器**通过改变程序计数器来依次读取指令，从而实现代码的流程控制，如：顺序执行、选择、循环、异常处理。
    2. 在多线程的情况下，**程序计数器**记录当前线程执行的位置，从而当线程被切换回来的时候能够知道该线程上次运行到哪儿了。
    3. 需要注意的是，如果执行的是 native 方法，那么程序计数器记录的是 undefined 地址，只有执行的是 Java 代码时程序计数器记录的才是下一条指令的地址。

### 2 为什么**虚拟机栈 和 本地方法栈** 是私有的？

- 虚拟机栈和本地方法栈是线程私有的，主要是为了**保证线程中的局部变量不被别的线程访问到**
- **虚拟机栈：** 每个 Java 方法在执行的同时会创建一个**栈帧用于存储局部变量表、操作数栈、常量池引用等信息**。从方法调用直至执行完成的过程，就对应着一个栈帧在 Java 虚拟机栈中入栈和出栈的过程。
- **本地方法栈：** 和虚拟机栈所发挥的作用非常相似，区别是： **虚拟机栈为虚拟机执行 Java 方法 （也就是字节码）服务，而本地方法栈则为虚拟机使用到的 Native 方法服务。** 在 HotSpot 虚拟机中和 Java 虚拟机栈合二为一。

### 3 为什么 **堆和方法区** 是共享的？

* 方便多个线程共享使用公共资源，否则线程不就和进程一样了嘛。
* 堆是进程中最大的一块内存，**主要用于存放新创建的对象** (几乎所有对象都在这里分配内存)
* 方法区**主要用于存放已被加载的类信息、常量、静态变量、即时编译器编译后的代码等数据**。

## 3 并发 和 并行

- **并发：** 同一时间段，多个任务都在执行 (单位时间内不一定同时执行)；
- **并行：** 单位时间内，多个任务同时执行。

## 4 多线程的优点

总体上来说：

- **从计算机底层来说：** 
    - 线程可以比作是轻量级的进程，是程序执行的最小单位,线程间的**切换和调度的成本远远小于进程**。
    - **多核** CPU 时代意味着多个线程可以同时运行，这**减少了线程上下文切换的开销**。

再深入到计算机底层来探讨：

- **单核时代：** **在单核时代多线程主要是为了提高 CPU 和 IO 设备的综合利用率**。
    - 举个例子：当只有一个线程的时候会导致 CPU 计算时，IO 设备空闲；进行 IO 操作时，CPU 空闲。我们可以简单地说这两者的利用率目前都是 50%左右。但是当有两个线程的时候就不一样了，当一个线程执行 CPU 计算时，另外一个线程可以进行 IO 操作，这样两个的利用率就可以在理想情况下达到 100%了。
- **多核时代:** **多核时代多线程主要是为了提高 CPU 利用率**。
    - 举个例子：假如我们要计算一个复杂的任务，我们只用一个线程的话，CPU 只会一个 CPU 核心被利用到，而创建多个线程就可以让多个 CPU 核心被利用到，这样就提高了 CPU 的利用率。

## 5 使用多线程带来的问题

1. 内存泄漏
2. 死锁
3. 线程不安全
4. 。。。

> 还有其他哪些问题？

## 6 什么是上下文切换?

CPU 核心在任意时刻只能被一个线程使用，为了让这些线程都能得到有效执行，CPU 采取的策略是为每个线程分配时间片并轮转的形式。

当一个线程的时间片用完的时候就会先保存自己的状态，重新处于就绪状态让出CPU给其他线程使用，这个过程就属于一次上下文切换。

上下文切换通常是计算密集型的。

* 上下文切换需要相当可观的处理器时间，事实上，可能是操作系统中时间消耗最大的操作。
* Linux 相比与其他操作系统（包括其他类 Unix 系统）有很多的优点，其中有一项就是，其上下文切换和模式切换的时间消耗非常少。

## 7 什么是死锁？

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

1. 互斥条件：该资源任意一个时刻只由一个线程占用。
2. 请求与保持条件：一个进程因请求资源而阻塞时，对已获得的资源保持不放。
3. 不剥夺条件：线程已获得的资源在未使用完之前不能被其他线程强行剥夺，只有自己使用完毕后才释放资源。
4. 循环等待条件：若干进程之间形成一种头尾相接的循环等待资源关系。

### 2 消除死锁

为了避免死锁，只要破坏产生死锁的四个条件中的其中一个就可以了：

1. **破坏互斥条件** ：这个条件破坏，因为锁是使临界资源互斥访问的。
2. **破坏请求与保持条件** ：要一次性申请所有的资源。
3. **破坏不剥夺条件** ：占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源。
4. **破坏循环等待条件** ：靠按序申请资源来预防。按某一顺序申请资源，释放资源则反序释放。破坏循环等待条件。



---



# 一、创建线程

## 1 创建线程的三种方法

1. 继承Thread类
2. 实现Runnable接口
3. 实现Callable接口

三种方式的比较：实现接口的方式更好

* Java不支持多重继承，继承了一个类并不能继承其他的类，但可以实现多个接口
* 类可能只要求可执行就行，继承整个 Thread 类开销过大。

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

- start()：线程对象初始化完成进入NEW状态之后，调用start()方法会创建一个新线程并执行run()方法。
    1. start() 启动一个线程时：当前线程（parent线程）同步告知JVM，只要线程规划器空闲，应立即启动 调用start()方法的线程（即子进程进入到了就绪RUNNABLE状态）。
    2. 然后子进程通过此Thread类调用方法run()完成运行的操作， 这里方法run()称为线程体， 它包含了新开的线程要执行的的内容，
    3. run方法运行结束， 此线程终止， CPU再运行其它线程，start()不能被重复调用。
    4. 注：只有处于**NEW**状态的线程才可以调用 start() 方法，否则会抛出 IllegalThreadStateException 。也就是说，一个线程不能调用两次start方法，此时线程处于终止或者其他非NEW状态，不可以再次启动。
- run()： 子类需要覆盖Thread类的run方法。
    - run()就和普通的成员方法一样，可以被重复调用。直接调用run方法并不会创建新的线程，只是作为一个普通的方法调用，在当前线程串行的执行run()方法中的代码。

---



# 二、线程 - 基础

## 1 sleep()

Thread.sleep(millisec) 方法会休眠当前正在执行的线程(放弃CPU)。

需要注意的亮点：

1. sleep() 可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。
    1. 线程中抛出的其它异常也同样需要在本地进行处理。 
    2. sleep() 等会抛出InterruptedException的方法，**抛出中断异常前会清除线程中断标记位！**然后再抛出异常。
2. **在线程休眠阶段，仍然会持有锁，并不会释放锁。**

> sleep() **抛出中断异常后会清除线程中断标记位！**
>
> 什么是中断标记位？
>
> * 中断标识位：表示一个运行中的线程 是否被其他线程进行了中断操作，其他线程通过调用该线程的interrupt() 方法对其进行中断操作。

## 2 yield()

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

对静态方法 Thread.yield() 的调用声明了：当前线程已经完成了生命周期中最重要的部分，使当前线程由“运行状态”进入到“就绪状态”，可以切换给其它线程来执行。

* yield()方法主要是为了保障线程间调度的连续性，防止某个线程一直长时间占用cpu资源。
* 一般不推荐使用，它主要用于debug和测试程序，用来减少bug以及对于并发程序结构的设计。

需要注意的几点：

1. **该方法只是对线程调度器的一个建议**，而且也只是建议具有相同优先级的其它线程可以运行。如果没有正在等待的线程，或者所有正在等待的线程的优先级都比较低，那么该线程会继续运行。
2. yield方法不保证当前的线程会暂停或者停止，但是可以保证当前线程在调用yield方法时会放弃CPU。
3. 执行了yield方法的线程什么时候会继续运行由线程调度器来决定，不同的厂商可能有不同的行为。

## 3 join()

## 4 wait() / notify( ) / notifyAll( )

等待/通知 机制：

* 线程A在某些条件满足时，调用了对象O的wait() 方法，从而该线程进入到等待状态。
* 线程B在某些条件满足时，调用了对象O的notify()/notifyAll()方法来唤醒刮起的线程。
* 线程A收到通知后从对象O的wait() 方法返回，从而进入到后续的操作。

wait() / notify( ) / notifyAll( ) 的关系：

* 多个线程通过对象O完成交互，而对象上的wait()及notify()方法就像是开关信号一样，完成等待方和通知方的信息交互工作

使用需要注意的地方：

* 使用 wait() **挂起期间，线程会释放锁**。如果没有释放锁，那么其它线程就无法进入对象的同步方法或者同步控制块中，那么就无法执行 notify() 或者 notifyAll() 来唤醒挂起的线程，造成死锁。

```java
public class WaitNotify {
    static boolean flag = true;
    static Object lock = new Object();

    public static void main(String[] args) throws Exception {
        Thread waitThread = new Thread(new Wait(), "WaitThread");
        waitThread.start();
        TimeUnit.SECONDS.sleep(1);

        Thread notifyThread = new Thread(new Notify(), "NotifyThread");
        notifyThread.start();
    }

    static class Wait implements Runnable {

        public void run() {
            // 加锁，拥有lock的Monitor
            synchronized (lock) {
                // 当条件不满足时，继续wait，同时释放了lock的锁
                while (flag) {
                    try {
                        System.out.println(Thread.currentThread() + " flag is true. wait @ "
                                + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                // 条件满足时，完成工作
                System.out.println(Thread.currentThread() + " flag is false. running @ "
                        + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            }
        }
    }

    static class Notify implements Runnable {
        public void run() {
            // 加锁，拥有lock的Monitor
            synchronized (lock) {
                // 获取lock的锁，然后进行通知，通知时不会释放lock的锁，
                // 直到当前线程释放了lock后，WaitThread才能从wait方法中返回
                System.out.println(Thread.currentThread() + " hold lock. notify @ " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                lock.notifyAll();
                flag = false;
                Sleeper.sleep(5);
            }
            // 再次加锁
            synchronized (lock) {
                System.out.println(Thread.currentThread() + " hold lock again. sleep @ "
                        + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                Sleeper.sleep(5);
            }
        }
    }
}
```

## 5 sleep()、wait()、yield() 的区别

作用对象上的不同：

- sleep和yeild是Thread的静态方法，作用对象是当前线程。
- wait是Object的方法，作用对象是实例。

锁资源释放的不同：

- sleep方法会让当前线程放弃CPU，休眠一段时间，**在此期间线程仍然会持有锁**。
- wait() **挂起期间，线程会释放锁**
- yield()不会释放锁，只是通知线程调度器自己可以让出cpu时间片，而且也只是建议而已。
- join() 不会释放锁。当前线程(parent线程) 等待调用join方法的线程结束，再继续执行。join()有资格释放资源其实是通过调用wait()来实现的

> 哪种方法会释放锁？sleep() / join() / yield / wait
>
> 所谓的释放锁资源实际是通知对象内置的monitor对象进行释放，而只有wait() 方法的持有对象有内置的monitor对象才能实现任何对象的锁资源都可以释放。
>
> 又因为所有类都继承自Object，所以wait() 就成了Object方法，也就是通过wait()来通知对象内置的monitor对象释放，而且事实上因为这涉及对硬件底层的操作，所以wait()方法是native方法，底层是用C写的。  
>
> 其他都是Thread所有，所以其他3个是没有资格释放资源的 

用法不同：

* `wait()` 通常被用于线程间交互/通信，`sleep() `通常被用于暂停执行。

## 6 Daemon 守护线程

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

## 7 线程中断

什么是中断？

* 中断标记位 表示一个运行中的线程 是否被其他线程进行了中断操作，其他线程通过调用该线程的interrupt() 方法对其进行中断操作。
* 线程通过检查自身是否被中断来响应。
* 对于中断的正确理解是：中断操作并不会真正的中断一个正在运行的线程，而知识发出中断请求，然后由线程在下一个合适的时刻中断自己（合适的时刻称为取消点）。有些方法（wait / sleep / join)会严格的处理这些中断请求，当他们收到中断请求或者在开始执行时，发现已被设置好的中断状态时，将抛出一个异常。



与中断有关的三个方法：

```java
public void Thread.interrupt() // 中断线程，Thread实例方法。通知目标线程中断，也就是设置中断标志位为true。中断标志位表示当前线程已经被中断了。
public boolean Thread.isInterrupted() // Thread实例方法。判断当前线程是否有被中断（通过检查中断标志位）。
public static boolean Thread.interrupted() // Thread类静态方法。判断当前线程的中断状态，但同时会清除当前线程的中断标志
位状态。
```



线程中断的异常处理：

* 与线程相关的一些方法可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。
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

### 两阶段提交

```java
public class Test{
	@Test
    public void testTwoPhaseTermination () {
        TwoPhaseTermination twoPhaseTermination = new TwoPhaseTermination();

        twoPhaseTermination.start();

        Sleeper.sleep(3.5);
        log.info("end monitoring");
        twoPhaseTermination.stop();
    }

    class TwoPhaseTermination {
        // 监控线程
        private Thread monitorThread;
        // 停止标记
        private volatile boolean stop = false;
        // 判断是否执行过start方法
        private boolean starting = false;

        public void start() {
            synchronized (this) {
                if (this.starting) {
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
}
```



## 8 线程的生命周期

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

| 状态名称                    | 说明                                                         |
| --------------------------- | ------------------------------------------------------------ |
| NEW(初始化状态）            | 初始状态，线程刚刚创建，还没调用start方法                    |
| RUNNABLE（运行/就绪状态）   | Java线程将操作系统中的就绪状态 和 运行状态 统称为Runnable状态。表示线程正在执行 或 正在等待某种资源（CPU、IO等） |
| BLOCKED（阻塞状态）         | 表示线程处于阻塞状态，正在等待锁的释放                       |
| WAITING(无时限等待状态)     | 表示线程处于无时限等待状态，正在等待其他线程的特定动作（通知 或 中断）以便唤醒该线程。 |
| TIMED_WAITING（有时限等待） | 表示线程处于有时限等待状态，等待其他线程唤醒 或者 超时自己自行唤醒返回（这是区别于waiting的地方） |
| TERMINATED（终止状态）      | 线程已经执行完毕                                             |

需要注意的地方：

* RUNNABLE 状态 表示操作系统中的运行和就绪这两种状态
* BLOCKED 状态表示线程阻塞 在进入Synchronized修饰的方法或代码块时 的等待锁的状态
    * 但阻塞状态在java.concurrent包中Lock接口的线程状态却是等待状态，因为java.concurrent包中Lock接口对于阻塞的实现均使用了LockSupport类中的相关方法。

> 但阻塞状态在java.concurrent包中Lock接口的线程状态却是等待状态，因为java.concurrent包中Lock接口对于阻塞的实现均使用了LockSupport类中的相关方法。
>
> 这一句话《线程并发艺术》P90这一句话是什么意思？

### 1 RUNNABLE 与 BLOCKED 的状态转换

只有一种场景会触发这种转换，就是线程等待 synchronized 的隐式锁。

- synchronized 修饰的方法、代码块同一时刻只允许一个线程执行，其他线程只能等待，这种情况下，等待的线程就会从 RUNNABLE 转换到 BLOCKED 状态。
- 而当等待的线程获得 synchronized 隐式锁时，就又会从 BLOCKED 转换到RUNNABLE 状态。

注意！！！

* JVM 层面并不关心操作系统调度相关的状态，因为在 JVM 看来，等待 CPU 使用权（操作系统层面此时处于可执行状态）与等待 I/O（操作系统层面此时处于休眠状态）没有区别，都是在等待某个资源，所以都归入了 RUNNABLE 状态。

### 2 RUNNABLE 与 WAITING 的状态转换

三种场景会触发这种转换。

1. 获得对象synchronized 隐式锁的线程，调用无参数的 Object.wait() 方法。
2. 调用无参数的 Thread.join() 方法。其中的 join() 是一种线程同步方法，例如有一个线程对象thread A，当调用 A.join() 的时候，执行这条语句的线程会等待 thread A 执行完，而等待中的这个线程，其状态会从 RUNNABLE 转换到 WAITING。当线程 thread A 执行完，原来等待它的线程又会从WAITING 状态转换到 RUNNABLE。
3. 调用 LockSupport.park() 方法。其中的 LockSupport 对象，Java 并发包中的锁，都是基于它实现的。调用 LockSupport.park() 方法，当前线程会阻塞，线程的状态会从 RUNNABLE 转换到WAITING。调用 LockSupport.unpark(Thread thread) 可唤醒目标线程，目标线程的状态又会从WAITING 状态转换到 RUNNABLE。

### 3  RUNNABLE 与 TIMED_WAITING 的状态转换

有五种场景会触发这种转换。
1. 调用带超时参数的 Thread.sleep(long millis) 方法；
2. 获得 synchronized 隐式锁的线程，调用带超时参数的 Object.wait(long timeout) 方法；
3. 调用带超时参数的 Thread.join(long millis) 方法；
4. 调用带超时参数的 LockSupport.parkNanos(Object blocker, long deadline) 方法；
5. 调用带超时参数的 LockSupport.parkUntil(long deadline) 方法。

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



## 9 Happens-before 原则

java 内存模型 JMM(Java Memory Model) 并**不能保证**：**一个线程对共享变量的写操作，对于其他线程对该共享变量的读操作可见**



**什么是Happens-before原则**？

* **规定了对共享变量的写操作 对其他线程的读操作可见**
* **是可见性与有序性**一套规则的总结



Happens-before原则有哪些？

1. 程序顺序性原则
  在一个线程中，按照程序顺序，前面的操作Happen-Before于后续的任意操作。

2. synchronized原则
  对锁对象解锁之前的写操作， 对于接下来对锁对象加锁的其他线程的读操作可见。

3. volatile原则
  对一个 volatile 变量的写操作，对于后续其他线程对这个 volatile 变量的读操作可见。

4. start() 原则
  主线程 A start() 启动子线程 B 后，子线程 B 能够看到主线程在启动子线程 B 前的操作

5. 线程join() 原则
  某线程在结束前对共享变量的写操作，对于得知它结束后的其他线程的读操作是可见的。通过调用该线程 的 join() 方法实现。

6. 线程interrupt原则

  线程A 修改共享变量的写操作，在线程A调用线程B的interrupt() 方法后，对线程B通过Thread.interrupted()方法检测到中断发生后的读操作可见。

7. 默认值修改原则
    对变量默认值（0，false，null）的写操作，对其他变量对该共享变量的读操作是可见的。

8. 对象终结原则
    一个对象的初始化完成(构造函数执行结束)先行发生于它的finalize()方法的开始。

9. 传递性原则
    如果 A Happens-Before B，且 B Happens-Before C，那么 A Happens-Before C。

> 看一下黑马并发的代码

