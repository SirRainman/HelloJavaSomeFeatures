# Java 并发面试题总结

## 0 基本知识

![img](https://images2015.cnblogs.com/blog/721070/201704/721070-20170421155802696-1378852793.png)

## 0.1 基础线程机制

**Daemon**

守护线程是程序运行时在后台提供服务的线程，不属于程序中不可或缺的部分。

当所有非守护线程结束时，程序也就终止，同时会杀死所有守护线程。

main() 属于非守护线程。

在线程启动之前使用 setDaemon() 方法可以将一个线程设置为守护线程。

```
public static void main(String[] args) {
    Thread thread = new Thread(new MyRunnable());
    thread.setDaemon(true);
}
```

**sleep**()

Thread.sleep(millisec) 方法会休眠当前正在执行的线程，millisec 单位为毫秒。

sleep() 可能会抛出 InterruptedException，因为异常不能跨线程传播回 main() 中，因此必须在本地进行处理。线程中抛出的其它异常也同样需要在本地进行处理。

```
public void run() {
    try {
        Thread.sleep(3000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

**yield**()

对静态方法 Thread.yield() 的调用声明了当前线程已经完成了生命周期中最重要的部分，可以切换给其它线程来执行。该方法只是对线程调度器的一个建议，而且也只是建议具有相同优先级的其它线程可以运行。

```
public void run() {
    Thread.yield();
}
```

## 0.2 中断

一个线程执行完毕之后会自动结束，如果在运行过程中发生异常也会提前结束。

**InterruptedException**

通过调用一个线程的 interrupt() 来中断该线程，如果该线程处于阻塞、限期等待或者无限期等待状态，那么就会抛出 InterruptedException，从而提前结束该线程。但是不能中断 I/O 阻塞和 synchronized 锁阻塞。

对于以下代码，在 main() 中启动一个线程之后再中断它，由于线程中调用了 Thread.sleep() 方法，因此会抛出一个 InterruptedException，从而提前结束线程，不执行之后的语句。

```
public class InterruptExample {

    private static class MyThread1 extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                System.out.println("Thread run");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
public static void main(String[] args) throws InterruptedException {
    Thread thread1 = new MyThread1();
    thread1.start();
    thread1.interrupt();
    System.out.println("Main run");
}
Main run
java.lang.InterruptedException: sleep interrupted
    at java.lang.Thread.sleep(Native Method)
    at InterruptExample.lambda$main$0(InterruptExample.java:5)
    at InterruptExample$$Lambda$1/713338599.run(Unknown Source)
    at java.lang.Thread.run(Thread.java:745)
```

**interrupted**()

如果一个线程的 run() 方法执行一个无限循环，并且没有执行 sleep() 等会抛出 InterruptedException 的操作，那么调用线程的 interrupt() 方法就无法使线程提前结束。

但是调用 interrupt() 方法会设置线程的中断标记，此时调用 interrupted() 方法会返回 true。因此可以在循环体中使用 interrupted() 方法来判断线程是否处于中断状态，从而提前结束线程。

```
public class InterruptExample {

    private static class MyThread2 extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                // ..
            }
            System.out.println("Thread end");
        }
    }
}
public static void main(String[] args) throws InterruptedException {
    Thread thread2 = new MyThread2();
    thread2.start();
    thread2.interrupt();
}
Thread end
```

**Executor** 的中断操作

调用 Executor 的 shutdown() 方法会等待线程都执行完毕之后再关闭，但是如果调用的是 shutdownNow() 方法，则相当于调用每个线程的 interrupt() 方法。

以下使用 Lambda 创建线程，相当于创建了一个匿名内部线程。

```
public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    executorService.execute(() -> {
        try {
            Thread.sleep(2000);
            System.out.println("Thread run");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
    executorService.shutdownNow();
    System.out.println("Main run");
}
Main run
java.lang.InterruptedException: sleep interrupted
    at java.lang.Thread.sleep(Native Method)
    at ExecutorInterruptExample.lambda$main$0(ExecutorInterruptExample.java:9)
    at ExecutorInterruptExample$$Lambda$1/1160460865.run(Unknown Source)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
    at java.lang.Thread.run(Thread.java:745)
```

如果只想中断 Executor 中的一个线程，可以通过使用 submit() 方法来提交一个线程，它会返回一个 Future<?> 对象，通过调用该对象的 cancel(true) 方法就可以中断线程。

```
Future<?> future = executorService.submit(() -> {
    // ..
});
future.cancel(true);
```

## 0.3 线程之间交互

当多个线程可以一起工作去解决某个问题时，如果某些部分必须在其它部分之前完成，那么就需要对线程进行协调。

**join**()

在线程中调用另一个线程的 join() 方法，会将当前线程挂起，而不是忙等待，直到目标线程结束。

对于以下代码，虽然 b 线程先启动，但是因为在 b 线程中调用了 a 线程的 join() 方法，b 线程会等待 a 线程结束才继续执行，因此最后能够保证 a 线程的输出先于 b 线程的输出。

```
public class JoinExample {

    private class A extends Thread {
        @Override
        public void run() {
            System.out.println("A");
        }
    }

    private class B extends Thread {

        private A a;

        B(A a) {
            this.a = a;
        }

        @Override
        public void run() {
            try {
                a.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("B");
        }
    }

    public void test() {
        A a = new A();
        B b = new B(a);
        b.start();
        a.start();
    }
}
public static void main(String[] args) {
    JoinExample example = new JoinExample();
    example.test();
}
A
B
```

**wait() notify() notifyAll()**

调用 wait() 使得线程放弃持有的锁，进入waiting状态。其它线程调用 notify() 或者 notifyAll() 来唤醒挂起的线程，进入runnable状态。

它们都属于 Object 的一部分，而不属于 Thread。

只能用在同步方法或者同步控制块中使用，否则会在运行时抛出 IllegalMonitorStateException。

使用 wait() 挂起期间，线程会释放锁。这是因为，如果没有释放锁，那么其它线程就无法进入对象的同步方法或者同步控制块中，那么就无法执行 notify() 或者 notifyAll() 来唤醒挂起的线程，造成死锁。

```
public class WaitNotifyExample {

    public synchronized void before() {
        System.out.println("before");
        notifyAll();
    }

    public synchronized void after() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("after");
    }
}
public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    WaitNotifyExample example = new WaitNotifyExample();
    executorService.execute(() -> example.after());
    executorService.execute(() -> example.before());
}
before
after
```

**wait() 和 sleep() 的区别**

- wait() 是 Object 的方法，而 sleep() 是 Thread 的静态方法；
- wait() 会释放锁，sleep() 不会。

**await() signal() signalAll()**

java.util.concurrent 类库中提供了 Condition 类来实现线程之间的协调，可以在 Condition 上调用 await() 方法使线程等待，其它线程调用 signal() 或 signalAll() 方法唤醒等待的线程。

相比于 wait() 这种等待方式，await() 可以指定等待的条件，因此更加灵活。

使用 Lock 来获取一个 Condition 对象。

```
public class AwaitSignalExample {

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void before() {
        lock.lock();
        try {
            System.out.println("before");
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void after() {
        lock.lock();
        try {
            condition.await();
            System.out.println("after");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    AwaitSignalExample example = new AwaitSignalExample();
    executorService.execute(() -> example.after());
    executorService.execute(() -> example.before());
}
before
after
```

**阻塞队列**

## 0.4 线程状态

一个线程只能处于一种状态，并且这里的线程状态特指 Java 虚拟机的线程状态，不能反映线程在特定操作系统下的状态。

**新建**（NEW）

创建后尚未启动。

**可运行**（RUNABLE）

正在 JVM中运行。

在操作系统层面，该状态指可以被运行，它可能处于运行状态，也可能处于等待资源调度状态（例如CPU资源）。

**阻塞**（BLOCKED）

访问 synchronized 修饰的函数或者代码块，在重量级锁下，请求获取 monitor lock 失败就会进入阻塞状态。

如果获取到其他线程释放的monitor lock就可以进入runnable状态。

**无限期等待（WAITING）**

调用 Object.wait() 等方法进入。等待其它线程显式地唤醒。

阻塞和等待的区别在于，阻塞是被动的，它是在等待获取 monitor lock。

| 进入方法                                   | 退出方法                             |
| ------------------------------------------ | ------------------------------------ |
| 没有设置 Timeout 参数的 Object.wait() 方法 | Object.notify() / Object.notifyAll() |
| 没有设置 Timeout 参数的 Thread.join() 方法 | 被调用的线程执行完毕                 |
| LockSupport.park() 方法                    | LockSupport.unpark(Thread)           |

**限期等待（TIMED_WAITING）**

在一段时间后会被系统自动唤醒，无需其他线程唤醒，通常调用Thread.sleep() 进入该状态。

| 进入方法                                 | 退出方法                                        |
| ---------------------------------------- | ----------------------------------------------- |
| Thread.sleep() 方法                      | 时间结束                                        |
| 设置了 Timeout 参数的 Object.wait() 方法 | 时间结束 / Object.notify() / Object.notifyAll() |
| 设置了 Timeout 参数的 Thread.join() 方法 | 时间结束 / 被调用的线程执行完毕                 |
| LockSupport.parkNanos() 方法             | LockSupport.unpark(Thread)                      |
| LockSupport.parkUntil() 方法             | LockSupport.unpark(Thread)                      |

调用 Thread.sleep() 方法使线程进入限期等待状态时，常常用“使一个线程睡眠”进行描述。调用 Object.wait() 方法使线程进入限期等待或者无限期等待时，常常用“挂起一个线程”进行描述。睡眠和挂起是用来描述行为，而阻塞和等待用来描述状态。

**死亡（*TERMINATED*）**

可以是线程**结束任务**之后自己结束，或者产生了**异常**而结束。

## 1. synchronized 

synchronized关键字解决的是多个线程之间访问资源的同步性，可以保证原子性，可见性，禁止指令重排。

在 Java 早期版本中，synchronized属于重量级锁，效率低下，因为监视器锁（monitor）是依赖于底层的操作系统的 Mutex Lock 来实现的，Java 的线程是映射到操作系统的原生线程之上的。如果要挂起或者唤醒一个线程，都需要操作系统帮忙完成，而操作系统实现线程之间的切换时需要从用户态转换到内核态，这个状态之间的转换需要相对比较长的时间，时间成本相对较高，这也是为什么早期的 synchronized 效率低的原因。

jdk1.6对synchronized进行了优化，引入自旋锁、适应性自旋锁、锁消除、锁粗化、偏向锁、轻量级锁等技术来减少锁操作的开销。

![image-20200221183613568](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200221183613568.png)

**synchronized关键字最主要的三种使用方式：**

- **修饰实例方法:** monitor锁当前实例
- **修饰静态方法:**monitor锁对象的Class实例
- **修饰代码块:**  monitor锁括号内对象的实例

### 锁膨胀

https://www.jianshu.com/p/4758852cbff4

https://github.com/farmerjohngit/myblog/issues/12

JDK1.6 对锁的实现引入了大量的优化，如偏向锁、轻量级锁、自旋锁、适应性自旋锁、锁消除、锁粗化等技术来减少锁操作的开销。

Java中的`synchronized`有偏向锁、轻量级锁、重量级锁三种形式，分别对应了锁只被一个线程持有、不同线程交替持有锁、多线程竞争锁三种情况。他们会随着竞争的激烈而逐渐升级。

#### **偏向锁**

偏向锁是在无竞争的情况下把整个同步都消除掉。
偏向锁偏向于第一个获得它的线程，如果在接下来没有其他线程竞争锁，再次进入或者退出同一段同步块代码，并不再需要去进行**加锁**或者**解锁**操作。

实例对象对象头的markword记录线程ID，获取锁的过程：

case 1：当该对象第一次被线程获得锁的时候，会用CAS指令，将`mark word`中的thread id由0改成当前线程Id。如果成功，则代表获得了偏向锁，继续执行同步块中的代码。否则，将偏向锁撤销，升级为轻量级锁。

case 2：当被偏向的线程再次进入同步块时，发现锁对象偏向的就是当前线程，此时不需要再加锁。

case 3：当其他线程进入同步块时，发现已经有偏向的线程了，则会进入到**撤销偏向锁**的逻辑里，一般来说，会在`safepoint`中去查看偏向的线程是否还存活，如果存活且还在同步块中则将锁升级为轻量级锁，原偏向的线程继续拥有锁，当前线程则走入到锁升级的逻辑里；如果偏向的线程已经不存活或者不在同步块中，则将对象头的`mark word`改为无锁状态（unlocked），之后再升级为轻量级锁。

`safe point`这个词我们在GC中经常会提到，其代表了一个状态，在该状态下所有线程都是暂停的

#### **轻量级锁**

轻量级锁使用CAS操作消除同步使用的互斥量，对象头的Mark Word存放的是线程Lock Recored的指针。

获取锁过程：

1 线程在自己的栈帧中创建**Lock Recored**将对象头的**Mark Word**复制到**Lock Recored**中。
2然后用**CAS操作尝试将对象的Mark Word更新为指向Lock Record的指针。**如果这个更新成功了，则将Mark Word的锁标志位将转变为**"00"**，表示处于轻量级锁的状态。

如果是当前线程已经持有该锁了，代表这是一次锁重入。设置`Lock Record`第一部分（`Displaced Mark Word`）为null，起到了一个重入计数器的作用。然后结束。

如果Mark Word指向其他线程的锁记录空间，则自旋CAS更新指针，如果自旋结束仍然没有获得锁，轻量级锁就需要膨胀为重量级锁。


释放锁的过程：

**使用CAS操作将对象当前的Mark Word和线程中复制的Displaced Mark Word替换回来**
如果替换成功，恢复到无锁的状态(01)。
如果替换失败，说明有其他线程尝试获取该锁(此时锁已膨胀)，那就要在释放锁的同时，唤醒被挂起的线程。

#### **重量级锁**：

重量级锁时，对象头的Mark Word存放monitor对象指针 。

monitor调用的是操作系统底层的互斥量(mutex)。JVM会阻塞未获取到锁的线程，阻塞和唤醒操作需要从用户态切换到内核态，开销很大。

**synchronized 同步语句块的情况**

**monitorenter 指令指向同步代码块的开始位置，monitorexit 指令则指明同步代码块的结束位置。** 

每个对象都有一个monitor监视器，调用monitorenter就是尝试获取这个对象，成功获取到了就将值+1，离开就将值减1。如果是线程重入，再将值+1，说明monitor对象是支持可重入的。

**synchronized 修饰方法的的情况**

ACC_SYNCHRONIZED 标识，该标识指明了该方法是一个同步方法，JVM 通过该 ACC_SYNCHRONIZED 访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

**锁膨胀：**

锁主要存在`4`种状态，级别从低到高依次是：**无锁状态、偏向锁状态、轻量级锁状态和重量级锁状态**，这几个状态会随着竞争的情况逐渐升级，这几个锁只有重量级锁是需要使用操作系统底层`mutex`互斥原语来实现，其他的锁都是使用对象头来实现的。**需要注意锁可以升级，但是不可以降级。**

<img src="https://img-blog.csdnimg.cn/20190323140321501.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3h1ZWJhOA==,size_16,color_FFFFFF,t_70" alt="img" style="zoom:200%;" />

**其他优化**

自旋锁和自适应性自旋锁
自旋：当有个线程A去请求某个锁的时候，这个锁正在被其它线程占用，但是线程A并不会马上进入阻塞状态，而是循环请求锁(自旋)。因为很多持有锁的线程会很快释放锁，循环请求锁省下切换cpu的开销。如果自旋指定的时间还没有获得锁，仍然会被挂起。

自适应性自旋：自适应性自旋的时间由前一次自旋时间及锁的拥有者的状态决定。自旋成功了，则下次自旋的次数会增多。反之，如果对某个锁自旋很少成功，那么在以后获取这个锁的时候，自旋的次数会变少。

锁消除
锁消除是指虚拟机即时编译器在运行时，对一些代码上要求同步，但是被检测到不可能存在共享数据竞争的锁进行消除。

锁粗化
在使用锁的时候，需要让同步块的作用范围尽可能小，这样做的目的是为了使需要同步的操作数量尽可能小，如果存在锁竞争，那么等待锁的线程也能尽快拿到锁。

### synchronized和ReentrantLock 的区别

**1 两者都是可重入锁**

两者都是可重入锁。“可重入锁”概念是：自己可以再次获取自己的内部锁。同一个线程每次获取锁，锁的计数器都自增1，计数器为0才释放锁。如果不可锁重入的话，就会造成死锁。

**2 实现方法**

synchronized 是依赖于 JVM 实现的，jdk1.6优化之后有四种锁状态，是逐渐升级的。

ReentrantLock 是 JDK 层面实现的，内部有一个sync继承AQS，通过cas实现锁。需要 lock() 和 unlock() 方法配合 try/finally 语句块来完成。

**3 ReentrantLock 比 synchronized 增加了一些高级功能**

相比synchronized，ReentrantLock增加了一些高级功能。主要来说主要有三点：**①等待可中断；②可实现公平锁；③可实现选择性通知（锁可以绑定多个条件）**

- **ReentrantLock提供了一种能够中断等待锁的线程的机制**，通过lock.lockInterruptibly()来实现这个机制。也就是说正在等待的线程可以选择放弃等待，改为处理其他事情。

- **ReentrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平锁就是先等待的线程先获得锁。** ReentrantLock默认情况是非公平的，可以通过 ReentrantLock类的`ReentrantLock(boolean fair)`构造方法来制定是否是公平的。

- synchronized关键字与wait()和notify()/notifyAll()方法相结合可以实现等待/通知机制,notify**被通知的线程是由 JVM 选择的.**notifyAll()方法的话就会通知所有处于等待状态的线程

  ReentrantLock类实现方式是Condition接口与newCondition() 方法。一个Lock对象中可以创建多Condition实例（即对象监视器），线程对象可以注册在指定的Condition中，可以有选择性的进行线程通知。signalAll()方法只会唤醒注册在该Condition实例中的所有等待线程。

## 2. volatile

### Java内存模型

![img](https://upload-images.jianshu.io/upload_images/4899162-66736384361f6b8b.png?imageMogr2/auto-orient/strip|imageView2/2/w/812/format/webp)

在JMM中，所有变量都存储在主内存，每个线程有自己的工作内存，工作内存存储变量副本，对共享变量操作必须在工作内存中进行。要求原子性，可见性，有序性。JVM 还规定了happensbefore原则，让一个操作无需控制就能先于另一个操作完成。

**1. 原子性**

AtomicInteger 能保证多个线程修改的原子性。

synchronized 互斥锁来保证操作的原子性。它对应的内存间交互操作为：lock 和 unlock，在虚拟机实现上对应的字节码指令为 monitorenter 和 monitorexit。

**2. 可见性**

可见性指当一个线程修改了共享变量的值，其它线程能够立即得知这个修改。Java 内存模型是通过在变量修改后将新值同步回主内存，在变量读取前从主内存刷新变量值来实现可见性的。

主要有三种实现可见性的方式：

- volatile
- synchronized，对一个变量执行 unlock 操作之前，必须把变量值同步回主内存。
- final，被 final 关键字修饰的字段在构造器中一旦初始化完成，并且没有发生 this 逃逸（其它线程通过 this 引用访问到初始化了一半的对象），那么其它线程就能看见 final 字段的值。

**3. 有序性**

有序性是指：在本线程内观察，所有操作都是有序的。在一个线程观察另一个线程，所有操作都是无序的，无序是因为发生了指令重排序。在 Java 内存模型中，允许编译器和处理器对指令进行重排序，重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性。

volatile 关键字通过添加内存屏障的方式来禁止指令重排，即重排序时不能把后面的指令放到内存屏障之前。

也可以通过 synchronized 来保证有序性，它保证每个时刻只有一个线程执行同步代码，相当于是让线程顺序执行同步代码。

**操作**

JMM定义了以下8种操作，除64 位数据（long，double）的读写操作外，都要求保证原子性。

lock，unlock 作用于主内存变量

read（读取）作用于主内存变量。将变量值从主内存传到工作内存，以便load使用

load（载入）作用于工作内存，将read操作从主内存得到的变量放入工作内存变量副本

use 作用于工作内存，把工作内存变量值传给执行引擎

assign 作用于工作内存，把执行引擎接受的值赋给工作内存的变量

store 作用于工作内存，把工作内存的值传给主存，给write使用

write 作用于主内存，把store传来的值写入主内存的变量种

### volatile原理

JMM要求原子性，可见性，有序性，而volatile实现了可见性和有序性。

**可见性：**

读 read -> load -> use   读变量之前将本地内存置为无效，从主存读变量值

写 assign - > store -> write  写变量后将新值刷新到主内存

在CPU层面是这样实现的，某个线程A进行写操作时，发出汇编LOCK#指令，锁缓存行，同时让其它CPU相关缓存行失效。线程A向主存回写最新修改的值。

**禁止指令重排：**

为了提高性能，编译器和处理器进行指令重排。volatile的底层就是通过内存屏障来实现禁止指令重排。

lock前缀指令其实就相当于一个内存屏障。内存屏障是一组处理指令，用来实现对内存操作的顺序限制。

> **LoadLoad屏障：**禁止下面的普通读和上面的volatile读重排序
>  **StoreStore屏障：**禁止上面的普通写和volatile写重排序
>  **LoadStore屏障：**禁止下面的普通写和上面的volatile读重排序
>  **StoreLoad屏障：**禁止上面的volatile写和下面有可能出现的volatile读写重排序

`volatile`采用保守内存屏障策略：

> **在每个volatile写操作前插入StoreStore屏障，在写操作后插入StoreLoad屏障；
>  在每个volatile读操作后插入LoadLoad屏障，然后插入LoadStore屏障；**

## 3. ThreadLocal

ThreadLocal是线程内部的局部变量，只在线程的生命周期内起作用。每个Thread维护一个ThreadLocalMap，使用ThreadLocal的弱引用作为Key，value是存储的Object。弱引用的对象在GC时会被回收，变成(null,value)的形式，key被收回掉了，value还在，造成内存泄漏。


![img](https://pic4.zhimg.com/80/v2-5191e0d3603df1fac89081c2b68a51b7_hd.jpg)

提供了4个对外的方法。

void set(Object value)：设置当前线程的线程局部变量的值。

Object get()：返回当前线程所对应的线程局部变量。

void remove()：将当前线程局部变量的值删除，目的是为了减少内存的占用，调用该方法并不是必须的操作，但调用它可以加快内存回收的速度。

Object initialValue()：返回该线程局部变量的初始值。

#### 应用场景

 **比如spring mvc收到请求。拦截器将用户id等数据放到threadlocal当中。在当前线程就可以直接拿到数据** 

对于数据库的并发操作，我们可以用一个ThreadLocal变量来存放Connection；在spring中也经常出现，如Bean、事务管理、任务调度、AOP等。

#### 内存泄漏

症结是由于ThreadLocalMap的生命周期跟线程一样长，如果没有手动删除对应key就会导致内存泄漏。

ThreadLocal好的使用习惯，是每次使用完ThreadLocal，都调用它的remove()方法，清除数据。

**跨进程通信**

父子进程之间通信使用InheritableThreadLocal中createMap会把父线程的threadLocalMap传递给子线程。

## 4. 原子类

Atomic 原子类是具有原子操作特征的类，位于juc包下

**4类原子类**

**基本类型**

使用原子的方式更新基本类型

- AtomicInteger：整形原子类
- AtomicLong：长整型原子类
- AtomicBoolean：布尔型原子类

**数组类型**

使用原子的方式更新数组里的某个元素

- AtomicIntegerArray：整形数组原子类
- AtomicLongArray：长整形数组原子类
- AtomicReferenceArray：引用类型数组原子类

**引用类型**

- AtomicReference：引用类型原子类
- AtomicStampedReference：原子更新引用类型里的字段原子类
- AtomicMarkableReference ：原子更新带有标记位的引用类型

**对象的属性修改类型**

- AtomicIntegerFieldUpdater：原子更新整形字段的更新器
- AtomicLongFieldUpdater：原子更新长整形字段的更新器
- AtomicStampedReference：原子更新带有版本号的引用类型。该类将整数值与引用关联起来，可用于解决原子的更新数据和数据的版本号，可以解决使用 CAS 进行原子更新时可能出现的 ABA 问题。

### AtomicInteger 

**AtomicInteger 类常用方法**

```
public final int get() //获取当前的值
public final int getAndSet(int newValue)//获取当前的值，并设置新的值
public final int getAndIncrement()//获取当前的值，并自增
public final int getAndDecrement() //获取当前的值，并自减
public final int getAndAdd(int delta) //获取当前的值，并加上预期的值
boolean compareAndSet(int expect, int update) //如果输入的数值等于预期值，则以原子方式将该值设置为输入值（update）
public final void lazySet(int newValue)//最终设置为newValue,使用 lazySet 设置之后可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
```

cas+自旋锁

#### CAS原理

CAS是compare and swap。相当于一个乐观锁，一旦冲突就重试当前操作，直到成功为止。

CAS 通过unsafe类的native方法来访问内存偏移量，有三个操作数，当前的内存值V，旧的预期值A，要修改的新值B。当预期值A和内存值V相等时，将内存值V修改为B，返回true，否则返回false，变量 value 用 volatile 修饰，保证了多线程之间的内存可见性。

**缺点**

1长时间不成功自旋CAS占用cpu

2ABA 问题

3只能保证一个变量原子性

getAndAddInt调用了usafe类的getAndAddInt方法

```java
// unsafe.getAndAddInt
public final int getAndAddInt(Object obj, long valueOffset, long expected, int val) {
    int temp;
    do {
        temp = this.getIntVolatile(obj, valueOffset);  // 获取快照值
    } while (!this.compareAndSwap(obj, valueOffset, temp, temp + val));  // 如果此时 temp 没有被修改，就能退出循环，否则重新获取
    return temp;
}。
```

#### ABA

如果一个变量初次读取的时候是 A 值，它的值被改成了 B，后来又被改回为 A，那 CAS 操作就会误认为它从来没有被改变过。

J.U.C 包提供了一个带有标记的原子引用类 AtomicStampedReference 来解决这个问题，它可以通过控制变量值的版本来保证 CAS 的正确性。大部分情况下 ABA 问题不会影响程序并发的正确性，如果需要解决 ABA 问题，改用传统的互斥同步可能会比原子类更高效。

## 5 AQS

### AQS 介绍

AQS，全称是 AbstractQueuedSynchronizer，中文译为抽象队列式同步器。

JUC包中的ReentrantLock,ReentrantReadWriteLock，CountDownLatch, CyclicBarrier, Semaphore 等类内部都有一个sync继承了AQS。

### AQS 原理分析

#### AQS结构

AQS 中有，一个CHL同步队列，还有一个状态标志位state。

CLH队列是一个双向链表，每个Node结点是对线程信息的封装。

state是int类型变量，用volatile修饰，表示共享资源同步状态，修改需要用CAS。

入队在尾，出队在头，出队后需要激活该出队结点的后继结点，若后继结点为空或后继结点waitStatus>0，则从队尾向前遍历取waitStatus<0的触发阻塞唤醒；

```
private volatile int state;//共享变量，使用volatile修饰保证线程可见性
```

状态信息通过protected类型的getState，setState，compareAndSetState进行操作

```
//返回同步状态的当前值
protected final int getState() {  
        return state;
}
 // 设置同步状态的值
protected final void setState(int newState) { 
        state = newState;
}
//原子地（CAS操作）将同步状态值设置为给定值update如果当前同步状态的值等于expect（期望值）
protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

#### 资源的共享方式

**AQS定义两种资源共享方式**

根据同一时刻能否有多个线程同时获取到同步状态，可以分为共享式和排他式

- **Exclusive**

  （独占）：只有一个线程能执行，如ReentrantLock。又可分为公平锁和非公平锁：

  - 公平锁：按照线程在队列中的排队顺序，先到者先拿到锁
  - 非公平锁：当线程要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的

- **Share**（共享）：多个线程可同时执行，如Semaphore/CountDownLatch。Semaphore、CountDownLatch、 CyclicBarrier、ReadWriteLock 我们都会在后面讲到。

ReentrantReadWriteLock 可以看成是组合式，因为ReentrantReadWriteLock也就是读写锁允许多个线程同时对某一资源进行读。

不同的自定义同步器争用共享资源的方式也不同。自定义同步器在实现时只需要实现共享资源 state 的获取与释放方式即可，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队等），AQS已经在顶层实现好了。

#### 同步器Sync

同步器的设计基于模板方法模式，自定义同步器：

1. 用一个Sync类继承AbstractQueuedSynchronizer，独占式重写tryAcquire-tryRelease`、`共享式重写tryAcquireShared-tryReleaseShared
2. 自定义同步器调用其模板方法（Acquire），而这些模板方法会调用我们重写的方法（tryAcquire）。

这和我们以往通过实现接口的方式有很大区别，这是模板方法模式很经典的一个运用。

**AQS使用了模板方法模式，自定义同步器时需要重写下面几个AQS提供的模板方法：**

```
isHeldExclusively()//该线程是否正在独占资源。只有用到condition才需要去实现它。
tryAcquire(int)//独占方式。尝试获取资源，成功则返回true，失败则返回false。
tryRelease(int)//独占方式。尝试释放资源，成功则返回true，失败则返回false。
tryAcquireShared(int)//共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
tryReleaseShared(int)//共享方式。尝试释放资源，成功则返回true，失败则返回false。
private transient Thread exclusiveOwnerThread
```

默认情况下，每个方法都抛出 `UnsupportedOperationException`。

再以CountDownLatch以例，任务分为N个子线程去执行，state也初始化为N（注意N要与线程个数一致）。这N个子线程是并行执行的，每个子线程执行完后countDown()一次，state会CAS(Compare and Swap)减1。等到所有子线程都执行完后(即state=0)，会unpark()主调用线程，然后主调用线程就会从await()函数返回，继续后余动作。

推荐两篇 AQS 原理和相关源码分析的文章：

- http://www.cnblogs.com/waterystone/p/4920797.html
- https://www.cnblogs.com/chengxiao/archive/2017/07/24/7141160.html

### AQS 组件总结

#### CountDownLatch

共享锁，内部类sync重写了tryacquireshared和tryreleaseshared两个方法

维护了一个计数器 state，意思是需要等待的线程数，每次调用 countDown() 方法以CAS方式让state的值减 1，减到 0 的时候，调用 await() 方法的线程就会被唤醒。

```
public class CountdownLatchExample {

    public static void main(String[] args) throws InterruptedException {
        final int totalThread = 10;
        CountDownLatch countDownLatch = new CountDownLatch(totalThread);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                System.out.print("run..");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("end");
        executorService.shutdown();
    }
}
run..run..run..run..run..run..run..run..run..run..end
```

应用场景：

- 线程1获取交易相关数据
- 线程2获取商品基本信息
- 线程3获取推荐的信息
- 线程4获取评价信息
- ....

但是最终拼装数据并返回给前端，需要等到上面的所有信息都获取完毕之后，才能返回，这个场景就非常的适合 `CountDownLatch`来做了



#### CyclicBarrier

用来控制多个线程互相等待，只有当多个线程都到达时，这些线程才会继续执行。

和 CountdownLatch 相似，都是通过维护计数器来实现的。线程执行 await() 方法之后计数器会减 1，并进行等待，直到计数器为 0，所有调用 await() 方法而在等待的线程才能继续执行。

CyclicBarrier 和 CountdownLatch 的一个区别是，CyclicBarrier 的计数器通过调用 reset() 方法可以循环使用，所以它才叫做循环屏障。

CyclicBarrier 有两个构造函数，其中 parties 指示计数器的初始值，barrierAction 在所有线程都到达屏障的时候会执行一次。

```java
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}

public CyclicBarrier(int parties) {
    this(parties, null);
}
```

```java
public class CyclicBarrierExample {

    public static void main(String[] args) {
        final int totalThread = 10;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(totalThread);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                System.out.print("before..");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.print("after..");
            });
        }
        executorService.shutdown();
    }
}
before..before..before..before..before..before..before..before..before..before..after..after..after..after..after..after..after..after..after..after..
```

#### Semaphore

Semaphore 类似于操作系统中的信号量，可以控制对互斥资源的访问线程数。

以下代码模拟了对某个服务的并发请求，每次只能有 3 个客户端同时访问，请求总数为 10。

```java
public class SemaphoreExample {

    public static void main(String[] args) {
        final int clientCount = 3;
        final int totalRequestCount = 10;
        Semaphore semaphore = new Semaphore(clientCount);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalRequestCount; i++) {
            executorService.execute(()->{
                try {
                    semaphore.acquire();
                    System.out.print(semaphore.availablePermits() + " ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            });
        }
        executorService.shutdown();
    }
}
```

#### Reentrantlock

**ReentrantLock主要是通过AQS来实现的，有一个静态内部类Sync继承了AQS，FairSync和UnfairSync分别继承Sync实现公平锁和非公平锁。**

**LOCK: 用lock加锁时，如果是非公平锁，首先会用CAS修改state，成功则获取锁，失败就调用tryacquire函数。公平锁则直接调调用tryacquire函数。**

**tryacquire函数先判断state，如果state为0，公平锁会让头节点的线程CAS修改state，非公平锁是所有节点的线程一起CAS修改state。如果修改成功，会把独占线程值设为自己。state不为零的话就判断独占线程是不是当前线程，是的话就给state+1；**

**如果tryacquire获取锁失败，就封装线程为Node结点，用CAS方式加入同步等待队列，调用interrupt中断，等待前驱节点唤醒。**

**UNLOCK:将state-1，等于0就唤醒后继结点**

lock vs lockInterruptibly

ReentrantLock.lockInterruptibly允许在等待时由其它线程调用等待线程的Thread.interrupt方法来中断等待线程的等待而直接返回，这时不用获取锁，而会抛出一个InterruptedException。 ReentrantLock.lock方法不允许Thread.interrupt中断,即使检测到Thread.isInterrupted,一样会继续尝试获取锁，失败则继续休眠。只是在最后获取锁成功后再把当前线程置为interrupted状态,然后再中断线程。
[https://www.cnblogs.com/takumicx/p/9402021.html#%E7%BA%BF%E7%A8%8B%E5%8A%A0%E5%85%A5%E5%90%8C%E6%AD%A5%E9%98%9F%E5%88%97%E5%90%8E%E4%BC%9A%E5%81%9A%E4%BB%80%E4%B9%88acquirequeued](https://www.cnblogs.com/takumicx/p/9402021.html#线程加入同步队列后会做什么acquirequeued)

```java
	
	abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * Performs {@link Lock#lock}. The main reason for subclassing
         * is to allow fast path for nonfair version.
         */
        abstract void lock();

        /**
         * Performs non-fair tryLock.  tryAcquire is implemented in
         * subclasses, but both need nonfair try for trylock method.
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
        ...
    }
```

默认非公平锁，上来就先CAS一次，然后

```java
	//默认非公平锁，上来就先CAS一次
	static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * Sync object for fair locks
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
```

https://www.cnblogs.com/takumicx/p/9402021.html

#### 自定义Sync

**mutex** 排他锁 ，不可重入 

```java
public class mutex {
	private static class Sync extends AbstractQueuedSynchronizer{
		protected boolean isHeldExclusively() {
			return getState() == 1;
		}
		public boolean tryAcquire(int acquires) {
			if(compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}else {
				return false;
			}
		}
		public boolean tryRelease(int releases) {
			if(getState() == 0) {
				throw new IllegalMonitorStateException();
			}
			setExclusiveOwnerThread(Thread.currentThread());
			setState(0);
			return true;
		}
	}
	private final Sync sync = new Sync();
	public void lock() {
		sync.acquire(1);
	}
	public boolean trylock() {
		return sync.tryAcquire(1);
	}
	public void unlock() {
		sync.release(1);
	}
	public boolean isLocked() {
		return sync.isHeldExclusively();                                                                        
	}	
}

```

# 6 线程池（重点）

![这里写图片描述](https://img-blog.csdn.net/20180911222702597?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM2NTIwMjM1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 使用线程池的好处

**线程池**提供了一种限制和管理资源（包括执行一个任务）。 每个**线程池**还维护一些基本统计信息，例如已完成任务的数量。

**线程池的好处：**

- **复用线程，降低资源消耗**。
- **无需等待线程创建，提高响应速度**。
- **提高线程的可管理性**。线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控。

## Executor 框架

Executor 框架包括了线程池的管理，还提供了线程工厂、队列以及拒绝策略等。



![img](https://img-blog.csdn.net/20180319222418739)

1. **主线程首先要创建实现 `Runnable` 或者 `Callable` 接口的任务对象。**
2. **把创建完成的实现 `Runnable`/`Callable`接口的 对象直接交给 `ExecutorService` 执行**: `ExecutorService.execute（Runnable command）`）或者也可以把 `Runnable` 对象或`Callable` 对象提交给 `ExecutorService` 执行（`ExecutorService.submit（Runnable task）`或 `ExecutorService.submit（Callable  task）`）。
3. **如果执行 `ExecutorService.submit（…）`，`ExecutorService` 将返回一个实现`Future`接口的对象**（我们刚刚也提到过了执行 `execute()`方法和 `submit()`方法的区别，`submit()`会返回一个 `FutureTask 对象）。由于 FutureTask` 实现了 `Runnable`，我们也可以创建 `FutureTask`，然后直接交给 `ExecutorService` 执行。
4. **最后，主线程可以执行 `FutureTask.get()`方法来等待任务执行完成。主线程也可以执行 `FutureTask.cancel（boolean mayInterruptIfRunning）`来取消此任务的执行。**

## ThreadPoolExecutor 

**线程池实现类 `ThreadPoolExecutor` 是 `Executor` 框架最核心的类。**

```
    /**
     * 用给定的初始参数创建一个新的ThreadPoolExecutor。
     */
    public ThreadPoolExecutor(int corePoolSize,//线程池的核心线程数量
                              int maximumPoolSize,//线程池的最大线程数
                              long keepAliveTime,//当线程数大于核心线程数时，多余的空闲线程存活的最长时间
                              TimeUnit unit,//时间单位
                              BlockingQueue<Runnable> workQueue,//任务队列，用来储存等待执行任务的队列
                              ThreadFactory threadFactory,//线程工厂，用来创建线程，一般默认即可
                              RejectedExecutionHandler handler//拒绝策略，当提交的任务过多而不能及时处理时，我们可以定制策略来处理任务
                               ) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```

### 七大参数

corePoolSize : 核心线程数线程数。

maximumPoolSize : 当队列中存放的任务达到队列容量的时候，当前可以同时运行的线程数量变为最大线程数。

workQueue: 是一个阻塞队列，当新任务来的时候会先判断当前运行的线程数量是否达到核心线程数，如果达到的话，信任就会被存放在队列中。

keepAliveTime：非核心线程在没有任务时，等待的时间，超时就被销毁。

Timeunit: keepAliveTime 参数的时间单位。

threadFactory :executor 创建新线程的时候会用到。

handler :拒绝策略。一共有四种：

- **`ThreadPoolExecutor.AbortPolicy`**：**抛异常来拒绝新任务的处理。**用try catch捕获
- **`ThreadPoolExecutor.CallerRunsPolicy`**：**把任务交给主线程运行。**也就是在调用`execute`方法的线程中运行(`run`)被拒绝的任务，如果执行程序已关闭，则会丢弃该任务。因此这种策略会降低对于新任务提交速度，影响程序的整体性能。另外，这个策略喜欢增加队列容量。如果您的应用程序可以承受此延迟并且你不能任务丢弃任何一个任务请求的话，你可以选择这个策略。
- **`ThreadPoolExecutor.DiscardPolicy`：** **不处理新任务，直接丢弃掉。**
- **`ThreadPoolExecutor.DiscardOldestPolicy`：** **丢弃最早的未处理的任务。**

### 原理分析

首先分析一下 `execute`方法。

1、如果当前运行的线程少于corePoolSize，则创建新线程来执行任务（注意，执行这一步骤需要获取全局锁）。

2、如果运行的线程等于或多于corePoolSize，则将任务加入BlockingQueue。

3、如果阻塞队列已满，则创建非核心线程来处理任务（注意，执行这一步骤需要获取全局锁）。

4、如果已达到maximumPoolSize，就根据

ThreadPoolExecutor采取上述步骤的总体设计思路，是为了在执行execute()方法时，尽可能地避免获取全局锁（那将会是一个严重的可伸缩瓶颈）。在ThreadPoolExecutor完成预热之后（当前运行的线程数大于等于corePoolSize），几乎所有的execute()方法调用都是执行步骤2，而步骤2不需要获取全局锁。

```
   // 存放线程池的运行状态 (runState) 和线程池内有效线程的数量 (workerCount)
   private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }

    private final BlockingQueue<Runnable> workQueue;

    public void execute(Runnable command) {
        // 如果任务为null，则抛出异常。
        if (command == null)
            throw new NullPointerException();
        // ctl 中保存的线程池当前的一些状态信息
        int c = ctl.get();

        //  下面会涉及到 3 步 操作
        // 1.首先判断当前线程池中之行的任务数量是否小于 corePoolSize
        // 如果小于的话，通过addWorker(command, true)新建一个线程，并将任务(command)添加到该线程中；然后，启动该线程从而执行任务。
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        // 2.如果当前之行的任务数量大于等于 corePoolSize 的时候就会走到这里
        // 通过 isRunning 方法判断线程池状态，线程池处于 RUNNING 状态才会被并且队列可以加入任务，该任务才会被加入进去
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            // 再次获取线程池状态，如果线程池状态不是 RUNNING 状态就需要从任务队列中移除任务，并尝试判断线程是否全部执行完毕。同时执行拒绝策略。
            if (!isRunning(recheck) && remove(command))
                reject(command);
                // 如果当前线程池为空就新创建一个线程并执行。
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        //3. 通过addWorker(command, false)新建一个线程，并将任务(command)添加到该线程中；然后，启动该线程从而执行任务。
        //如果addWorker(command, false)执行失败，则通过reject()执行相应的拒绝策略的内容。
        else if (!addWorker(command, false))
            reject(command);
    }
```



### 几个常见的对比

**Runnable 和Callable**

**`Runnable` 接口**不会返回结果或抛出检查异常，但是**`Callable` 接口**可以。

工具类 `Executors` 可以实现 `Runnable` 对象和 `Callable` 对象之间的相互转换。（`Executors.callable（Runnable task`）或 `Executors.callable（Runnable task，Object resule）`）。

```
Runnable.java
@FunctionalInterface
public interface Runnable {
   /**
    * 被线程执行，没有返回值也无法抛出异常
    */
    public abstract void run();
}
Callable.java
@FunctionalInterface
public interface Callable<V> {
    /**
     * 计算结果，或在无法这样做时抛出异常。
     * @return 计算得出的结果
     * @throws 如果无法计算结果，则抛出异常
     */
    V call() throws Exception;
}
```

**execute vs submit**

1. **`execute()`方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功与否；**
2. **`submit()`方法用于提交需要返回值的任务。线程池会返回一个 `Future` 类型的对象，通过这个 `Future` 对象可以判断任务是否执行成功**，并且可以通过 `Future` 的 `get()`方法来获取返回值，`get()`方法会阻塞当前线程直到任务完成，而使用 `get（long timeout，TimeUnit unit）`方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。

## 其他线程池（都不用）

### FixedThreadPool和SingleThreadExecutor

`FixedThreadPool` 是固定n线程的线程池，`corePoolSize` 和 `maximumPoolSize` 都被设置为 nThreads，

`SingleThreadExecutor` 是只有一个线程的线程池。

```
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```

**为什么不推荐使用**`FixedThreadPool`和`SingleThreadExecutor`？

**`FixedThreadPool` `SingleThreadExecutor`使用无界队列 `LinkedBlockingQueue`，队列的容量为 Intger.MAX_VALUE，不会拒绝任务，在任务比较多的时候会导致 OOM。

### CachedThreadPool 详解

`CachedThreadPool` 是一个会根据需要创建新线程的线程池。下面通过源码来看看 `CachedThreadPool` 的实现：

```
    /**
     * 创建一个线程池，根据需要创建新线程，但会在先前构建的线程可用时重用它。
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```

`CachedThreadPool` 的` corePoolSize` 被设置为空（0），`maximumPoolSize `被设置为 Integer.MAX.VALUE，即它是无界的，这也就意味着如果主线程提交任务的速度高于 `maximumPool` 中线程处理任务的速度时，`CachedThreadPool` 会不断创建新的线程。极端情况下，这样会导致耗尽 cpu 和内存资源。

## ScheduledThreadPoolExecutor 

**`ScheduledThreadPoolExecutor` 主要用来在给定的延迟后运行任务，或者定期执行任务。** `maximumPoolSize `被设置为 Integer.MAX.VALUE。其任务队列是一个PriorityQueue，根据timer，sequenceNumber排序



## 线程池大小确定

> Ncpu/（1-阻塞系数）=Ncpu*(1+w/c),===》阻塞系数=w/(w+c)，即阻塞系数=阻塞时间/（阻塞时间+计算时间）

***最大线程数***

IO密集型=2Ncpu（可以测试后自己控制大小，2Ncpu一般没问题）（常出现于线程中：数据库数据交互、文件上传下载、网络数据传输等等）

计算密集型=Ncpu（常出现于线程中：复杂算法）

- **CPU 密集型任务(N+1)：** 这种任务消耗的主要是 CPU 资源，可以将线程数设置为 N（CPU 核心数）+1，比 CPU 核心数多出来的一个线程是为了防止任务暂停而带来的影响，CPU 就会处于空闲状态，而在这种情况下多出来的一个线程就可以充分利用 CPU 的空闲时间。
- **IO密集型=2Ncpu**（可以测试后自己控制大小，2Ncpu一般没问题）（常出现于线程中：数据库数据交互、文件上传下载、网络数据传输等等）问题

***核心线程数：***估算平时的流量需要的线程数

## 线程池故障排查

 https://mp.weixin.qq.com/s/0U_xmzvmnY354Lgv0IwZ6g 

故障： 分销员系统某核心应用，接口响应全部超时，dubbo 线程池被全部占满，并堆积了大量待处理任务，整个应用无法响应任何外部请求，处于“夯死”的状态。 

查看线程池参数是否有问题，自带的三个pool不能用

QPS      QPS是否过高，过高需要加机器

GC   Stop the world      jps  jstack查看线程    jstat查看GC，jmap输出dump文件

慢查询   开启慢查询日志，分析sql语句

## FutureTask

Future是个接口类，有cancel代表了未来的某个结果，当我们向线程池中submit提交任务的时候会返回该对象，可以通过future获得执行的结果。

`CompletableFuture`可以实现异步的操作，同时结合`lambada`表达式

future的底层实现异步原理？

### **基本知识**

**Java内存模型**

**线程通信**   ：管道，消息队列，共享缓冲区，信号量semaphore

**线程安全：**代码本身是不可变的，或者封装了互斥同步等手段，或者说是栈封闭和threadlocal变量。

**线程和进程的区别**

**线程和进程的区别？多线程编程需要注意什么？**

根本区别：进程是操作系统资源分配的基本单位，而线程是任务调度和执行的基本单位

在开销方面：每个进程都有独立的代码和数据空间（程序上下文），程序之间的切换会有较大的开销；线程可以看做轻量级的进程，同一类线程共享代码和数据空间，每个线程都有自己独立的**运行栈和程序计数器（PC）**，线程之间切换的开销小。

**Java中线程是如何停止的？**Interrupt

**操作系统是如何维护Java所创建的线程的？**一对一，使用操作系统原生的线程模型，通过系统调用，将程序的线程交给了操作系统内核进行调度

**如何防止死锁？**设置超时时间，超时可以退出防止死锁。尽量降低锁的使用粒度。

**Java中pv操作实现时用的类** Wait()和Notify  lock await signal

**wait和sleep分别属于哪个类的方法** sleep方法是Thread类的静态方法，wait是Object类的方法

线程之间的状态转换？

线程的几种状态？

线程状态切换

线程的几种状态—阻塞和销毁的产生原因—sleep和wait的区别和使用场景（在项目中如何使用他们）

**线程调度方式？**

**一般线程调度模式分为两种——抢占式调度和协同式调度。**抢占式调度指的是多条线程争用时间片，会出现饥饿。

协同式调度指某一线程执行完后主动通知系统切换到另一线程上执行

**调度算法？**

**先来先服务调度算法**

 FCFS调度算法的特点是算法简单，但效率低；对长作业比较有利，但对短作业不利（相对SJF和高响应比）；有利于CPU繁忙型作业，而不利于I/O繁忙型作业。

**时间片轮转调度法**

**短作业(SJF)优先调度算法**

**高响应比优先调度算法**

 根据比率：R=(w+s)/s （R为响应比，w为等待处理的时间，s为预计的服务时间）　　

**线程之间的交互方式有哪些？有没有线程交互的封装类 （join）**

lock condition await 

**解释一下并行和并发的区别** 并行是同一时刻可以多个进程在运行(处于running)，并发的是经过上下文快速切换，使得看上去多个进程同时都在运行

ThreadLocal 类的底层实现是怎么实现的？使用场景 

threadlocal

### **关键字和锁**

**volatile的底层如何实现，怎么就能保住可见性了？**

**什么是线程的可见性（volatile），根据操作系统底层解释**

**volatile原理**

JMM，可见性：读写，禁止指令重排LOCK

什么是偏向锁，膨胀锁、重量级锁，解释一下，如何转变的

Java的锁机制，synchronized的原理，与其他锁的区别

synchronized和lock的区别和应用场景，

Synchronized和ReentrantLock 比较

lock和synchronized的区别是什么

说一下synchronized的优点和缺点，与lock进行比较

Synchronize可以用什么代替

说一下ReetrantLock的内部实现

**什么是自旋锁，为什么需要他，应用场景是什么**

**自旋锁（深挖实现原理）**

https://www.jianshu.com/p/48e2510c13b3

Java锁机制，都说一下~

Java和mysql的锁介绍，乐观锁和悲观锁

乐观锁和悲观锁是啥？

**什么情况下适合用偏向锁？**只有一个线程竞争资源

### **工具类**

多线程讲一下，FutureTask

### **线程池**

线程池有哪些创建方式和安全性问题

有哪些线程池的类型
线程池中LinkedBlockingQueue满了的话，线程会怎么样
线程池的底层原理和实现方法

线程池原理，拒绝策略，核心线程数

线程池当队列中的任务都执行完毕之后会对线程进行怎样的操作？

**多线程是不是肯定比单线程好？**
**什么样的任务适合用多线程什么适合单线程？**
**单CPU的情况下适合用多线程吗？**

多线程编程的目的,就是"最大限度地利用CPU资源",CPU密集型。。。IO密集型。。。

线程池使用时核心线程数和最大线程数的设计要考虑什么因素？

Java 中多线程有哪几种实现方式？ 

线程池了解吗？说一下为什么要有线程池？ 

说一下线程池核心的几个参数 

### **代码题**

**三个线程如何实现交替打印ABC** 核心类 lock，3个condition，一个标识num，3个方法

起3个线程调10次方法

https://blog.csdn.net/L_Mcode/article/details/88310350

JDK中消费者生产者应用场景 

写过异步的代码吗

https://www.cnblogs.com/liumaowu/p/9293627.html

如果我要让10个线程并发同时开始运行，你要怎么做？

一上来就让写代码，生产者消费者模式，考虑多线程并发

生产者消费者模式介绍一下、阻塞与非阻塞

我在代码中用到了阻塞对列，然后问阻塞队列的底层原理，当时就有点懵说没看过，但是让我实现的话我可以这么这么做。。。。

### 系统设计

火车票抢票，只有一台服务器，瞬时访问量很大，如何系统的解决？

前端页面限制，url屏蔽，缓存 ，消息队列，负载均衡。