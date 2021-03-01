# HashMap

HashMap的默认初始化长度为什么是16？

* 16（另：初始化大小最好是2的幂）
* 需要将所有经过hash运算的key均匀的分不到数组中，**而位与运算比算数计算的效率高了很多**，
    * index = HashCode（Key） & （Length- 1）
* 之所以选择16，是为了服务将Key映射到index的算法。
    * 在使用2的幂的初始化大小时，Length-1的值是所有二进制位全为1，这种情况下，index的结果等同于HashCode后几位的值。
    * 只要输入的HashCode本身分布均匀，Hash算法的结果就是均匀的



hashmap的get原理

1. 调用get(key)的时候，会调用key的hashcode方法获得hashcode.
2. 根据hashcode获取相应的bucket。
3. 由于一个bucket对应的链表中可能存有多个Entry,
    1. 这个时候会调用key的equals方法来找到对应的Entry
4. 最后把值返回



为什么重写equals方法的时候需要重写hashCode方法呢？

* 因为在java中，所有的对象都是继承于Object类。Ojbect类中有两个方法equals、hashCode，这两个方法都是用来比较两个对象是否相等的。
* 在未重写equals方法我们是继承了object的equals方法，**那里的 equals是比较两个对象的内存地址**
    * 对于值对象，== 比较的是两个对象的值
    * 对于引用对象，== 比较的是两个对象的地址
* HashMap是通过key的hashCode去寻找index
    * get是根据key去hash然后计算出index，
    * 如何在index的链表上找到相应的对象？让值相同的对象返回相同的hash值，不同值的对象返回不同的hash值



HashMap的hash函数:

1. HashMap和其他基于map的类都是通过链地址法解决冲突，如果恶意程序知道我们用的是Hash算法，则在纯链表情况下，它能够发送大量请求导致哈希碰撞，然后不停访问这些key导致HashMap忙于进行线性查找，最终陷入瘫痪，即形成了拒绝服务攻击（DoS）。
2. 求index是hash & (length-1)，如果length 很小，则index只与hash低16位有关，容易产生hash碰撞，所以在低16位上取 低16与高16的异或值。

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

```java
static int indexFor(int hash, int length) {
    return hash & (length-1);
}
```



HashMap的负载因子初始值为什么是0.75？

* 太高了：时间换空间，虽然空间利用率高了，但是hash碰撞较多，**底层的红黑树变得异常复杂。对于查询效率极其不利。这种情况就是牺牲了时间来保证空间的利用率。**
* 太低了：空间换时间，当数组中的元素达到了一半就开始扩容，既然填充的元素少了，Hash冲突也会减少，那么底层的链表长度或者是红黑树的高度就会降低。查询效率就会增加。



Hashmap中的链表大小超过八个时会自动转化为红黑树，当删除小于六时重新变为链表，为啥呢？

* 根据泊松分布，在负载因子默认为0.75的时候，单个hash槽内元素个数为8的概率小于百万分之一，所以将7作为一个分水岭，等于7的时候不转换，大于等于8的时候才进行转换，小于等于6的时候就化为链表。

## 底层数据结构：

* **数组和链表组合构成**

