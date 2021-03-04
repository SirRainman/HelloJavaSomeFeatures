# Reids原理

Redis是C语言写的内存数据库，是NoSQL数据库

**Redis 底层是 I/O 多路复用的Reactor 模式，封装epoll函数，从而用一个IO多路复用线程监听多个socket，并将到达的事件传送给dispatcher，调用相应的event handler处理。**

redis默认16个库，select切换

redis单线程，因此保证了其原子性。

指令存入队列。

## 网络协议

Redis服务器与客户端通过RESP（REdis Serialization Protocol）协议通信。 

  间隔符号，在Linux下是\r\n，在Windows下是\n

1. 简单字符串 Simple Strings, 以 "+"加号 开头

   格式：+ 字符串 \r\n

​        字符串不能包含 CR或者 LF(不允许换行)

   eg: "+OK\r\n"

   注意：为了发送二进制安全的字符串，一般推荐使用后面的 Bulk Strings类型

2. 错误 Errors, 以"-"减号 开头

　　格式：- 错误前缀 错误信息 \r\n

​        错误信息不能包含 CR或者 LF(不允许换行)，Errors与Simple Strings很相似，不同的是Erros会被当作异常来看待

   eg: "-Error unknow command 'foobar'\r\n"

3. 整数型 Integer， 以 ":" 冒号开头

　　格式：: 数字 \r\n

   eg: ":1000\r\n"

4. 大字符串类型 Bulk Strings, 以 "$"美元符号开头，长度限制512M

　　格式：$ 字符串的长度 \r\n 字符串 \r\n

​        字符串不能包含 CR或者 LF(不允许换行);

   eg: "$**6**\r\n**foobar**\r\n"   其中字符串为 foobar，而6就是foobar的字符长度

​      "$0\r\n\r\n"    空字符串

​      "$-1\r\n"      null

5. 数组类型 Arrays，以 "*"星号开头

　　格式：* 数组元素个数 \r\n 其他所有类型 (结尾不需要\r\n)

​       注意：只有元素个数后面的\r\n是属于该数组的，结尾的\r\n一般是元素的

   eg: "*0\r\n"    空数组

​      "*2\r\n$2\r\nfoo\r\n$3\r\nbar\r\n"    数组包含2个元素，分别是字符串foo和bar

　　　　"*3\r\n:1\r\n:2\r\n:3\r\n"    数组包含3个整数：1、2、3

​      "*5\r\n:1\r\n:2\r\n:3\r\n:4\r\n$6\r\nfoobar\r\n"  包含混合类型的数组

​      "*-1\r\n"     Null数组

​      "*2\r\n*3\r\n:1\r\n:2\r\n:3\r\n*2\r\n+Foo\r\n-Bar\r\n"  数组嵌套，外层数组包含2个数组，整理后如下：

​         "*2\r\n

　　　　　　*3\r\n:1\r\n:2\r\n:3\r\n

　　　　　　*2\r\n+Foo\r\n-Bar\r\n"

https://www.jianshu.com/p/daa3cb672470 

# redis五大数据类型

