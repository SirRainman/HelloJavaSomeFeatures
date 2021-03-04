# **java集合**

集合有两个接口，Collection 和 Map

![img](https://img-blog.csdn.net/20131126154350953?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdmtpbmdfd2FuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![img](https://img-blog.csdn.net/20131126154335359?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdmtpbmdfd2FuZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

1. **Set**

- TreeSet：基于红黑树实现，支持有序性操作，例如根据一个范围查找元素的操作。但是查找效率不如 HashSet，HashSet 查找的时间复杂度为 O(1)，TreeSet 则为 O(logN)。底层是TreeMap
- HashSet：基于哈希表实现，支持快速查找，但不支持有序性操作。并且失去了元素的插入顺序信息，也就是说使用 Iterator 遍历 HashSet 得到的结果是不确定的。底层是HashMap
- LinkedHashSet：具有 HashSet 的查找效率，并且内部使用双向链表维护元素的插入顺序。

2. **List**

- ArrayList：基于动态数组实现，支持随机访问。
- Vector：和 ArrayList 类似，但它是线程安全的。
- LinkedList：基于双向链表实现，只能顺序访问，但是可以快速地在链表中间插入和删除元素。不仅如此，LinkedList 还可以用作栈、队列和双向队列。

3. **Queue**

- LinkedList：可以用它来实现双向队列。
- PriorityQueue：基于堆结构实现，可以用它来实现优先队列。

**MAP**



- TreeMap：基于红黑树实现。
- HashMap：基于哈希表实现。
- HashTable：和 HashMap 类似，但它是线程安全的，这意味着同一时刻多个线程同时写入 HashTable 不会导致数据不一致。它是遗留类，不应该去使用它，而是使用 ConcurrentHashMap 来支持线程安全，ConcurrentHashMap 的效率会更高，因为 ConcurrentHashMap 引入了分段锁。
- LinkedHashMap：使用双向链表来维护元素的顺序，顺序为插入顺序或者最近最少使用（LRU）顺序。

# 二、容器中的设计模式

## 迭代器模式

**让用户通过特定的接口访问容器的数据，不需要了解容器内部的数据结构。**

Collection 继承了 Iterable 接口，其中的 iterator() 方法能够产生一个 Iterator 对象，通过这个对象就可以迭代遍历 Collection 中的元素。

从 JDK 1.5 之后可以使用 foreach 方法来遍历实现了 Iterable 接口的聚合对象。

```
List<String> list = new ArrayList<>();
list.add("a");
list.add("b");
for (String item : list) {
    System.out.println(item);
}
```

## 适配器模式

将一个接口转换成客户希望的另一个接口，使接口不兼容的那些类可以一起工作

java.util.Arrays#asList() 可以把数组类型转换为 List 类型。

```
@SafeVarargs
public static <T> List<T> asList(T... a)
```

应该注意的是 asList() 的参数为泛型的变长参数，不能使用基本类型数组作为参数，只能使用相应的包装类型数组。

```
Integer[] arr = {1, 2, 3};
List list = Arrays.asList(arr);
```

也可以使用以下方式调用 asList()：

```
List list = Arrays.asList(1, 2, 3);
```

使用工具类Arrays.asList()把数组转换成集合时，不能使用其修改集合相关的方法，它的add/remove/clear方法会抛出UnsupportOperationException异常说明：asList的返回对象是一个Arrays内部类,并没有实现集合的修改方法。**Arrays.asList体现的是适配器模式，只是转换接口，后台的数据仍是数组。**

String[] str = new String[]{"1","2"};

List list = Arrays.asList(str);

第一种情况：list.add("x");//java.lang.UnsupportedOperationException

第二种情况:str[0] = "unv";//那么list.get(0)也随着修改。

## ArrayList

### 1. 概览

因为 ArrayList 是**基于数组实现的**，所以支持快速随机访问。RandomAccess 接口标识着该类支持快速随机访问。

```
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```

数组的**默认大小为 10**。

```
private static final int DEFAULT_CAPACITY = 10;
```

### 2. 扩容

添加元素时使用 ensureCapacityInternal() 方法来保证容量足够，如果不够时，需要使用 grow() 方法进行扩容，新容量的大小为 `oldCapacity + (oldCapacity >> 1)`，也就是旧容量的 1.5 倍。

**扩容为1.5倍，调用 `Arrays.copyOf()` 把原数组复制到新数组中，**这个操作代价很高，因此最好在创建 ArrayList 对象时就指定大概的容量大小，减少扩容操作的次数。

```
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
}

private void ensureExplicitCapacity(int minCapacity) {
    modCount++;
    // overflow-conscious code
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}

private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

### 3. 删除元素

需要调用 System.arraycopy() 将 index+1 后面的元素都复制到 index 位置上，该操作的时间复杂度为 O(N)，可以看到 ArrayList 删除元素的代价是非常高的。

```
public E remove(int index) {
    rangeCheck(index);
    modCount++;
    E oldValue = elementData(index);
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null; // clear to let GC do its work
    return oldValue;
}
```

### 4. Fail-Fast

集合里会有一个内部类实现iterator接口，迭代器iterator遍历集合类时，调用next方法会比较modcount

modCount 用来记录 ArrayList 结构发生变化的次数。调用add，remove时modcount就会+1。改变了需要抛出 ConcurrentModificationException。

https://blog.csdn.net/zymx14/article/details/78394464

## Vector废弃类

**1. 同步**

它的实现与 ArrayList 类似，但是使用了 synchronized 进行同步。

**2. 与 ArrayList 的比较**

- Vector 是同步的，因此开销就比 ArrayList 要大，访问速度更慢。最好使用 ArrayList 而不是 Vector，因为同步操作完全可以由程序员自己来控制；
- Vector 每次扩容请求其大小的 2 倍（也可以通过构造函数设置增长的容量），而 ArrayList 是 1.5 倍。

**3. 替代方案**

可以使用 `Collections.synchronizedList();` 得到一个线程安全的 ArrayList。

```
List<String> list = new ArrayList<>();
List<String> synList = Collections.synchronizedList(list);
```

也可以使用 concurrent 并发包下的 CopyOnWriteArrayList 类。

```
List<String> list = new CopyOnWriteArrayList<>();
```

## CopyOnWriteArrayList

### 1. 读写分离

写操作在一个复制的数组上进行，读操作还是在原始数组中进行，读写分离，互不影响。

写操作需要加锁，防止并发写入时导致写入数据丢失。

写操作结束之后需要把原始数组指向新的复制数组。

```
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len + 1);
        newElements[len] = e;
        setArray(newElements);
        return true;
    } finally {
        lock.unlock();
    }
}