![image-20210228140615797](http://haoimg.hifool.cn/img/image-20210228140615797.png)

数组中存Key-Value实例，Key-Value实例在数组中存储的位置是由hash决定的，倘若有hash冲突，则存放在该位置的链表中。

* 在Java7叫Entry
* 在Java8中叫Node

Node的结构:

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }

    public final K getKey()        { return key; }
    public final V getValue()      { return value; }
    public final String toString() { return key + "=" + value; }

    public final int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    public final V setValue(V newValue) {}

    public final boolean equals(Object o) {}
}
```

## Entry节点插入方式

### 扩容

数组容量是有限的，数据多次插入的，到达一定的数量就会进行扩容，也就是resize。

扩容的决定因素：

- Capacity：HashMap当前长度。
- LoadFactor：负载因子，默认值0.75f。
- 当**Capacity * LoadFactor > current HashMap Size**时，就需要扩容了



如何扩容：

1. 扩容：创建一个新的Entry空数组，长度是原数组的2倍。

2. ReHash：遍历原Entry数组，把所有的Entry重新ReHash到新数组。

    > 为什么不直接复制？
    >
    > 答：因为长度扩大以后，Hash的规则也随之改变。
    >
    > index的Hash规则公式---> index = HashCode（Key） & （Length - 1）

### jdk 1.7

Entry节点在插入链表的时候,**头插法**

![img](http://haoimg.hifool.cn/img/HashMap01.jpg)



源码解析：

1. 死循环的根源在transfer函数

![image-20210228141306802](http://haoimg.hifool.cn/img/image-20210228141306802.png)



2. transfer函数使用头插法

```
void transfer(Entry[] newTable) {
    Entry[] src = table;
    int newCapacity = newTable.length;
    //下面这段代码的意思是：
    //  从OldTable里摘一个元素出来，然后放到NewTable中
    for (int j = 0; j < src.length; j++) {
        Entry<K,V> e = src[j];
        if (e != null) {
            src[j] = null;
            do {
                Entry<K,V> next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            } while (e != null);
        }
    }
} 
```

```
do {
    Entry<K,V> next = e.next; // <--假设线程一执行到这里就被调度挂起了
    int i = indexFor(e.hash, newCapacity);
    e.next = newTable[i];
    newTable[i] = e;
    e = next;
} while (e != null);
```



3. 线程一停止，线程二执行结束

    1. **因为Thread1的 e 指向了key(3)，而next指向了key(7)，其在线程二rehash后，指向了线程二重组后的链表**。

    ![img](https://coolshell.cn/wp-content/uploads/2013/05/HashMap02.jpg)



4. **线程一被调度回来执行。**
    - **先是执行 newTalbe[i] = e;**
    - **然后是e = next，导致了e指向了key(7)，**
    - **而下一次循环的next = e.next导致了next指向了key(3)**

![img](http://haoimg.hifool.cn/img/HashMap04.jpg)



5. **线程一接着工作。**把key(7)摘下来，放到newTable[i]的第一个，然后把e和next往下移**。**

![img](http://haoimg.hifool.cn/img/HashMap04.jpg)

6. **环形链接出现。**

    **e.next = newTable[i] 导致 key(3).next 指向了 key(7)**

    **注意：此时的key(7).next 已经指向了key(3)， 环形链表就这样出现了。**

![img](http://haoimg.hifool.cn/img/HashMap05.jpg)

### jdk 1.8

Entry节点在插入链表的时候,**尾插法**

**java8之后链表有红黑树**的部分



并发：

Java7在多线程操作HashMap时可能引起死循环，原因是扩容转移后前后链表顺序倒置，在转移过程中修改了原来链表中节点的引用关系。

* Java8在同样的前提下并不会引起死循环，原因是扩容转移后前后链表顺序不变，保持之前节点的引用关系。
* 但是在并发的情况下会出现数据覆盖的问题（多个线程同时创建节点）
* 是否意味着Java8是线程安全的？
    * 不是，
    * put/get方法都没有加同步锁，无法保证上一秒put的值，下一秒get的时候还是原值，所以线程安全还是无法保证。

## HashTable

并发度不高，直接在方法上锁，最多同时允许一个线程访问该对象

Hashtable 跟HashMap不一样点:

* **Hashtable 是不允许键或值为 null 的，HashMap 的键值则都可以为 null。**
    * Hashtable在我们put 空值的时候会直接抛空指针异常，但是HashMap却做了特殊处理。
    * Hashtable使用的是**安全失败机制（fail-safe）**，这种机制会使你此次读到的数据不一定是最新的数据。
    * 如果你使用null值，就会使得其无法判断对应的key是不存在还是为空，因为你无法再调用一次contain(key）来对key是否存在进行判断，ConcurrentHashMap同理。





**快速失败（fail—fast）**是java集合中的一种机制， 在用迭代器遍历一个集合对象时，如果遍历过程中对集合对象的内容进行了修改（增加、删除、修改），则会抛出Concurrent Modification Exception。

* 这里异常的抛出条件是检测到 modCount！=expectedmodCount 这个条件。如果集合发生变化时修改modCount值刚好又设置为了expectedmodCount值，则异常不会抛出。

**安全失败（fail—safe）**java.util.concurrent包下的容器都是安全失败，可以在多线程下并发使用，并发修改。

## SynchronizedMap

SynchronizedMap内部维护了一个普通对象Map，还有排斥锁mutex

![image-20210228160938586](http://haoimg.hifool.cn/img/image-20210228160938586.png)



![image-20210228160956285](http://haoimg.hifool.cn/img/image-20210228160956285.png)

# ConurrentHashMap

高并发场景的多线程可以使用Collections.synchronizedMap同步加锁的方式

JDK版本变动：

* 1.7使用Segment+HashEntry分段锁的方式实现
* 1.8则抛弃了Segment，改为使用CAS+synchronized+Node实现，同样也加入了红黑树，避免链表过长导致性能的问题。

## JDK 1.7：分段锁

从结构上说，1.7版本的ConcurrentHashMap采用分段锁机制

* Segment数组，Segment继承与ReentrantLock，
    * Segment则包含HashEntry的数组，
        * HashEntry本身就是一个链表的结构，具有保存key、value的能力能指向下一个节点的指针。
        * HashEntry跟HashMap差不多的，但是不同点是，他使用volatile去修饰了他的数据Value还有下一个节点next。
    * 每个Segment都是一个HashMap，默认的Segment长度是16，也就是支持16个线程的并发写，Segment之间相互不会受到影响。

![image-20210228165400131](http://haoimg.hifool.cn/img/image-20210228165400131.png)

### put流程

整个流程和HashMap非常类似，只不过是先定位到具体的Segment，然后通过ReentrantLock去操作而已，和HashMap基本上是一样的。

1. 计算hash，定位到segment，segment如果是空就先初始化
2. 使用ReentrantLock加锁，如果获取锁失败则尝试自旋，自旋超过次数就阻塞获取，保证一定获取锁成功
3. 遍历HashEntry，就是和HashMap一样，数组中key和hash一样就直接替换，不存在就再插入链表，链表同样

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUljPgPC9h7FmEyOSbttvPeh5Ua8JnShvjMmVbqbnG4SBeM0XbGC7XicL1tyic2ZsCLUM8doxianE5W9w/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### get流程

get也很简单，key通过hash定位到segment，再遍历链表定位到具体的元素上，需要注意的是value是volatile的，所以get是不需要加锁的。



## JDK 1.8：CAS + synchronized

1.8抛弃分段锁，转为用CAS+synchronized来实现，同样HashEntry改为Node，也加入了红黑树的实现。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUljPgPC9h7FmEyOSbttvPehOaIZhNNGIw92iaLxnZW4PsxRN64LOy5vZCLrOcjf22f4umKTgtEU9TQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### put流程

1. 首先计算hash，遍历node数组，如果node是空的话，就通过CAS+自旋的方式初始化
2. 如果当前数组位置是空则直接通过CAS自旋写入数据
3. 如果hash==MOVED，说明需要扩容，执行扩容
4. 如果都不满足，就使用synchronized写入数据，写入数据同样判断链表、红黑树，链表写入和HashMap的方式一样，key hash一样就覆盖，反之就尾插法，链表长度超过8就转换成红黑树

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUljPgPC9h7FmEyOSbttvPehSWXgNAwd00W76yvhUsqNK8uztPmTQwzicee3zNic0po5hjZILceUTiaCg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### get查询

get很简单，通过key计算hash，如果key hash相同就返回，如果是红黑树按照红黑树获取，都不是就遍历链表获取。