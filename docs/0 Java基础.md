# String

- String:
    - 不可变的字符序列
    - 底层使用`char[]`存储，final
- StringBuffer:
    - 可变的字符序列
    - **线程安全的，效率低**
    - 底层使用`char[]`存储
- StringBuilder:
    - 可变的字符序列
    - **线程不安全的，效率高**
    - 底层使用`char[]`存储

都继承子charSequence

- **StringBuffer 和 StringBuilder都继承自AbstractStringBuilder**
    - **初始化创建一个长度为16的`char[]`数组**
    - 每次append需要**判断容量是否足够，进行扩容**（ensureCapcityInternal()）
    - 默认情况下，**每次扩容为原来的2倍再加2，原有内容复制到新的数组中**



面试题：

```java
public static void main(String[] args) {
    String str = null;
    StringBuilder sb = new StringBuilder();
    sb.append(str);

    System.out.println(sb.length());    //  4

    System.out.println(sb);     //  "null"

    StringBuilder sb1 = new StringBuilder(str); //exception
    System.out.println(sb1);
}
```

appendNull()

# List

## ArrayList和LinkedList特点及各自应用场景

ArrayList：

* ArrayList是基于数组实现的
* ArryList初始化时，elementData数组大小默认为10；
* 每次add（）时，先调用ensureCapacity（）保证数组不会溢出，如果此时已满，会扩展为数组length的1.5倍+1，然后用array.copy的方法，将原数组拷贝到新的数组中；
* ArrayList线程不安全，Vector方法是同步的，线程安全；



LinkedList：

* LinkedList是基于双链表实现的
* 初始化时，有个header Entry，值为null；
* 使用header的优点是：在任何一个条目（包括第一个和最后一个）都有一个前置条目和一个后置条目，因此在LinkedList对象的开始或者末尾进行插入操作没有特殊的地方；



使用场景：

* 对于随机访问的get和set方法，ArrayList要优于LinkedList，因为LinkedList要移动指针。
* 对于新增和删除操作add和remove，LinkedList比较占优势，因为ArrayList要移动数据。

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

## 节点插入方式

### 扩容

数组容量是有限的，数据多次插入的，到达一定的数量就会进行扩容，也就是resize。

扩容的决定因素：

- Capacity：HashMap当前长度。
- LoadFactor：负载因子，默认值0.75f。
- 当**Capacity * LoadFactor > current HashMap Size**时，就需要扩容了



如何扩容：

1. 扩容：**创建一个新的Entry空数组，长度是原数组的2倍。**

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
* **但是在并发的情况下会出现数据覆盖的问题（多个线程同时创建节点）**
* 是否意味着Java8是线程安全的？
    * 不是，
    * put/get方法都没有加同步锁，无法保证上一秒put的值，下一秒get的时候还是原值，所以线程安全还是无法保证。

## HashTable

在所有涉及到多线程操作的都加上了synchronized关键字来锁住整个table，这就意味着所有的线程都在竞争一把锁，在多线程的环境下，它是安全的，但是无疑是效率低下的。

Hashtable 跟HashMap不一样点:

* **Hashtable 是不允许键或值为 null 的，HashMap 的键值则都可以为 null。**
    * Hashtable在我们put 空值的时候会直接抛空指针异常，但是HashMap却做了特殊处理。
    * Hashtable使用的是**安全失败机制（fail-safe）**，这种机制会使你此次读到的数据不一定是最新的数据。
    * 如果你使用null值，就会使得其无法判断对应的key是不存在还是为空，因为你无法再调用一次contain(key）来对key是否存在进行判断，ConcurrentHashMap同理。





**快速失败（fail—fast）**是java集合中的一种机制， 在用迭代器遍历一个集合对象时，如果遍历过程中对集合对象的内容进行了修改（增加、删除、修改），则会抛出Concurrent Modification Exception。

* 这里异常的抛出条件是检测到 modCount！=expectedmodCount 这个条件。如果集合发生变化时修改modCount值刚好又设置为了expectedmodCount值，则异常不会抛出。

**安全失败（fail—safe）**java.util.concurrent包下的容器都是安全失败，可以在多线程下并发使用，并发修改。

## TreeMap

TreeMap实现SortMap接口，能够把它保存的记录根据键排序,默认是按键值的升序排序，也可以指定排序的比较器，当用Iterator 遍历TreeMap时，得到的记录是排过序的。

```java
public TreeMap(Comparator<? super K> comparator){}

Map<String,String> map = new TreeMap<>(new Comparator<String>(){
    public int compare(String o1,String o2){
        return  o2.compareTo(o1); //用正负表示大小值
    }
});
```



## LinkedHashMap

LinkedHashMap保存了记录的插入顺序，在用Iterator遍历LinkedHashMap时，先得到的记录肯定是先插入的.也可以在构造时用带参数，按照应用次数排序。

在遍历的时候会比HashMap慢，不过有种情况例外，

* 当HashMap容量很大，实际数据较少时，遍历起来可能会比LinkedHashMap慢，
* 因为LinkedHashMap的遍历速度只和实际数据有关，和容量无关，
* 而HashMap的遍历速度和他的容量有关。

## SynchronizedMap

SynchronizedMap内部维护了一个普通对象Map，还有排斥锁mutex

![image-20210228160938586](http://haoimg.hifool.cn/img/image-20210228160938586.png)



![image-20210228160956285](http://haoimg.hifool.cn/img/image-20210228160956285.png)

# ConurrentHashMap

[源码解析](https://www.cnblogs.com/study-everyday/p/6430462.html)

HashMap在put的时候，插入的元素超过了容量（由负载因子决定）的范围就会触发扩容操作，就是rehash，

* 这个会重新将原数组的内容重新hash到新的扩容数组中，如果hash值相同，可能出现同时在同一数组下用链表完成数据的插入工作
* 在多线程的环境下，存在同时其他的元素也在进行put操作，
    * 在hashmap1.7中 put插入到链表中使用的是头插法，会造成死循环
    * 在hashmap1.8中 put插入到聊表中使用的是尾插法，不会造成死循环，但是在并发的情况下会出现数据覆盖的问题（多个线程同时创建节点）
* 如果hash值相同，可能出现同时在同一数组下用链表表示，造成闭环，导致在get时会出现死循环，
* 所以HashMap是线程不安全的。

高并发场景的多线程可以使用Collections.synchronizedMap同步加锁的方式



JDK版本变动：

* 1.7使用ReentrantLock+Segment+HashEntry 的方式实现
* 1.8使用synchronized+CAS+HashEntry+红黑树实现，
    * 加入红黑树，避免链表过长导致性能的问题。
    * JDK1.8的实现降低锁的粒度
    * JDK1.8为什么使用内置锁synchronized来代替重入锁ReentrantLock？
        1. 因为粒度降低了，在相对而言的低粒度加锁方式，synchronized并不比ReentrantLock差，在粗粒度加锁中ReentrantLock可能通过Condition来控制各个低粒度的边界，更加的灵活，而在低粒度中，Condition的优势就没有了
        2. 基于JVM的**synchronized优化空间更大**，使用内嵌的关键字比使用API更加自然
        3. 在大量的数据操作下，对于**JVM的内存压力**，基于API的ReentrantLock会开销更多的内存，虽然不是瓶颈，但是也是一个选择依据

## JDK 1.7：分段锁

从结构上说，1.7版本的ConcurrentHashMap采用分段锁机制

* Segment数组，Segment继承于ReentrantLock，
    * Segment则包含HashEntry的数组，
        * HashEntry本身就是一个链表的结构，具有保存key、value的能力能指向下一个节点的指针。
        * HashEntry跟HashMap差不多的，但是不同点是，他使用volatile去修饰了他的数据Value还有下一个节点next。
    * 每个**Segment都是一个HashMap**，默认的Segment长度是16，也就是支持16个线程的并发写，Segment之间相互不会受到影响。

![img](http://haoimg.hifool.cn/img/926638-20170809132445011-2033999443.png)

### put流程

整个流程和HashMap非常类似，只不过是先定位到具体的Segment，然后通过ReentrantLock去操作而已，和HashMap基本上是一样的。

1. 第一次计算hash，定位到Segment，Segment还没有初始化，即通过CAS操作进行赋值
2. 第二次hash操作，在HashEntry数组中，找到相应的HashEntry的位置，使用ReentrantLock加锁，如果获取锁失败则尝试自旋，自旋超过次数就阻塞获取，保证一定获取锁成功
3. 遍历HashEntry，就是和HashMap一样，数组中key和hash一样就直接替换，不存在就再插入链表，链表同样



### get流程

get也很简单，key通过hash定位到segment，再遍历链表定位到具体的元素上，需要注意的是value是volatile的，所以get是不需要加锁的。

ConcurrentHashMap get()

* 第一次key需要经过一次hash定位到Segment的位置，
* 第二次再hash定位到指定的HashEntry，遍历该HashEntry下的链表进行对比，成功就返回，不成功就返回null

### size 流程

计算size的时候，可能会有其他线程在并发的插入数据，可能会导致你计算出来的size和你实际的size有相差（在你return size的时候，插入了多个数据），要解决这个问题，JDK1.7版本用两种方案：

1. 第一种方案他会使用不加锁的模式去尝试多次计算ConcurrentHashMap的size，最多三次，比较前后两次计算的结果，结果一致就认为当前没有元素加入，计算的结果是准确的
2. 第二种方案是如果第一种方案不符合，他就会给每个Segment加上锁，然后计算ConcurrentHashMap的size返回



## JDK 1.8：CAS + synchronized

* 1.8抛弃分段锁，转为用CAS+synchronized来实现并发安全性
* 同样HashEntry改为Node，整个看起来就像是优化过且线程安全的HashMap
    * Node数据结构很简单，是一个链表，但是只允许对数据进行查找，不允许进行修改
* 加入了红黑树的实现。
    * TreeNode继承与Node，但是数据结构换成了二叉树结构，当链表的节点数大于8时会转换成红黑树的结构
    * TreeBin可以理解为存储TreeNode树形结构的容器，所以TreeBin就是封装TreeNode的容器，它提供转换黑红树的一些条件和锁的控制

![img](http://haoimg.hifool.cn/img/926638-20170809132741792-1171090777.png)





### put流程

1. 首先，如果没有初始化，先调用initTable方法来进行初始化过程
2. 计算hash，遍历node数组，如果node是空的话，就通过CAS+自旋的方式初始化
    1. 如果当前数组位置是空，则直接通过CAS自旋写入数据
    2. 如果hash==MOVED，说明需要扩容，执行扩容
    3. 如果存在hash冲突，就synchronized加锁来保证线程安全，
        1. 链表长度小于8，链表写入和HashMap的方式一样，key hash一样就覆盖，反之就尾插法
        2. 链表长度超过8就转换成红黑树



### get查询

get很简单，通过key计算hash，如果key hash相同就返回，

1. 计算hash值，定位到该table索引位置，如果是首节点符合就返回
2. 如果是红黑树按照红黑树获取，不是就遍历链表获取。
3. 如果遇到扩容的时候，会调用标志正在扩容节点ForwardingNode的find方法，查找该节点，匹配就返回

### size 计算

**指导思想：** 

* 尽量降低线程冲突，以最快的速度写入 size 的变化。

**如何降低冲突？**

* 如果没有冲突发生，只将 size 的变化写入 baseCount。
* 一旦发生冲突，就用一个数组（counterCells）来存储后续所有 size 的变化。
    * counterCells存储的都是value为1的CounterCell对象，而这些对象是因为在CAS更新baseCounter值时，由于高并发而导致失败，最终将值保存到CounterCell中，放到counterCells里。
    * 这也就是为什么sumCount()中需要遍历counterCells数组，sum累加CounterCell.value值了。
    * 线程只要对任意一个数组元素写入 size 变化成功即可，数组长度越长，线程发生冲突的可能性就越小。

**关于 counterCells 扩容：**

* 如果 CAS 数组元素连续失败两次，就会进行 counterCells 数组的扩容，直到达到机器的处理器数为止。
* 比如我的机器是双核四线程，真正能并行的线程数是 4，所以在我机器上 counterCells 初始化后，最多扩容一次。





# 异常

异常包括

* **Error**(栈溢出（StackOverflowError），
* **OOM（Out of Mem）**
    * 什么时候OOM？
* **Exception**



**Exception分为两种**

![20210210091200](http://haoimg.hifool.cn/img/img20210210091200.png)

![20210210091613](http://haoimg.hifool.cn/img/img20210210091613.png)

### finally

- finally中声明的是一定会被执行的代码。即使catch中又出现异常了 / try中有return语句 / catch中有return语句等情况，也会被执行。
- 像数据库连接、输入输出流、网络变成socket等资源，JVM是不能自动回收的，我们需要手动的进行资源释放。此时资源释放就需要声明在finally里面。

### 开发中如何选择使用try-catch-fianlly还是throws

- 如果父类中被重写的方法没有thorws方式处理异常，则子类重写的方法也不能使用throws，意味着如果子类重写的方法中有异常，必须使用try-catch-finally方式处理。
- 执行的方法a中，先后调用了另外的几个方法，这几个方法是递进关系执行的。我们建议这几个方法使用thorws的方式进行处理。而执行的方法a可以考虑使用try-catch-finally方式进行处理。

### throw 和 throws

throw与throws的比较

1. throws出现在方法函数头；而throw出现在函数体。

2. throws表示出现异常的一种可能性，并不一定会发生这些异常；throw则是抛出了异常，执行throw则一定抛出了某种异常对象。

3. 两者都是消极处理异常的方式（这里的消极并不是说这种方式不好），只是抛出或者可能抛出异常，但是不会由函数去处理异常，真正的处理异常由函数的上层调用处理。