final void setArray(Object[] a) {
    array = a;
}
@SuppressWarnings("unchecked")
private E get(Object[] a, int index) {
    return (E) a[index];
}
```

### 2. 适用场景

**CopyOnWriteArrayList 适合读多写少的应用场景。**

缺陷是：

- **写时需要复制一个新的数组，使得内存占用是原来的两倍；**
- **读操作不能读取实时性的数据，产生一致性问题。**

所以 CopyOnWriteArrayList 不适合内存敏感以及对实时性要求很高的场景。

## LinkedList

### 1. 概览

是一个双向链表

### 2. 与 ArrayList 的比较

ArrayList 是个动态数组，LinkedList 是个双向链表实现。两者的区别就是数组和链表的区别：

- 数组访问时间复杂度O1，但插入删除时间复杂度On；
- 链表访问时间复杂度On，但插入删除时间复杂度O1；

## 平衡二叉树

左旋    /   左右 /    右旋  \      	右左   \

​		/				\				\				 /

## 红黑树

红黑树的特性:
 （1）节点有红黑两种颜色。根节点和叶子结点（空结点）是黑色。
 （2）如果一个节点是红色的，则它的子节点必须是黑色的。
 （3）从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。

## TreeMap

TreeMap中的元素默认按照keys的自然排序排列。能便捷的实现对其内部元素的各种排序。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190613185311129.png)

继承了AbstractMap抽象类，实现了Map接口；
NavigableMap继承了SortedMap类：
public interface NavigableMap<K,V> extends SortedMap<K,V>
也就是这个类，决定了TreeMap与HashMap的不同：HashMap的key是无序的，TreeMap的key是有序的。
TreeMap底层采用红黑树的数据结构实现的 。其树节点Entry实现了Map.Entry，采用内部类的方式实现

```java
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color = BLACK;
    
    //
}
```

put方法

```java
public V put(K key, V value) {
        Entry<K,V> t = root;  //根节点
        if (t == null) {           如果根节点为空，则创建一个根节点。
            compare(key, key); // type (and possibly null) check

            root = new Entry<>(key, value, null);
            size = 1;
            modCount++;  修改modCount;
            return null;
        }
        int cmp;
        Entry<K,V> parent;
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            do {
                parent = t;
                cmp = cpr.compare(key, t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else     如果当前节点的key和新插入的key相等的话，则覆盖map的value，返回
                    return t.setValue(value);
            } while (t != null);    t为null，即没有要比较的节点了，说明找到该插入的位置。
        }								
        else {        如果没有比较器
            if (key == null)    用key时必须实现Comparable接口，且不能为空
                throw new NullPointerException();
            Comparable<? super K> k = (Comparable<? super K>) key;
            do {
                parent = t;
                cmp = k.compareTo(t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else        同上
                    return t.setValue(value);
            } while (t != null);     同上   
        } 
        Entry<K,V> e = new Entry<>(key, value, parent);  创建新节点对象
        if (cmp < 0)     如果新节点key的值小于父节点key的值，则插在父节点的左侧
            parent.left = e;
        else
            parent.right = e;   否则，插在右侧。
        fixAfterInsertion(e); //  为了保持红黑性质，调整此树
        size++;      map元素个数加1
        modCount++;  有插入则改变加1
        return null;
    }
```

插入后调整的fixAfterInsertion函数

```java
private void fixAfterInsertion(Entry<K,V> x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry<K,V> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Entry<K,V> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }
```

左旋和右旋

```java
private void rotateLeft(Entry<K,V> p) {
        if (p != null) {
            Entry<K,V> r = p.right;
            p.right = r.left;
            if (r.left != null)
                r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null)
                root = r;
            else if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
            r.left = p;
            p.parent = r;
        }
    }

    /** From CLR */
    private void rotateRight(Entry<K,V> p) {
        if (p != null) {
            Entry<K,V> l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null)
                root = l;
            else if (p.parent.right == p)
                p.parent.right = l;
            else p.parent.left = l;
            l.right = p;
            p.parent = l;
        }
    }
```

# HashMap

### 构造函数

```java
    // 1.无参构造方法、
    // 构造一个空的HashMap，初始始容量为16，负载因子为0.75
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
```

无参构造方法。初始容量为16，负载因子为0.75

`tableSizeFor()`

`tableSizeFor()`的主要功能是返回一个比给定整数大且最接近的2的幂次方整数，如给定10，返回2的4次方16.

我们进入`tableSizeFor(int cap)`的源码中看看：

```java
    //Returns a power of two size for the given target capacity.
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```

**note：** HashMap要求容量必须是2的幂。

原因： 1 扩容两倍，位运算左移一位效率高

​			2  (n - 1) & hash 计算效率高

![img](https://upload-images.jianshu.io/upload_images/5679451-42e4ccca9e5b0392.png?imageMogr2/auto-orient/strip|imageView2/2/w/631/format/webp)

tableSizeFor示例图

### 成员变量

介绍putVal方法前，说一下HashMap的几个重要的成员变量：

```java

    //实际存储key，value的数组，只不过key，value被封装成Node了
    transient Node<K,V>[] table;

    transient int size;

    //fail-fast.  (See ConcurrentModificationException).

    transient int modCount;

    int threshold;

    final float loadFactor;
