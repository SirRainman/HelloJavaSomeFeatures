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



String.intern()

![img](http://haoimg.hifool.cn/img/v2-dc134981965e3023fc2f9fd20e892672_1440w.jpg)

# List

## ArrayList 和 LinkedList 特点及各自应用场景

### ArrayList：

* ArrayList是基于**Object[] elementData 数组**实现的
* Jdk 7 之前：
    - `new ArrayList()`底层创建了**长度为10**的`Object[]`数组
    - `grow()`方法扩容，**默认扩容为原来的1.5倍**
* Jdk 8 之后：
    - `new ArrayList()`底层`Object[] elementData`初始化为`{}`，并没有创建长度为10的数组
    - 第一次调用`add()`时，底层才创建长度为10的数组，延迟数组创建，节省内存
* 每次add() 时，先**调用ensureCapacity() 保证数组不会溢出**
    * 如果此时已满，会扩展为数组length的1.5倍+1，然后用array.copy的方法，将原数组拷贝到新的数组中；
* **ArrayList线程不安全，Vector方法是同步的，线程安全；**



### LinkedList：

* LinkedList是基于双链表实现的



**使用场景：**

* 对于随机访问的get和set方法，ArrayList要优于LinkedList，因为LinkedList要移动指针。
* 对于新增和删除操作add和remove，LinkedList比较占优势，因为ArrayList要移动数据。

---

# Set - HashSet

1. 底层使用HashMap实现，所有 **value 值使用PRESENT一个Object对象来填充**
2. 不可重复性：
    1. 元素插入时先调用hashcode计算存储位置，如果存储位置相同先比较hash值，如果hash值相同再使用equals()方法，如果返回值为true则舍弃新值，否则拉链发同时存储。
    2. 向Set中添加的数据，其所在类一定要重写hashCode()和equals()方法；



- **HashSet**
    - Set接口主要实现类
    - **可以存储null**
- **LinkedHashSet**
    - HashSet子类
    - **遍历其内部数据时，可以按照添加的顺序遍历**
    - 在添加数据的同时，每个数据还维护了一个双向链表
    - 对于频繁的遍历操作，效率较高一些
- **TreeSet**
    - 使用**红黑树实现**
    - **可以按照添加对象指定属性进行排序**
        - 实现Comparator接口，使用compareTo来比较元素是否相同，传入TreeSet构造方法中
    - 向TreeSet中添加的数据，要求是相同类的对象。 



---

# HashMap



![image-20211115201616145](http://haoimg.hifool.cn/img/image-20211115201616145.png)

* 在Java7叫Entry
* 在Java8中叫Node



## HashMap的默认初始化长度为什么是16？

* 默认数组长度：16（另：初始化大小最好是2的幂）
    * 之所以选择16，是为了服务将Key映射到index的算法。
    * 为什么扩容的大小是2的幂？
        * 只要输入的HashCode本身分布均匀，在保证index计算结果的均匀分布的要求下，**位与运算比取模运算的效率高**
            * index = HashCode（Key） & （Length- 1）
            * Length-1的值是所有二进制位全为1，这种情况下，index的结果等同于HashCode后几位的值。
* `new HashMap()`时，底层还没有创建一个长度为16的数组，第一次add时创建。
* 链表长度大于8，且mapsize > 64的时候
    * 链表会转成红黑树
    * 链表长度大于8，但mapsize < 64时，会优先进行扩容操作



## get()

1. 调用get(key)的时候，会调用key的hashcode方法获得hashcode.
2. 根据hashcode计算相应的index。
3. 由于一个bucket对应的链表中可能存有多个Entry,
    1. 如果在bucket里的第一个节点里直接命中，则直接返回； 
    2. 如果未命中，则通过key.equals(k)去查找对应的Entry;
4. 最后把值返回



## put()

1. 调用key1所在类的hashCode()计算key1哈希值
2. 通过计算（`h & (length - 1)`）得到Entry数组中存放的index位置
3. 如果此位置上数据为空，则entry添加成功
4. 如果此位置上的数据不为空，比较当前位置链表上（拉链法）key1和已经存在的一个或多个数据的哈希值：
    - 如果key1的哈希值与已经存在的数据的哈希值都不相同，此时key1-value1添加成功
    - 如果key1和已经存在的某一个数据的哈希值相同，继续调用key1所在类的equals()方法
        - 如果equals()返回false：添加成功
        - 如果equals()返回true：新值替换旧值
    - 如果碰撞导致链表过长(大于等于TREEIFY_THRESHOLD)，就把链表转换成红黑树(JDK1.8中的改动)；
5. 如果bucket满了(超过load factor*current capacity)，就要resize。



## HashMap - 红黑树

- 数组 + 链表 + 红黑树
    - 当某一个索引位置上的元素以链表形式存在的数据个数 > 8，且mapsize > 64时，此时链表改为使用红黑树存储
    - 查询冲突时提高效率 从O(n)  -> O(log n )
- 用二叉查找树可以么?
    - 二叉查找树在特殊情况下会变成一条线性结构，遍历查找会非常慢。
- 当链表转为红黑树后，什么时候退化为链表?
    - 为6的时候退转为链表，中间有个差值7可以防止链表和树之间频繁的转换。



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





## Hashmap - 节点插入方式

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



并发：

* **Java7在多线程操作HashMap时可能生成环形链表，引起死循环**。
    * 原因是扩容转移后前后链表顺序倒置，在转移过程中修改了原来链表中节点的引用关系。

* Java8在同样的前提下并不会引起死循环，原因是扩容转移后前后链表顺序不变，保持之前节点的引用关系。
* **但是在并发的情况下会出现数据覆盖的问题（多个线程同时创建节点）**

---

## HashTable

* **在所有涉及到多线程操作的都加上了synchronized关键字来锁住整个table**
* **Hashtable 是不允许键或值为 null 的，HashMap 的键值则都可以为 null。**
    * Hashtable在我们put 空值的时候会直接抛空指针异常，但是HashMap却做了特殊处理。
    * Hashtable使用的是**安全失败机制（fail-safe）**，这种机制会使你此次读到的数据不一定是最新的数据。
    * 如果你使用null值，就会使得其无法判断对应的key是不存在还是为空，因为你无法再调用一次contain(key）来对key是否存在进行判断，ConcurrentHashMap同理。





**快速失败（fail—fast）**是java集合中的一种机制， 在用迭代器遍历一个集合对象时，如果遍历过程中对集合对象的内容进行了修改（增加、删除、修改），则会抛出Concurrent Modification Exception。

* 这里异常的抛出条件是检测到 modCount！=expectedmodCount 这个条件。如果集合发生变化时修改modCount值刚好又设置为了expectedmodCount值，则异常不会抛出。

**安全失败（fail—safe）**java.util.concurrent包下的容器都是安全失败，可以在多线程下并发使用，并发修改。

---

## TreeMap

TreeMap实现SortMap接口，能够把它保存的**记录根据键值排序器进行排序。**

```java
public TreeMap(Comparator<? super K> comparator){}

Map<String,String> map = new TreeMap<>(new Comparator<String>(){
    public int compare(String o1,String o2){
        return  o2.compareTo(o1); //用正负表示大小值
    }
});
```

---



## LinkedHashMap

**LinkedHashMap保存了记录的插入顺序**，也可以在构造时用带参数，按照应用次数排序。

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



## JDK版本变动：

* 1.7使用 **ReentrantLock+Segment+HashEntry** 的方式实现
* 1.8使用 **synchronized+CAS+HashEntry+红黑树** 实现，
    * 加入红黑树，避免链表过长导致性能的问题。
    * **JDK1.8的实现降低锁的粒度**
    * JDK1.8为什么使用内置锁 synchronized 来代替重入锁 ReentrantLock？
        1. 因为粒度降低了，
            1. 在低粒度加锁方式下，synchronized并不比ReentrantLock差，
            2. 在粗粒度加锁方式下，ReentrantLock通过Condition来控制各个低粒度的边界，更加的灵活，而在低粒度中，Condition的优势就没有了
        2. 基于JVM的**synchronized优化空间更大**，使用内嵌的关键字比使用API更加自然
        3. 在大量的数据操作下，对于**JVM的内存压力**，基于API的ReentrantLock会开销更多的内存



---



## JDK 1.7：分段锁

从结构上说，1.7版本的ConcurrentHashMap采用分段锁机制

* Segment数组，Segment继承于ReentrantLock，
    * Segment则包含HashEntry的数组
        * HashEntry使用volatile去修饰了他的数据Value，还有下一个节点next。
    * 每个**Segment都是一个HashMap**，默认的Segment长度是16，也就是支持16个线程的并发写，Segment之间相互不会受到影响。

![img](http://haoimg.hifool.cn/img/926638-20170809132445011-2033999443.png)

### put流程

整个流程和HashMap非常类似，只不过是先定位到具体的Segment，然后通过ReentrantLock去操作

1. 第一次计算hash，定位到Segment，Segment还没有初始化，即通过CAS操作进行赋值
    1. 使用ReentrantLock加锁，如果获取锁失败则尝试自旋，自旋超过次数就阻塞获取，保证一定获取锁成功
2. 第二次hash操作，在HashEntry数组中，找到相应的HashEntry的位置，
3. 遍历HashEntry，就是和HashMap一样，数组中key和hash一样就直接替换，不存在就再插入链表，链表同样



### get流程

key通过hash定位到segment，再遍历链表定位到具体的元素上，需要注意的是value是volatile的，所以get是不需要加锁的。

* 第一次key需要经过一次hash定位到Segment的位置，
* 第二次再hash定位到指定的HashEntry，遍历该HashEntry下的链表进行对比，成功就返回，不成功就返回null



### size 流程

计算size的时候，可能会有其他线程在并发的插入数据，要解决这个问题，JDK1.7版本用两种方案：

1. 第一种方案他会使用不加锁的模式去尝试多次计算ConcurrentHashMap的size，最多三次，比较前后两次计算的结果，结果一致就认为当前没有元素加入，计算的结果是准确的
2. 第二种方案是如果第一种方案不符合，他就会给每个Segment加上锁，然后计算ConcurrentHashMap的size返回

---

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
    3. **如果存在hash冲突，就synchronized加锁来保证线程安全，**
        1. 链表长度小于8，链表写入和HashMap的方式一样，key hash一样就覆盖，反之就尾插法
        2. 链表长度超过8就转换成红黑树



### get查询

1. 计算hash值，定位索引位置，如果是首节点符合就返回
2. 如果是红黑树按照红黑树获取，不是就遍历链表获取。
3. 如果遇到扩容的时候，会调用标志正在扩容节点ForwardingNode的find方法，查找该节点，匹配就返回



### size 计算(不懂)

* 指导思想：尽量降低线程冲突，以最快的速度写入 size 的变化。
* JDK8求解size有两个重要变量：
    * baseCount：用于记录节点的个数，是个volatile变量；
    * counterCells数组：每个counterCell存着部分的节点数量，这样做的目的就是尽可能地减少冲突。
* ConcurrentHashMap节点的数量=baseCount+counterCells每个cell记录下来的节点数量和。
    * 统计数量的时候并没有加锁。
* 总体的原则：先尝试更新baseCount，失败再利用CounterCell。
    * 通过CAS尝试更新baseCount 
        * 如果更新成功则完成，
    * 如果CAS更新失败
        * 线程通过随机数ThreadLocalRandom.getProbe() & (n-1) 计算出在counterCells数组的位置，
        * 如果不为null，则CAS尝试在couterCell上直接增加数量，
        * 如果失败，counterCells数组会进行扩容为原来的两倍，继续随机，继续添加



----

# 异常

![img](http://haoimg.hifool.cn/img/690102-20160728164909622-1770558953.png)

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

