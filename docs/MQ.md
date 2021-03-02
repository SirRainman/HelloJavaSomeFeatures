# 消息队列的作用：

1. **异步：**
    1. 和线程池有什么区别：线程池需要进行对线程进行管理，需要有较大的代码变动
2. **削峰：**处理速度不匹配
3. **解耦：**
    1. 只需要进行发送消息就行了

# 分布式事务

一个服务在与其他服务相关联时，怎么保证多个事务都成功

## ACID

- **原子性（atomicity）**：一个事务是一个不可分割的工作单位，事务中包括的操作要么都做，要么都不做。
- **一致性（consistency）**：事务必须是使数据库从一个一致性状态变到另一个一致性状态。一致性与原子性是密切相关的。
- **隔离性（isolation）**：一个事务的执行不能被其他事务干扰。即一个事务内部的操作及使用的数据对并发的其他事务是隔离的，并发执行的各个事务之间不能互相干扰。
- **持久性（durability）**：**持久性也称永久性（permanence）**，指一个事务一旦提交，它对数据库中数据的改变就应该是永久性的。接下来的其他操作或故障不应该对其有任何影响。

## 分布式事务



# 消息队列组件

- rocketmq-client：提供发送、接受消息的客户端API。
- rocketmq-broker：**接受生产者发来的消息并存储**（通过调用rocketmq-store），**消费者从这里取得消息**
- rocketmq-namesrv：类似于Zookeeper，**这里保存着消息的TopicName，队列等运行时的元信息**。
- rocketmq-common：**通用的一些类，方法，数据结构**等。
- rocketmq-remoting：基于Netty4的client/server + fastjson**序列化 + 自定义二进制协议**。
- rocketmq-store：**消息、索引存储**等。
- rocketmq-filtersrv：**消息过滤器Server**，需要注意的是，要实现这种过滤，需要上传代码到MQ！（一般而言，我们利用Tag足以满足大部分的过滤需求，如果更灵活更复杂的过滤需求，可以考虑filtersrv组件）。
- rocketmq-tools：命令行工具。

## name-server

**NameServer**压力不会太大，平时主要开销是在维持心跳和**提供Topic-Broker的关系数据**。

* Broker向NameServer发心跳时， 会带上当前自己所负责的所有**Topic**信息，
* 如果**Topic**个数太多（万级别），会导致一次心跳中，就Topic的数据就几十M，网络情况差的话， 网络传输失败，心跳失败，导致NameServer误认为Broker心跳失败。

**NameServer** 被设计成几乎无状态的，可以横向扩展，节点之间相互之间无通信，通过部署多台机器来标记自己是一个伪集群。

* 每个 Broker 在启动的时候会到 NameServer 注册，
* Producer 在发送消息前会根据 Topic 到 **NameServer** 获取到 Broker 的路由信息，
* Consumer 也会定时获取 Topic 的路由信息。



## producer

消息生产者，负责产生消息，一般由业务系统负责产生消息。

**Producer**由用户进行分布式部署，消息由**Producer**通过多种负载均衡模式发送到**Broker**集群，发送低延时，支持快速失败。



**RocketMQ** 提供了三种方式发送消息：同步、异步和单向

- **同步发送**：同步发送指消息发送方发出数据后会在收到接收方发回响应之后才发下一个数据包。
    - 一般用于重要通知消息，例如重要通知邮件、营销短信。
- **异步发送**：异步发送指发送方发出数据后，不等接收方发回响应，接着发送下个数据包，
    - 一般用于可能链路耗时较长而对响应时间敏感的业务场景，例如用户视频上传后通知启动转码服务。
- **单向发送**：单向发送是指只负责发送消息而不等待服务器回应且没有回调函数触发，
    - 适用于某些耗时非常短但对可靠性要求并不高的场景，例如日志收集。



## broker

消息中转角色，负责**存储消息**，转发消息。

- **Broker**是具体提供业务的服务器，单个Broker节点与所有的NameServer节点保持长连接及心跳，并会定时将**Topic**信息注册到NameServer，
    - 顺带一提底层的通信和连接都是**基于Netty实现**的。
- **Broker**负责消息存储，
    - 以Topic为纬度支持轻量级的队列，单机可以支撑上万队列规模，支持消息推拉模型。
- 官网上有数据显示：具有**上亿级消息堆积能力**，同时可**严格保证消息的有序性**