```

其实就是哈希表。HashMap使用链表法避免哈希冲突（相同hash值），当链表长度大于TREEIFY_THRESHOLD（默认为8）时，将链表转换为红黑树，当然小于UNTREEIFY_THRESHOLD（默认为6）时，又会转回链表以达到性能均衡。 我们看一张HashMap的数据结构（数组+链表+红黑树 ）n就更能理解table了：

![img](https://upload-images.jianshu.io/upload_images/5679451-c6e80d30b6653324.png?imageMogr2/auto-orient/strip|imageView2/2/w/552/format/webp)

HashMap的数据结构

再回到putMapEntries函数中，如果table为null，那么这时就设置合适的threshold，如果不为空并且指定的map的size>threshold，那么就**resize()**。然后把指定的map的所有Key，Value，通过**putVal**添加到我们创建的新的map中。

### 插入table的位置

**高16位异或低16位**

```java
/**
     * key 的 hash值的计算是通过hashCode()的高16位异或低16位实现的：(h = k.hashCode()) ^ (h >>> 16)
     * 主要是从速度、功效、质量来考虑的，这么做可以在数组table的length比较小的时候
     * 也能保证考虑到高低Bit都参与到Hash的计算中，同时不会有太大的开销
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

异或运算：(h = key.hashCode()) ^ (h >>> 16)

这样做的好处是，可以将hashcode高位和低位的值进行混合做异或运算，而且混合后，低位的信息中加入了高位的信息，这样高位的信息被变相的保留了下来。掺杂的元素多了，那么生成的hash值的随机性会增大。

然后所得hash值对length-1做与运算（这就是length必须是2的幂的原因）。

### resize

![img](https://upload-images.jianshu.io/upload_images/5679451-c99e998989cb9e19.png?imageMogr2/auto-orient/strip|imageView2/2/w/570/format/webp)

**创建一个容量为oldcapacity两倍的数组，再把旧数组的结点rehash过去。桶坐标位置的计算相比1.7做了优化，是通过hash值对oldcapacity做与运算，如果结果是0就在原位置，是1就在加上oldcapacity的位置。**

为什么？hash值是

源码：

```java
    final Node<K,V>[] resize() {
        // 保存当前table
        Node<K,V>[] oldTab = table;
        // 保存当前table的容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 保存当前阈值
        int oldThr = threshold;
        // 初始化新的table容量和阈值 
        int newCap, newThr = 0;
        /*
        1. resize（）函数在size　> threshold时被调用。oldCap大于 0 代表原来的 table 表非空，
           oldCap 为原表的大小，oldThr（threshold） 为 oldCap × load_factor
        */
        if (oldCap > 0) {
            // 若旧table容量已超过最大容量，更新阈值为Integer.MAX_VALUE（最大整形值），这样以后就不会自动扩容了。
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
             // 容量翻倍，使用左移，效率更高
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                // 阈值翻倍
                newThr = oldThr << 1; // double threshold
        }
        /*
        2. resize（）函数在table为空被调用。oldCap 小于等于 0 且 oldThr 大于0，代表用户创建了一个 HashMap，但是使用的构造函数为      
           HashMap(int initialCapacity, float loadFactor) 或 HashMap(int initialCapacity)
           或 HashMap(Map<? extends K, ? extends V> m)，导致 oldTab 为 null，oldCap 为0， oldThr 为用户指定的 HashMap的初始容量。
    　　*/
        else if (oldThr > 0) // initial capacity was placed in threshold
            //当table没初始化时，threshold持有初始容量。还记得threshold = tableSizeFor(t)么;
            newCap = oldThr;
        /*
        3. resize（）函数在table为空被调用。oldCap 小于等于 0 且 oldThr 等于0，用户调用 HashMap()构造函数创建的　HashMap，所有值均采用默认值，oldTab（Table）表为空，oldCap为0，oldThr等于0，
        */
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 新阈值为0
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        // 初始化table
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            // 把 oldTab 中的节点　reHash 到　newTab 中去
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    // 若节点是单个节点，直接在 newTab　中进行重定位
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    // 若节点是　TreeNode 节点，要进行 红黑树的 rehash　操作
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    // 若是链表，进行链表的 rehash　操作
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        // 将同一桶中的元素根据(e.hash & oldCap)是否为0进行分割（代码后有图解，可以回过头再来看），分成两个不同的链表，完成rehash
                        do {
                            next = e.next;
                            // 根据算法　e.hash & oldCap 判断节点位置rehash　后是否发生改变
                            //最高位==0，这是索引不变的链表。
                            if ((e.hash & oldCap) == 0) { 
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            //最高位==1 （这是索引发生改变的链表）
                            else {  
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {  // 原bucket位置的尾指针不为空(即还有node)  
                            loTail.next = null; // 链表最后得有个null
                            newTab[j] = loHead; // 链表头指针放在新桶的相同下标(j)处
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            // rehash　后节点新的位置一定为原来基础上加上　oldCap，具体解释看下图
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
}
```

我们使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再oldcapacity的位置。看下图可以明白这句话的意思，n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。

![img](https://upload-images.jianshu.io/upload_images/5679451-a8f2e1917e6c188a.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：

![img](https://upload-images.jianshu.io/upload_images/5679451-b2d09c67373f217a.png?imageMogr2/auto-orient/strip|imageView2/2/w/1064/format/webp)

hashMap 1.8 哈希算法例图2.png

因此，我们在扩充HashMap的时候，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，可以看看下图为16扩充为32的resize示意图 ：

![img](https://upload-images.jianshu.io/upload_images/5679451-d193fbb84c53dd09.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

**什么时候扩容：**通过HashMap源码可以看到是在put操作时，即向容器中添加元素时，判断当前容器中元素的个数是否达到阈值（当前数组长度乘以加载因子的值）的时候，就要自动扩容了。

**扩容(resize)：**其实就是重新计算容量；而这个扩容是计算出所需容器的大小之后重新定义一个新的容器，将原来容器中的元素放入其中。

### putVal

**首先判断Node数组table[i]是否为空，空的话就调resize()扩容；**

**根据key值求出插入位置i**（讲一下如何确定），**如果位置为null，直接新建节点添加；**

**否则，判断table[i]的首个元素是否和key一样，如果相同直接覆盖value；**

**不相同的话，判断table[i] 是否为红黑树结点，如果是红黑树，则直接在树中插入键值对；**

**如果不是红黑树，遍历table[i]链表，如果找到key完全相等的节点，则用新节点替换老节点，否则，在尾部插入新节点，然后判断链表长度是否大于8，大于8的话把链表转换为红黑树。**

**插入成功后，修改modcount，并判断键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。**

上源码：

```java
    //实现put和相关方法。
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        //如果table为空或者长度为0，则resize()
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        //确定插入table的位置，算法是(n - 1) & hash，在n为2的幂时，相当于取摸操作。
        ////找到key值对应的槽并且是第一个，直接加入
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        //在table的i位置发生碰撞，有两种情况，1、key值是一样的，替换value值，
        //2、key值不一样的有两种处理方式：2.1、存储在i位置的链表；2.2、存储在红黑树中
        else {
            Node<K,V> e; K k;
            //第一个node的hash值即为要加入元素的hash
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            //2.2
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            //2.1
            else {
                //不是TreeNode,即为链表,遍历链表
                for (int binCount = 0; ; ++binCount) {
                ///链表的尾端也没有找到key值相同的节点，则生成一个新的Node,
                //并且判断链表的节点个数是不是到达转换成红黑树的上界达到，则转换成红黑树。
                    if ((e = p.next) == null) {
                         // 创建链表节点并插入尾部
                        p.next = newNode(hash, key, value, null);
                        ////超过了链表的设置长度8就转换成红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            //如果e不为空就替换旧的oldValue值
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

> 注：hash 冲突发生的几种情况：
> 1.两节点key 值相同（hash值一定相同），导致冲突；
> 2.两节点key 值不同，由于 hash 函数的局限性导致hash 值相同，冲突；
> 3.两节点key 值不同，hash 值不同，但 hash 值对数组长度取模后相同，冲突；

### 为什么是8？

![img](https://img2018.cnblogs.com/blog/1766855/201910/1766855-20191013182222710-422191263.png)

意思就是HashMap节点分布遵循泊松分布，按照泊松分布的计算公式计算出了链表中元素个数和概率的对照表，可以看到**链表中元素个数为8时的概率已经非常小**。

**红黑树平均查找长度是log(n)，长度为8的时候，平均查找长度为3，如果继续使用链表，平均查找长度为8/2=4，这才有转换为树的必要**。链表长度如果是小于等于6，6/2=3，虽然速度也很快的，但是链表和红黑树之间的转换也很耗时。

## 1.7和1.8比较

（1）JDK1.7用的是头插法，而JDK1.8及之后使用的都是尾插法。头插法就是能够提高插入的效率，但是resize后是逆序的。这会造成并发下同时resize,第一个线程标记完e和next之后，第二个线程执行resize，此时e和next位置对调了，第一个线程再resize就会出现死循环。https://www.jianshu.com/p/619a8efcf589

​	DK1.8之后是因为加入了红黑树使用尾插法，能够避免出现逆序且链表死循环的问题。

（2）扩容后数据存储位置的计算方式也不一样：

链表扩容rehash时

1. JDK1.7是直接用hash值和新的容量-1进行&运算 。
2. JDK1.8是和扩容前容量与运算，只需要判断Hash值的新增参与运算的位是0还是1，也就是扩容前的原始位置+扩容的大小值=JDK1.8的计算方式。

（3）JDK1.7的时候使用的是数组+ 单链表的数据结构。但是在JDK1.8及之后时，使用的是数组+链表+红黑树的数据结构（当链表的深度达到8的时候，也就是默认阈值，就会自动扩容把链表转成红黑树的数据结构来把时间复杂度从O（N）变成O（logN）提高了效率）。

## HashMap为什么线程不安全？

**resize而引起死循环**

jdk1.7头插法才有这种问题，jdk1.8改为尾插法已经修复。

由于JDK1.7用的头插法，resize前后元素顺序倒置。当2个线程同时扩容，有可能出现循环链表，接下来再put（）get()获取不存在的元素，就会出现死循环。

**put的时候导致的多线程数据不一致**
线程A和B做put操作，计算记录所要落到的 hash桶的索引坐标，这个位置为null，此时线程A的时间片用完了，而此时线程B被调度得以执行，两者hash桶索引相同，线程B成功插入。线程A再次被调度运行，它不知道持有链表头已经被更新，覆盖了线程B插入的记录，造成了数据不一致的行为。



## HashMap和HashTable的区别

1. Hashtable 使用 synchronized 来进行同步。

2. HashMap 可以插入键为 null 的 Entry。

3. HashMap 的迭代器是 fail-fast 迭代器。

   迭代器调next()方法时，会先检查modcount。若发生改变，将会抛出ConcurrentModificationException异常。

4. HashMap 不能保证随着时间的推移 Map 中的元素次序是不变的。

**HashMap可以通过下面的语句进行同步：
Map m = Collections.synchronizeMap(hashMap);**

# ConcurrentHashMap

参考文献 https://blog.csdn.net/qq_35425070/article/details/84672765

​				https://blog.csdn.net/sihai12345/article/details/79383766



### jdk 1.8

#### 读 Get

在1.8中ConcurrentHashMap的get操作全程不需要加锁。因为**Node的成员val是用volatile修饰的**。

和数组用volatile修饰没有关系，数组用volatile修饰主要是保证在数组扩容的时候保证可见性。

#### 写 put

1.8用的是Synchronized + CAS

首先，判断key、value是否为null，若为null，则抛出异常，然后计算key的hash值。

接下来进入死循环，判断table数组是否为空，若为空则初始化

 根据key的hash值取出table表中的结点元素，若取出的结点为空（该桶为空），则使用CAS将key、value、hash值生成的结点放入桶中。

否则，若该结点的的hash值为-1，说明其他线程在扩容，会帮忙扩容。

否则，就用synchronize锁桶中的第一个结点，判断是链表结点还是红黑树结点，链表结点对该桶进行遍历，桶中的结点的hash值与key值与给定的hash值和key值相等，则根据标识选择是否进行更新操作（用给定的value值替换该结点的value值），若遍历完桶仍没有找到hash值与key值和指定的hash值与key值相等的结点，则直接新生一个结点并赋值为之前最后一个结点的下一个结点。红黑树结点就按红黑树方式插入

 若binCount大于8，则将链表转化为红黑树，最后，增加binCount的值，可能会引发扩容。

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}

final V putVal(K key, V value, boolean onlyIfAbsent) {
    // 键和值都不允许为null
    if (key == null || value == null) throw new NullPointerException();
    // 得到 hash 值
    int hash = spread(key.hashCode());
    // 用于记录相应链表的长度
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        // 如果数组"空"，进行数组初始化
        if (tab == null || (n = tab.length) == 0)
            // 初始化数组，后面会详细介绍
            tab = initTable();

        // 找该 hash 值对应的数组下标，得到第一个节点 f
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // 如果数组该位置为空，
            //    用一次 CAS 操作将这个新值放入其中即可，这个 put 操作差不多就结束了，可以拉到最后面了
            //          如果 CAS 失败，那就是有并发操作，进到下一个循环就好了
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        // hash 居然可以等于 MOVED，这个需要到后面才能看明白，不过从名字上也能猜到，肯定是因为在扩容
        else if ((fh = f.hash) == MOVED)
            // 帮助数据迁移，这个等到看完数据迁移部分的介绍后，再理解这个就很简单了
            tab = helpTransfer(tab, f);

        else { // 到这里就是说，f 是该位置的头结点，而且不为空

            V oldVal = null;
            // 获取数组该位置的头结点的监视器锁
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) { // 头结点的 hash 值大于 0，说明是链表
                        // 用于累加，记录链表的长度
                        binCount = 1;
                        // 遍历链表
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 如果发现了"相等"的 key，判断是否要进行值覆盖，然后也就可以 break 了
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            // 到了链表的最末端，将这个新值放到链表的最后面
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) { // 红黑树
                        Node<K,V> p;
                        binCount = 2;
                        // 调用红黑树的插值方法插入新节点
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            // binCount != 0 说明上面在做链表操作
            if (binCount != 0) {
                // 判断是否要将链表转换为红黑树，临界值和 HashMap 一样，也是 8
                if (binCount >= TREEIFY_THRESHOLD)
                    // 这个方法和 HashMap 中稍微有一点点不同，那就是它不是一定会进行红黑树转换，
                    // 如果当前数组的长度小于 64，那么会选择进行数组扩容，而不是转换为红黑树
                    //    具体源码我们就不看了，扩容部分后面说
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 
    addCount(1L, binCount);
    return null;
}
```

#### TryPresize

```java
// 首先要说明的是，方法参数 size 传进来的时候就已经翻了倍了
private final void tryPresize(int size) {
    // c：size 的 1.5 倍，再加 1，再往上取最近的 2 的 n 次方。
    int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
        tableSizeFor(size + (size >>> 1) + 1);
    int sc;
    while ((sc = sizeCtl) >= 0) {
        Node<K,V>[] tab = table; int n;

        // 这个 if 分支和之前说的初始化数组的代码基本上是一样的，在这里，我们可以不用管这块代码
        if (tab == null || (n = tab.length) == 0) {
            n = (sc > c) ? sc : c;
            if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if (table == tab) {
                        @SuppressWarnings("unchecked")
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = nt;
                        sc = n - (n >>> 2); // 0.75 * n
                    }
                } finally {
                    sizeCtl = sc;
                }
            }
        }
        else if (c <= sc || n >= MAXIMUM_CAPACITY)
            break;
        else if (tab == table) {
            // 我没看懂 rs 的真正含义是什么，不过也关系不大
            int rs = resizeStamp(n);

            if (sc < 0) {
                Node<K,V>[] nt;
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                // 2. 用 CAS 将 sizeCtl 加 1，然后执行 transfer 方法
                //    此时 nextTab 不为 null
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            // 1. 将 sizeCtl 设置为 (rs << RESIZE_STAMP_SHIFT) + 2)
            //     我是没看懂这个值真正的意义是什么？不过可以计算出来的是，结果是一个比较大的负数
            //  调用 transfer 方法，此时 nextTab 参数为 null
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                         (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
        }
    }
}
```

这个方法的核心在于 sizeCtl 值的操作，首先将其设置为一个负数，然后执行 transfer(tab, null)，再下一个循环将 sizeCtl 加 1，并执行 transfer(tab, nt)，之后可能是继续 sizeCtl 加 1，并执行 transfer(tab, nt)。

所以，可能的操作就是执行 `1 次 transfer(tab, null) + 多次 transfer(tab, nt)`，这里怎么结束循环的需要看完 transfer 源码才清楚。

#### 扩容

**扩容会复制一个长度为原tab数组两倍的nexttab数组，将原来数组节点迁移过去。由于桶坐标位置是哈希值对length - 1做与运算，所以扩容后要么在原坐标，要么再原坐标+oldcapacity的位置。**

**1.8中将迁移任务分成多小任务并发进行。每个线程每次负责一个步长的任务，计算方式是原数组长度除以8除以cpu个数，最小为16。用transferIndex指示扩容位置。第一个线程会将 transferIndex 指向原数组末尾，然后从后往前的步长个任务属于第一个线程，然后将 transferIndex 指向新的位置。**

```
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;

    // stride 在单核下直接等于 n，多核模式下为 (n>>>3)/NCPU，最小值是 16
    // stride 可以理解为”步长“，有 n 个位置是需要进行迁移的，
    //   将这 n 个任务分为多个任务包，每个任务包有 stride 个任务
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range

    // 如果 nextTab 为 null，先进行一次初始化
    //    前面我们说了，外围会保证第一个发起迁移的线程调用此方法时，参数 nextTab 为 null
    //       之后参与迁移的线程调用此方法时，nextTab 不会为 null
    if (nextTab == null) {
        try {
            // 容量翻倍
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        // nextTable 是 ConcurrentHashMap 中的属性
        nextTable = nextTab;
        // transferIndex 也是 ConcurrentHashMap 的属性，用于控制迁移的位置
        transferIndex = n;
    }

    int nextn = nextTab.length;

    // ForwardingNode 翻译过来就是正在被迁移的 Node
    // 这个构造方法会生成一个Node，key、value 和 next 都为 null，关键是 hash 为 MOVED
    // 后面我们会看到，原数组中位置 i 处的节点完成迁移工作后，
    //    就会将位置 i 处设置为这个 ForwardingNode，用来告诉其他线程该位置已经处理过了
    //    所以它其实相当于是一个标志。
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);


    // advance 指的是做完了一个位置的迁移工作，可以准备做下一个位置的了
    boolean advance = true;
    boolean finishing = false; // to ensure sweep before committing nextTab

    /*
     * 下面这个 for 循环，最难理解的在前面，而要看懂它们，应该先看懂后面的，然后再倒回来看
     * 
     */

    // i 是位置索引，bound 是边界，注意是从后往前
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;

        // 下面这个 while 真的是不好理解
        // advance 为 true 表示可以进行下一个位置的迁移了
        //   简单理解结局：i 指向了 transferIndex，bound 指向了 transferIndex-stride
        while (advance) {
            int nextIndex, nextBound;
            if (--i >= bound || finishing)
                advance = false;

            // 将 transferIndex 值赋给 nextIndex
            // 这里 transferIndex 一旦小于等于 0，说明原数组的所有位置都有相应的线程去处理了
            else if ((nextIndex = transferIndex) <= 0) {
                i = -1;
                advance = false;
            }
            else if (U.compareAndSwapInt
                     (this, TRANSFERINDEX, nextIndex,
                      nextBound = (nextIndex > stride ?
                                   nextIndex - stride : 0))) {
                // 看括号中的代码，nextBound 是这次迁移任务的边界，注意，是从后往前
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            if (finishing) {
                // 所有的迁移操作已经完成
                nextTable = null;
                // 将新的 nextTab 赋值给 table 属性，完成迁移
                table = nextTab;
                // 重新计算 sizeCtl：n 是原数组长度，所以 sizeCtl 得出的值将是新数组长度的 0.75 倍
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }

            // 之前我们说过，sizeCtl 在迁移前会设置为 (rs << RESIZE_STAMP_SHIFT) + 2
            // 然后，每有一个线程参与迁移就会将 sizeCtl 加 1，
            // 这里使用 CAS 操作对 sizeCtl 进行减 1，代表做完了属于自己的任务
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                // 任务结束，方法退出
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;

                // 到这里，说明 (sc - 2) == resizeStamp(n) << RESIZE_STAMP_SHIFT，
                // 也就是说，所有的迁移任务都做完了，也就会进入到上面的 if(finishing){} 分支了
                finishing = advance = true;
                i = n; // recheck before commit
            }
        }
        // 如果位置 i 处是空的，没有任何节点，那么放入刚刚初始化的 ForwardingNode ”空节点“
        else if ((f = tabAt(tab, i)) == null)
            advance = casTabAt(tab, i, null, fwd);
        // 该位置处是一个 ForwardingNode，代表该位置已经迁移过了
        else if ((fh = f.hash) == MOVED)
            advance = true; // already processed
        else {
            // 对数组该位置处的结点加锁，开始处理数组该位置处的迁移工作
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;
                    // 头结点的 hash 大于 0，说明是链表的 Node 节点
                    if (fh >= 0) {
                        // 下面这一块和 Java7 中的 ConcurrentHashMap 迁移是差不多的，
                        // 需要将链表一分为二，
                        //   找到原链表中的 lastRun，然后 lastRun 及其之后的节点是一起进行迁移的
                        //   lastRun 之前的节点需要进行克隆，然后分到两个链表中
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        // 其中的一个链表放在新数组的位置 i
                        setTabAt(nextTab, i, ln);
                        // 另一个链表放在新数组的位置 i+n
                        setTabAt(nextTab, i + n, hn);
                        // 将原数组该位置处设置为 fwd，代表该位置已经处理完毕，
                        //    其他线程一旦看到该位置的 hash 值为 MOVED，就不会进行迁移了
                        setTabAt(tab, i, fwd);
                        // advance 设置为 true，代表该位置已经迁移完毕
                        advance = true;
                    }
                    else if (f instanceof TreeBin) {
                        // 红黑树的迁移
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            }
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        // 如果一分为二后，节点数少于 8，那么将红黑树转换回链表
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                            (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                            (lc != 0) ? new TreeBin<K,V>(hi) : t;

                        // 将 ln 放置在新数组的位置 i
                        setTabAt(nextTab, i, ln);
                        // 将 hn 放置在新数组的位置 i+n
                        setTabAt(nextTab, i + n, hn);
                        // 将原数组该位置处设置为 fwd，代表该位置已经处理完毕，
                        //    其他线程一旦看到该位置的 hash 值为 MOVED，就不会进行迁移了
                        setTabAt(tab, i, fwd);
                        // advance 设置为 true，代表该位置已经迁移完毕
                        advance = true;
                    }
                }
            }
        }
    }
}
```

#### size

1.8中使用一个volatile类型的变量baseCount记录元素的个数，当插入新数据或则删除数据时，会通过addCount()方法CAS更新baseCount  。如果CAS更新失败，失败的线程使用CounterCell记录元素个数的变化。调用size方法时，累加baseCount和CounterCell数组中的数量即可。

### 1.7 和1.8的区别

1、**整体结构**
1.7：用了分段锁（Segment）继承自重入锁 ReentrantLock，并发度与 Segment 数量相等。，每个分段锁维护着几个桶（HashEntry），多个线程可以同时访问不同分段锁上的桶

1.8: 移除Segment，使锁的粒度更小，Synchronized + CAS + Node + Unsafe

**2、put（）**
1.7：先定位Segment，再定位桶，put全程加锁，没有获取锁的线程提前找桶的位置，并最多自旋64次获取锁，超过则挂起。

1.8：由于移除了Segment，类似HashMap，可以直接定位到桶，拿到first节点后进行判断，1、为空则CAS插入；2、fh = f.hash) == MOVED则说明在扩容，则多线程帮着一起扩容；3、else则用synchronize锁住桶first节点，执行插入操作

**3、get（）**
基本类似，由于value声明为volatile，保证了修改的可见性，因此不需要加锁。

**4、resize（）**
1.7：跟HashMap步骤一样，只不过是搬到单线程中执行，避免了HashMap在1.7中扩容时死循环的问题，保证线程安全。

1.8：**支持并发扩容**，HashMap扩容在1.8中由头插改为尾插（为了避免死循环问题），ConcurrentHashmap也是，迁移也是从尾部开始，扩容前在桶的头部放置一个hash值为-1的节点，这样别的线程访问时就能判断是否该桶已经被其他线程处理过了。

5、size（）
1.7：很经典的思路：计算两次，如果不变则返回计算结果，若不一致，则锁住所有的Segment求和。

1.8：用baseCount来存储当前的节点个数，这就设计到baseCount并发环境下修改的问题

### 如何在很短的时间内将大量数据插入到ConcurrentHashMap

换句话说，就是提高ConcurrentHashMap的插入效率，我大概说了尽量散列均匀和避免加锁两个点，但是面试官还在问还有什么思路没？

将大批量数据保存到map中有两个地方的消耗将会是比较大的：第一个是扩容操作，第二个是锁资源的争夺。第一个扩容的问题，主要还是要通过配置合理的容量大小和扩容因子，尽可能减少扩容事件的发生；第二个锁资源的争夺，在put方法中会使用synchonized对头节点进行加锁，而锁本身也是分等级的，因此我们的主要思路就是尽可能的避免锁等级。所以，针对第二点，我们可以将数据通过通过ConcurrentHashMap的spread方法进行预处理，这样我们可以将存在hash冲突的数据放在一个组里面，每个组都使用单线程进行put操作，这样的话可以保证锁仅停留在偏向锁这个级别，不会升级，从而提升效率。

用线程池或者信号量来控制线程数量是必须的。另外，可以将多组Hash冲突的数据集合到一个线程就OK，只需要确保数据上只有一个线程持有锁，锁就不会升级了。



## LinkedHashMap

### 存储结构

继承自 HashMap，因此具有和 HashMap 一样的快速查找特性。

```
public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
```

内部维护了一个双向链表，用来维护插入顺序或者 LRU 顺序。

```
/**
 * The head (eldest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> head;

/**
 * The tail (youngest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> tail;
```

accessOrder 决定了顺序，默认为 false，此时维护的是插入顺序。

```
final boolean accessOrder;
```

LinkedHashMap 最重要的是以下用于维护顺序的函数，它们会在 put、get 等方法中调用。

```
void afterNodeAccess(Node<K,V> p) { }
void afterNodeInsertion(boolean evict) { }
```

### afterNodeAccess()

当一个节点被访问时，如果 accessOrder 为 true，则会将该节点移到链表尾部。也就是说指定为 LRU 顺序之后，在每次访问一个节点时，会将这个节点移到链表尾部，保证链表尾部是最近访问的节点，那么链表首部就是最近最久未使用的节点。

```
void afterNodeAccess(Node<K,V> e) { // move node to last
    LinkedHashMap.Entry<K,V> last;
    if (accessOrder && (last = tail) != e) {
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a != null)
            a.before = b;
        else
            last = b;
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
        tail = p;
        ++modCount;
    }
}
```

### afterNodeInsertion()

在 put 等操作之后执行，当 removeEldestEntry() 方法返回 true 时会移除最晚的节点，也就是链表首部节点 first。

evict 只有在构建 Map 的时候才为 false，在这里为 true。

```
void afterNodeInsertion(boolean evict) { // possibly remove eldest
    LinkedHashMap.Entry<K,V> first;
    if (evict && (first = head) != null && removeEldestEntry(first)) {
        K key = first.key;
        removeNode(hash(key), key, null, false, true);
    }
}
```

removeEldestEntry() 默认为 false，如果需要让它为 true，需要继承 LinkedHashMap 并且覆盖这个方法的实现，这在实现 LRU 的缓存中特别有用，通过移除最近最久未使用的节点，从而保证缓存空间足够，并且缓存的数据都是热点数据。

```
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return false;
}
```

### LRU 缓存

以下是使用 LinkedHashMap 实现的一个 LRU 缓存：

- 设定最大缓存空间 MAX_ENTRIES 为 3；
- 使用 LinkedHashMap 的构造函数将 accessOrder 设置为 true，开启 LRU 顺序；
- 覆盖 removeEldestEntry() 方法实现，在节点多于 MAX_ENTRIES 就会将最近最久未使用的数据移除。

```
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final int MAX_ENTRIES = 3;

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > MAX_ENTRIES;
    }

    LRUCache() {
        super(MAX_ENTRIES, 0.75f, true);
    }
}
```



## 阻塞队列

ArrayBlockingQueue：采用**数组实现的有界阻塞队列**，按照先进先出的原则，初始化时，需要**指定容量的大小**，一旦创建，容量就不能改变。采用可重入**锁**进行并发控制，添加操作和移除操作采用的同一个锁。
LinkedBlockingQueue：采用**单向链表实现的无界阻塞队列**，**默认容量为Integer.MAX_VALUE**，按照先进先出的原则，**锁**是分离的，添加和移除操作使用两个不同的锁，在高并发场景下，生产者和消费者可以并行的操作队列中的数据，所以提高了并发性能。
PriorityBlockingQueue：支持优先级排序的阻塞队列。可以通过实现 compareTo 方法来指定元素比较规则，也可以使用 Comparator 比较器来指定比较规则。Comparable和Comparator的区别
SynchronousQueue：不保存元素的阻塞队列，直接新建一个线程来处理新任务。轻量级的ArrayBlockingQueue，在只有一个生产者和一个消费者的场景下性能更优。
![image-20200220230357164](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200220230357164.png)

**阻塞队列底层通过Reentrantlock和两个condition来控制。put方法和take这对方法是阻塞的，put方法先调用lockInterruptibly()方法加锁，然后判断是否满，满则调用notFull.await()方法阻塞，否则就调用ENqueue方法插入，插入后调用notEmpty.singal()唤醒消费者**

lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();



## 问题总结

- ​        		

- ​        ArrayList 初始化时数组的默认长度是多少？ 

- ​		ArrayList 扩容是扩容多少倍？扩容后是用原来的数组还是新的数组？ 

- ​		ArrayList 是一个线程安全的集合类吗？ 

- ​		判断一个集合类是否为线程安全的机制是什么？

  ​		多线程访问时是否需要手动同步 

- ​		说一下 Fail-Fast 机制，结合源码说一下（如果可以的话） 

- ​		ArrayList 和 LinkedList 的使用场景 

- ​        ArrayList的扩容

- ​        为什么ArrayList的查询时间复杂度为O（1）？为什么数组查询可以到O（1）

  

- ​		说一下 HashMap 的底层数据结构 

- ​		说一下 HashMap 的存储逻辑（put() 函数） 

- ​        说一下你对HashMap的理解？说完......put操作的流程大概是怎样的呢？

- ​		HashMap 存储元素时 key 完全一样该怎么处理？ 替换value

- ​		HashMap 的默认长度是多少？扩容是扩成几倍？

- ​        hashmap结构与扩容  

- ​        HashMap链表转红黑树是怎么实现的 

- 

- ​		若两个 key 的 hashcode 值相同但 equals 不同，也就是说它们会插入到同一个桶里，新添加的节点是插入到已有元素的前面还是后面？ 

- ​		为什么 JDK 1.7 是头插法，JDK 1.8 是尾插法？ 

- ​	     hashmap并发读写死循环问题

- 

- ​		JDK 1.8 的 HashMap 是否线程安全？ 

- ​		既然 HashMap 不是线程安全的类，有啥办法解决这个问题？ 

- HashMap、Hashtable

- hashMap和ConcurrentHashMap的区别，如何保证线程安全

- HashMap与ConcurrentHashMap的区别，ConcurrentHashMap的底层原理

- Hashmap，currentHashmap的源码以及解决多线程问题，JDK1.7与1.8的区别

- ConcurrentHashMap 和 HashMap 的区别？为什么 ConcurrentHashMap 会线程安全？ 

- ConcurrentHashMap怎么实现的线程安全

- ConcurrentHashMap 虽然是线程安全的，但它也存在什么问题？ 

- ConcurrentHashMap迭代器是强一致性还是弱一致性？HashMap呢？

  弱一致性，hashmap强一致性。

  ConcurrentHashMap可以支持在迭代过程中，向map添加新元素，而HashMap则抛出了ConcurrentModificationException，

  因为HashMap包含一个修改计数器，当你调用他的next()方法来获取下一个元素时，迭代器将会用到这个计数器。

- 现在有一亿条数据，要求你利用HashMap对数据进行去重并排序，你会怎么做？

  

- ​		了解 TreeMap 吗？TreeMap 最大的特点是什么？为什么已经有了 HashMap 了还要有 TreeMap 类？ 

  按自定义顺序排序

- ​		说一下红黑树的特点 