![img](https://img2018.cnblogs.com/blog/1289934/201906/1289934-20190621163930814-1395015700.png)

## String SDS字符串 

**Value值为String。string类型是Redis最基本的数据类型，一个键最大能存储512MB。底层是SDS字符串。**

```c
struct sdshdr{
    int len;
    int free;
    char buf[]
}
```

**带有长度len，可以o(1)时间获取长度，剩余空间free防止溢出。C字符串不能有空字符，否则会被认为是结尾。**

**sds字符串不用空字符判断结尾，不会过滤其中数据，所以是二进制安全的。可以保存任意格式数据。比如jpg图片或者序列化的对象 。**

**free在拼接字符串时防止溢出。**

格式: set key value  用法 get  set  append  strlen incr decr 

用处：

**1.做缓存： 把常用信息，字符串，图片或者视频等信息放到redis中**

**2.做分布式Session，Key是SessionID，V是用户信息**

**3.计数器 decr incr原子操作**

## Hash 哈希

**Value值由field， value键值对构成。**

**底层是字典，字典由两个哈希表组成，一般只用ht[0]表，ht[1]表在扩容时使用。每个哈希表节点有key，v 和next指针组成，用链地址法解决冲突。**

**当负载因子达到1时，执行扩容，小于0.1时执行缩容。扩容缩容时会用ht[1]表进行rehash，完成后释放ht[0】，并将ht[1]设置成ht[0]**

**为避免对服务器性能影响，采用渐进式rehash，分多次rehash完成。在此期间，同时使用两个哈希表。**

```java
typedef struct dictEntry{
     void *key;//键
     union{
          void *val;
          uint64_tu64;
          int64_ts64;
     }v;//值
     struct dictEntry *next;//指向下一个哈希表节点，形成链表
}dictEntry；
typedef struct dict{
	dictType *type; //类型特定函数
	void *privdata;//私有数据
	dictht ht[2];//一般情况下只使用ht[0]哈希表，ht[1]哈希表只会在进行rehash时使用
	int rehashidx;//rehash索引,当rehash不在进行时，值为-1
}dict;
```

格式: hmset key field1 value1 field2 value2  field3 value3

​		hget key filed1

## List 列表

**Value是一个带头尾指针的双向链表**

lpush/rpush/lrange

lpush 顶部（left)添加字符串

lpop

rpush 底部添加字符串

rpop

brpop 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止

应用场景： 

- lpush+lpop=Stack(栈)
- lpush+rpop=Queue（队列）
- lpush+brpop=Message Queue（消息队列）

## Set 集合

Value值为String类型的无序集合，底层是value为null的hash表

使用场景：Set 可以实现交集、并集等操作，从而实现共同好友等功能。

## Zset 有序集合

**value 值为有序集合。zset是一个有序集合，在set基础上加入score字段，通过score排序。每个zset包含一个跳跃表和字典，**

字典的key值为元素成员，value为分数，作用是在o(1)时间内查找分数

跳跃表：由zskiplist和zskiplistNode定义。

zskiplist包含头尾指针，层数level和节点数length

zskiplistNode中score保存分数，sds字符串保存元素成员。后退指针是从表尾遍历表头时使用。level数组代表每个节点的各层。每层有跨度和前进指针两个属性。前进指针指向表尾方向的另一个节点。span跨度则代表这两个节点的距离。

跳跃表的查找方式是从表头最高层开始，后继节点小于关键字就右移，否则就下移。

**redis使用跳表不用B+树的原因是**：B+树是为mysql这种IO数据库准备的。利用了磁盘预读特性和局部性原理，节点的大小设为等于一个页，一个节点的载入只需要一次I/O。redis存在内存中，没有这种需求。跳跃表插入简单。只要修改前后指针，利于范围查找

```c
typedef struct zskiplistNode {
    sds ele;
    double score;
    struct zskiplistNode *backward;// 后退指针
    struct zskiplistLevel {
        // 前进指针
        struct zskiplistNode *forward;// 前进指针
        unsigned long span;//跨度记录两个节点间距离，查找节点的过程中，将访过的层的跨度相加，得到排位
    } level[];// 层
} zskiplistNode;
typedef struct zskiplist {
    struct zskiplistNode *header, *tail;
    unsigned long length;
    int level;
} zskiplist;
typedef struct zset {
    /*
     * Redis 会将跳跃表中所有的元素和分值组成 
     * key-value 的形式保存在字典中
     * todo：注意：该字典并不是 Redis DB 中的字典，只属于有序集合
     */
    dict *dict;
    /*
     * 底层指向的跳跃表的指针
     */
    zskiplist *zsl;
} zset;
```



 ![img](https://upload-images.jianshu.io/upload_images/10204326-98cf6cafa703e4a1.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/778/format/webp) 

查找：先从最高层开始找，后继比关键字大就右移，比关键字小就下移。

应用场景： 例如视频网站需要对视频做排行榜，榜单可以按照点击量排序。

格式: zadd name score value

Redis zset 和 set 一样也是string类型元素的集合,且不允许重复的成员。

每个元素都会关联一个double类型的分数，通过分数排序。

# 高级数据结构

## Bitmaps

redis bitmaps是一个可以进行位操作的字符串。一个数据用一位存储，节约空间。

## Hyperloglogs

GEO

# Redis事务

单个完整的命令是原子性的。如果需要将**多个命令作为一个不可分割的处理序列**, 就需要使用事务。Redis通过multi exec watch等命令实现事务。底层通过一个事务队列来实现，通过FIFO保存入队命令。

MULTI 事务开始命令  EXEC 事务提交命令

WATCH是一个乐观锁，在EXEX命令执行之前，监视数据库键，如果至少一个被修改过，就将拒绝执行事务。

**Redis不支持事务回滚，一个命令出现错误，后续命令也会继续执行下去。**原因是这种复杂功能和redis简单高效的主题不符。

# Redis 与 Memcached

**数据类型**Memcached 仅支持字符串类型，而 Redis 支持五种不同的数据类型，可以更灵活地解决问题。

**数据持久化**Redis 支持RDB 和AOF 两种持久化方式，而 Memcached 不支持持久化。

**分布式**Memcached 不支持分布式。redis通过Redis Cluster 支持分布式。

**内存管理机制**

- 在 Redis 中，并不是所有数据都一直存储在内存中，可以将一些很久没用的 value 交换到磁盘，而 Memcached 的数据则会一直在内存中。
- Memcached 将内存分割成特定长度的块来存储数据，以完全解决内存碎片的问题。但是这种方式会使得内存的利用率不高，例如块的大小为 128 bytes，只存储 100 bytes 的数据，那么剩下的 28 bytes 就浪费掉了。

# Redis持久化

**什么是Redis持久化？Redis有哪几种持久化方式？优缺点是什么？**

持久化就是把内存的数据写到磁盘中去，防止服务宕机了内存数据丢失。

Redis 提供了两种持久化方式:RDB（默认） 和AOF 

## **RDB：**

rdb是Redis DataBase缩写

**将内存中数据库快照写入磁盘，生成一个二进制RDB文件。恢复时将rdb文件读入内存。可以手动也可以定期执行。生成RDB文件有两种命令，save：主进程执行，阻塞主进程，bgsave： 子进程执行。持久化结束后替换上次持久化的文件。**

比AOF高效，缺点是最后一次持久化之后的数据可能丢失。

功能核心函数rdbSave(生成RDB文件)和rdbLoad（从文件加载内存）两个函数

![img](https://img2018.cnblogs.com/blog/1481291/201809/1481291-20180925141429889-1694430603.png)

## **AOF:**

**Aof是Append-only file缩写，将写命令添加到 AOF 文件（Append Only File）的末尾。**

**对文件进行写入先存储到缓冲区，然后根据同步选项决定什么时候同步到磁盘。**有以下同步选项：

| 选项     | 同步频率                 |
| -------- | ------------------------ |
| always   | 每个写命令都同步         |
| everysec | 每秒同步一次             |
| no       | 让操作系统来决定何时同步 |

- always 选项会严重减低服务器的性能；
- everysec 选项比较合适，可以保证系统崩溃时只会丢失一秒左右的数据，并且 Redis 每秒执行一次同步对服务器性能几乎没有任何影响；
- no 选项并不能给服务器性能带来多大的提升，而且也会增加系统崩溃时数据丢失的数量。

**AOF 文件会越来越大。可以进行 AOF 重写，创建一个新的AOF文件来替代现有的AOF文件， 这个功能是通过读取服务器当前的数据库状态来实现的，不会读取原文件 。**

![img](https://img2018.cnblogs.com/blog/1481291/201809/1481291-20180925141527592-2105439510.png)

每当执行服务器(定时)任务或者函数时flushAppendOnlyFile 函数都会被调用， 这个函数执行以下两个工作

aof写入保存：

WRITE：根据条件，将 aof_buf 中的缓存写入到 AOF 文件

SAVE：根据条件，调用 fsync 或 fdatasync 函数，将 AOF 文件保存到磁盘中。

**存储结构:**

 内容是redis通讯协议(RESP )格式的命令文本存储。

**比较**：

1、aof文件比rdb更新频率高，优先使用aof还原数据。

2、aof比rdb更安全也更大

3、rdb性能比aof好

4、如果两个都配了优先加载AOF

# 架构

## 主从复制

通过使用 slaveof host port 命令来让一个服务器成为另一个服务器的从服务器。

一个从服务器只能有一个主服务器，并且不支持主主复制。

**连接过程**

1. master调用BGSAVE创建rdb快照，发送给slave，并在发送期间使用缓冲区记录执行的写命令。快照文件发送完毕之后，再向slave发送缓冲区中的写命令；
2. slave丢弃所有旧数据，载入master发来的RDB文件，然后接受master发来的写命令；
3. master每次写，就向slave发送相同的写命令。

**主从链**

随着负载上升，可以创建一个中间层，既是最上层服务器的从服务器，又是最下层服务器的主服务器。

**缺点**

无法保证高可用

没有解决 master 写的压力

## 哨兵

Sentinel（哨兵）可以监听集群中的服务器，并在主服务器进入下线状态时，自动从从服务器中选举出新的主服务器。 

哨兵每秒ping一次master，如果规定时间内没有回复，则认为是主观下线。

如果超过一半节点认为主管下线，就会形成客观下线，进行leader选举。

按照 slave-priority->复制偏移量->run id  的优先级进行选举

**特点**

保证高可用，没能解决master写压力

## Redis Cluster

 Redis Cluster 把数据划分为16384个哈希槽，每个master对应一部分哈希槽。 客户端保存Redis Cluster槽的路由表，然后将key 按照CRC16规则进行hash运算，对16384取模得到哈希槽位置，访问对应的redis结点。 

**数据访问**

如果客户端访问的key不在访问节点，则节点会返回MOVED错误，同时把该slot对应的节点告诉客户端。客户端可以去该节点执行命令。

目前客户端有两种做法获取数据分布表：

1. 一种就是客户端每次根据返回的MOVED信息缓存一个slot对应的节点，但是这种做法在初期会经常造成访问两次集群。

2. 还有一种做法是在节点返回MOVED信息后，通过cluster nodes命令获取整个数据分布表，这样就能每次请求到正确的节点，一旦数据分布表发生变化，请求到错误的节点，返回MOVED信息后，重新执行cluster nodes命令更新数据分布表。

    https://www.cnblogs.com/williamjie/p/11132211.html 

## 一致性hash

将key值计算哈希值，映射到0-2^31 -1的环上，顺时针行走遇到的第一台服务器就是定位的服务器。可以通过增加虚拟节点来使得数据映射均匀

https://www.cnblogs.com/lpfuture/p/5796398.html

https://blog.csdn.net/z15732621582/article/details/79121213

 

 

**Redis常用命令？**

Keys pattern

*表示区配所有

以bit开头的

查看Exists key是否存在

Set

设置 key 对应的值为 string 类型的 value。

setnx

设置 key 对应的值为 string 类型的 value。如果 key 已经存在，返回 0，nx 是 not exist 的意思。

删除某个key

第一次返回1 删除了 第二次返回0

Expire 设置过期时间（单位秒）

TTL查看剩下多少时间

返回负数则key失效，key不存在了

Setex

设置 key 对应的值为 string 类型的 value，并指定此键值对应的有效期。

Mset

一次设置多个 key 的值，成功返回 ok 表示所有的值都设置了，失败返回 0 表示没有任何值被设置。

Getset

设置 key 的值，并返回 key 的旧值。

Mget

一次获取多个 key 的值，如果对应 key 不存在，则对应返回 nil。

Incr

对 key 的值做加加操作,并返回新的值。注意 incr 一个不是 int 的 value 会返回错误，incr 一个不存在的 key，则设置 key 为 1

incrby

同 incr 类似，加指定值 ，key 不存在时候会设置 key，并认为原来的 value 是 0

Decr

对 key 的值做的是减减操作，decr 一个不存在 key，则设置 key 为-1

Decrby

同 decr，减指定值。

Append

给指定 key 的字符串值追加 value,返回新字符串值的长度。

Strlen

取指定 key 的 value 值的长度。

persist xxx(取消过期时间)

选择数据库（0-15库）

Select 0 //选择数据库

move age 1//把age 移动到1库

Randomkey随机返回一个key

Rename重命名

Type 返回数据类型

**08**

**使用过Redis分布式锁么，它是怎么实现的？**

 

先拿setnx来争抢锁，抢到之后，再用expire给锁加一个过期时间防止锁忘记了释放。

**如果在setnx之后执行expire之前进程意外crash或者要重启维护了，那会怎么样？**

set指令有非常复杂的参数，这个应该是可以同时把setnx和expire合成一条指令来用的！

**09**

**使用过Redis做异步队列么，你是怎么用的？有什么缺点？**

一般使用list结构作为队列，rpush生产消息，lpop消费消息。当lpop没有消息的时候，要适当sleep一会再重试。

缺点：

在消费者下线的情况下，生产的消息会丢失，得使用专业的消息队列如rabbitmq等。

**能不能生产一次消费多次呢？**

使用pub/sub主题订阅者模式，可以实现1:N的消息队列。

**10**

**什么是缓存穿透？如何避免？什么是缓存雪崩？何如避免？**

缓存穿透

一般的缓存系统，都是按照key去缓存查询，如果不存在对应的value，就应该去后端系统查找（比如DB）。一些恶意的请求会故意查询不存在的key,请求量很大，就会对后端系统造成很大的压力。这就叫做缓存穿透。

如何避免？

1：对查询结果为**空**的情况也进行**缓存**，缓存时间设置短一点，或者该key对应的数据insert了之后清理缓存。

2：对一定不存在的key进行过滤。**布隆过滤器**是一个 bit 向量或者说 bit 数组，过滤一定不存在的key

缓存雪崩

当缓存服务器重启或者大量缓存集中在某一个时间段失效，这样在失效的时候，会给后端系统带来很大压力。导致系统崩溃。

如何避免？

1：在缓存失效后，通过加锁或者队列来控制读数据库写缓存的线程数量。比如对某个key只允许一个线程查询数据和写缓存，其他线程等待。

2：做二级缓存，A1为原始缓存，A2为拷贝缓存，A1失效时，可以访问A2，A1缓存失效时间设置为短期，A2设置为长期

3：不同的key，设置不同的过期时间，让缓存失效的时间点尽量均匀

# 问题

Redis 有哪些数据结构？ 

介绍下 Redis 的基本数据结构 

说一下这 5 种数据结构的底层实现 

说一下你看过的 Redis 源码 

Redis 的哪种数据类型用到了跳表结构？

list 如何实现的异步消息队列？ 

看你项目中用 Redis 中的 List 来实现异步队列，说一下具体是怎么做的？是如何基于 Redis 来实现异步的？有没有一个拉取消息的过程？还是说基于 Redis 你就把它放到队列里，然后有人来处理还是说订阅处理 

Redis 在单线程下实现高并发的？核心的机制是什么？ 

内存，IO多路复用，单线程

![img](https://upload-images.jianshu.io/upload_images/7368936-fe23b577eef07aa3.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

IO 多路复用模型有哪些？ 

select 和 epoll 有什么区别？ 

redis的数据结构,集群是怎么做的

Redis缓存一致性 

redis和 数据库是怎么保持一致性的？ 

## 缓存穿透 缓存雪崩 缓存击穿

**缓存穿透**

恶意请求故意查询不存在的key,请求量很大，就会对后端系统造成很大的压力。

如何避免？

1：对查询结果为**空**的情况也进行**缓存**，缓存时间设置短一点，或者该key对应的数据insert了之后清理缓存。

2：对一定不存在的key进行过滤。**布隆过滤器**是一个 bit 向量或者说 bit 数组，过滤一定不存在的key

**缓存雪崩**

当缓存服务器重启或者大量缓存集中同时失效，此时请求直接穿透到DB，会给后端系统带来很大压力。

如何避免？

1：在缓存失效后，通过加锁或者队列来控制读数据库写缓存的线程数量。比如对某个key只允许一个线程查询数据和写缓存，其他线程等待。

2：做二级缓存，A1为原始缓存，A2为拷贝缓存，A1失效时，可以访问A2，A1缓存失效时间设置为短期，A2设置为长期

3：不同的key，设置不同的过期时间，让缓存失效的时间点尽量均匀

**缓存击穿**

缓存中热点数据过期，大量请求穿透到db

   **解决方案：**

1. 设置热点数据永远不过期。
2. 去数据库获取缓存加分布式锁，只有第一个线程能去获取缓存。

## redis的一致性问题

多个线程同时进行读写操作，可能会导致一致性问题的产生。

**为什么不更新缓存，而是删除缓存？**

 当更新操作简单，如只是将这个值直接修改为某个值时，更新cache与淘汰cache的消耗差不多
但当更新操作的逻辑较复杂时，需要涉及到其它数据，如用户购买商品付款时，需要考虑折扣，红包，津贴，优惠券等因素，这样需要缓存可能需要与数据库进行多次交互，将打折等信息传入缓存，再与缓存中的其它值进行计算才能得到最终结果，多次交互又会产生新的一致性问题。



先删缓存，后写数据库的缺点：A线程写数据，B线程读数据，A线程删除了缓存，B线程读数据发现缓存没有命中从数据库中读数据，B线程把读出的旧数据写到Redis里，A线程把新数据写回去。

延时双删策略

1）先删除缓存

2）再写数据库

3）休眠500毫秒

4）再次删除缓存

Redis缓存的实现方式 	

java用jedis

## Redis的缓存淘汰策略

删除方式：定期删除+惰性删除

定期删除是每隔100ms抽一些key检查是否过期

惰性删除是查询key时检查是否过期，过期则删除

有六种策略，一种是不驱逐，满了就报错。剩下的分为两类，一类是对所有key操作

allkeys-lru: 所有key通用; 优先删除最近最少使用(less recently used ,LRU) 的 key。
allkeys-random: 所有key通用; 随机删除一部分 key。

另一类是对设置了过期时间的key操作

volatile-lru: ; 优先删除最近最少使用(less recently used ,LRU) 的 key。

volatile-random: ; 随机删除一部分 key。
volatile-ttl: ; 优先删除TTL(time to live,TTL) 短的key。

分布式锁的实现方式，zk实现和redis实现哪个比较好

## redis的热点key问题

**热点key：**　　缓存中的某些Key(可能对应用与某个促销商品)对应的value存储在集群中一台机器，使得所有流量涌向同一机器，成为系统的瓶颈，该问题的挑战在于它无法通过增加机器容量来解决。

**热点key的解决方案：**

- 客户端热点key缓存：用一个`HashMap`，将热点key对应value缓存在本地，并且设置一个失效时间。对于每次读请求，将首先检查key是否存在于本地缓存中，如果存在则直接返回，如果不存在再去访问分布式缓存的机器。
- 将热点key分散为多个子key，然后存储到redis集群的不同机器上，当通过热点key去查询数据时，通过某种hash算法随机选择一个子key，然后再去访问缓存机器，将热点分散到了多个子key上。

## Redis并发竞争key

多个redis的client同时set key

**分布式锁+时间戳**


# 补充问题

我将 Redis 面试中常见的题目划分为如下几大部分： 

1. Redis 的概念理解 
2. Redis 基本数据结构详解 
3. Redis 高并发问题策略 
4. Redis 集群结构以及设计理念 
5. Redis 持久化机制 
6. Redis 应用场景设计 

部分涉及到的题目如下：

- 什么是 Redis? 
- Redis 的特点有哪些？ 
- Redis 支持的数据类型 
- 为什么 Redis 需要把所有数据放到内存中？ 
- Redis 适用场景有哪些？ 
- Redis常用的业务场景有哪些？ 
- Mem*** 与 Redis 的区别都有哪些？     
- Redis 相比 mem***d 有哪些优势？ 
- Redis常用的命令有哪些？ 
- Redis 是单线程的吗？ 
- Redis 为什么设计成单线程的？ 
- 一个字符串类型的值能存储最大容量是多少？ 
- Redis各个数据类型最大存储量分别是多少? 
- Redis 持久化机制有哪些？ 区别是什么？ 
- 请介绍一下 RDB, AOF两种持久化机制的优缺点？ 
- 什么是缓存穿透？怎么解决？ 
- 什么是缓存雪崩？ 怎么解决？ 
- Redis支持的额Java客户端有哪些？ 简单说明一下特点。 
- 缓存的更新策略有几种？分别有什么注意事项？ 
- 什么是分布式锁？有什么作用？   
- 分布式锁可以通过什么来实现？  
- 介绍一下分布式锁实现需要注意的事项？  
- Redis怎么实现分布式锁？  
- 常见的淘汰算法有哪些？  
- Redis 淘汰策略有哪些？ 
- Redis 缓存失效策略有哪些？  
- Redis 的持久化机制有几种方式？  
- 请介绍一下持久化机制 RDB, AOF的优缺点分别是什么？ 
- Redis 通讯协议是什么？有什么特点？ 
- 请介绍一下 Redis 的数据类型 SortedSet(zset) 以及底层实现机制？ 
- Redis 集群最大节点个数是多少？ 
- Redis 集群的主从复制模型是怎样的？ 
- Redis 如何做内存优化？ 
- Redis 事务相关命令有哪些？ 
- 什么是 Redis 事务？原理是什么？ 
- Redis 事务的注意点有哪些？  
- Redis 为什么不支持回滚？  
- 请介绍一下 Redis 集群实现方案 
- 请介绍一下 Redis 常见的业务使用场景？ 
- Redis 集群会有写操作丢失吗？为什么？ 
- 请介绍一下 Redis 的 Pipeline (管道)，以及使用场景 
- 请说明一下 Redis 的批量命令与 Pipeline 有什么不同？  
- Redis 慢查询是什么？通过什么配置？ 
- Redis 的慢查询修复经验有哪些？ 怎么修复的？  
- 请介绍一下 Redis 的发布订阅功能 
- 请介绍几个可能导致 Redis 阻塞的原因 
- 怎么去发现 Redis 阻塞异常情况？ 
- 如何发现大对象 
- Redis 的内存消耗分类有哪些？内存统计使用什么命令？  
- 简单介绍一下 Redis 的内存管理方式有哪些？  
- 如何设置 Redis 的内存上限？有什么作用？ 
- 什么是 bigkey？ 有什么影响？  
- 怎么发现bigkey? 
- 请简单描述一下 Jedis 的基本使用方法？ 
- Jedis连接池链接方法有什么优点？ 
- 冷热数据表示什么意思？  
- 缓存命中率表示什么？  
- 怎么提高缓存命中率？ 
- 如何优化 Redis 服务的性能？ 
- 如何实现本地缓存？请描述一下你知道的方式 
- 请介绍一下 Spring 注解缓存 
- 如果 AOF 文件的数据出现异常， Redis服务怎么处理？  
- Redis 的主从复制模式有什么优缺点?  
- Redis sentinel (哨兵) 模式优缺点有哪些？ 
- Redis 集群架构模式有哪几种？ 
- 如何设置 Redis 的最大连接数？查看Redis的最大连接数？查看Redis的当前连接数？  
- Redis 的链表数据结构的特征有哪些？  
- 请介绍一下 Redis 的 String 类型底层实现？ 
- Redis 的 String 类型使用 SDS 方式实现的好处？ 
- 设计一下在交易网站首页展示当天最热门售卖商品的前五十名商品列表?





#### 	