## consumer

消息消费者，负责消费消息，一般是后台系统负责异步消费。

- **Consumer**也由用户部署，支持PUSH和PULL两种消费模式，支持**集群消费**和**广播消息**，提供**实时的消息订阅机制**。
- **Pull**：拉取型消费者（Pull Consumer）主动从消息服务器拉取信息，只要批量拉取到消息，用户应用就会启动消费过程，所以 Pull 称为主动消费型。
- **Push**：推送型消费者（Push Consumer）封装了消息的拉取、消费进度和其他的内部维护工作，将消息到达时执行的回调接口留给用户应用程序来实现。
    - 所以 Push 称为被动消费类型，但从实现上看还是从消息服务器中拉取消息，
    - 不同于 Pull 的是 Push 首先要注册消费监听器，当监听器处触发后才开始消费消息。



# 消息领域模型

![image-20210302132644963](http://haoimg.hifool.cn/img/image-20210302132644963.png)

## Message

**Message**（消息）就是要传输的信息。

一条消息必须有一个主题（Topic），主题可以看做是你的信件要邮寄的地址。

一条消息也可以拥有一个可选的标签（Tag）和额处的键值对，它们可以用于设置一个业务 Key 并在 Broker 上查找此消息以便在开发期间查找问题。

## Topic

**Topic**（主题）可以看做消息的规类，它是消息的第一级类型。比如一个电商系统可以分为：交易消息、物流消息等，一条消息必须有一个 Topic 。

**Topic** 与生产者和消费者的关系非常松散，一个 Topic 可以有0个、1个、多个生产者向其发送消息，一个生产者也可以同时向不同的 Topic 发送消息。

一个 Topic 也可以被 0个、1个、多个消费者订阅。

## Tag

**Tag**（标签）可以看作子主题，它是消息的第二级类型，用于为用户提供额外的灵活性。

使用标签，同一业务模块不同目的的消息就可以用相同 Topic 而不同的 **Tag** 来标识。

比如交易消息又可以分为：交易创建消息、交易完成消息等，一条消息可以没有 **Tag** 。

标签有助于保持您的代码干净和连贯，并且还可以为 **RocketMQ** 提供的查询系统提供帮助。

## Group

分组，一个组可以订阅多个Topic。

分为ProducerGroup，ConsumerGroup，代表某一类的生产者和消费者，一般来说同一个服务可以作为Group，同一个Group一般来说发送和消费的消息都是一样的

## Queue

在**Kafka**中叫Partition，每个Queue内部是有序的，

在**RocketMQ**中分为读和写两种队列，一般来说读写队列数量一致，如果不一致就会出现很多问题。

## Message Queue

**Message Queue**（消息队列），主题被划分为一个或多个子主题，即消息队列。

一个 Topic 下可以设置多个消息队列，发送消息时执行该消息的 Topic ，RocketMQ 会轮询该 Topic 下的所有队列将消息发出去。

消息的物理管理单位。一个Topic下可以有多个Queue，Queue的引入使得消息的存储可以分布式集群化，具有了水平扩展能力。

## Offset

在**RocketMQ** 中，所有消息队列都是持久化，长度无限的数据结构，所谓长度无限是指队列中的每个存储单元都是定长，访问其中的存储单元使用Offset 来访问，Offset 为 java long 类型，64 位，理论上在 100年内不会溢出，所以认为是长度无限。

也可以认为 Message Queue 是一个长度无限的数组，**Offset** 就是下标。

## 消息消费模式

消息消费模式有两种：**Clustering**（集群消费）和**Broadcasting**（广播消费）。

默认情况下就是集群消费，该模式下一个消费者集群共同消费一个主题的多个队列，一个队列只会被一个消费者消费，如果某个消费者挂掉，分组内其它消费者会接替挂掉的消费者继续消费。

而广播消费消息会发给消费者组中的每一个消费者进行消费。

## Message Order

**Message Order**（消息顺序）有两种：**Orderly**（顺序消费）和**Concurrently**（并行消费）。

顺序消费表示消息消费的顺序同生产者为每个消息队列发送的顺序一致，所以如果正在处理全局顺序是强制性的场景，需要确保使用的主题只有一个消息队列。

并行消费不再保证消息顺序，消费的最大并行数量受每个消费者客户端指定的线程池限制。






























