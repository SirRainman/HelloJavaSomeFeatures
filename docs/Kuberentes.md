# Kubernetes Architecture

![image-20210228215540348](http://haoimg.hifool.cn/img/image-20210228215540348.png)

master

* kube-apiserver
* kube-controller-manager
* kube-scheduler
* etcd

worker

* kubelet
* kube-proxy



[提交yaml文件的流程](http://dockone.io/article/9134)：

1.	提交含有所部署的容器的相关信息的yaml文件
2.	通过kubectl客户端工具发送给k8s集群中master结点的API server。
3.	API server接收到外部请求
    1.	认证：验证请求者是合法的
    2.	授权：身份 != 权限，对用户进行授权
    3.	准入控制：kube-apiserver 验证了身份并且赋予了相应的权限，Kubernetes 其他组件对于应不应该允许发生的事情还是很有意见的，准入控制器会拦截请求以确保它符合集群的更广泛的期望和规则。
4.	kube-apiserver 对 HTTP 请求进行反序列化，然后利用得到的结果构建运行时对象（有点像 kubectl 生成器的逆过程），并保存到 etcd 中。
5.	Controller组件监控ETCD中的资源变化并作出反应：
    1.	replicaSet检查数据库变化，创建yaml文件中所期望数量的pod实例。
    2.	在pod运行过程中，master节点中的Control manager会负责持续管理pod的状态。
6.	Kube-scheduler检测到pod信息会开始调度预选，会先过滤掉不符合Pod资源配置要求的节点，然后开始调度调优，主要是挑选出更适合运行pod的节点，然后将pod的资源配置单发送到node节点上的kubelet组件上。
    1.	Scheduler把新的pods分配到可以运行这些pod的node结点上，形成一个部署方案，并把部署方案传给API server
7.	API server根据上述部署方案请求相关node的Kubelet，通过Kubelet把pod运行起来，并把pod的相关信息保存至ETCD。
8.	Kubelet根据scheduler发来的资源配置单运行pod，管理后续pod的生命周期，并将pod的运行信息返回给scheduler，scheduler将返回的pod运行状况的信息存储到etcd数据中心。
9.	kuber-proxy运行在集群各个主机上，管理网络通信，如服务发现、负载均衡。例如当有数据或请求发送到主机时，将其路由到正确的pod或容器。



controller-manager 和etcd 通信吗？不通信

* 原因：`controller-manager` 和 `scheduler` 它们都是和 `kube-apiserver`通信，然后 `kube-apiserver` 再和`etcd`通信。



Minikube：

* Minikube是一种工具，可以在本地轻松运行Kubernetes。这将在虚拟机中运行单节点Kubernetes群集。

Kubectl：

* Kubectl是一个平台，您可以使用该平台将命令传递给集群。因此，它基本上为CLI提供了针对Kubernetes集群运行命令的方法，以及创建和管理Kubernetes组件的各种方法。



---



# Master

## API server

API server主要负责接收、校验并响应所有的REST请求，是kubernetes对外的唯一接口，提供HTTP/HTTPS RESTful API，即kubernetes API。

* 所有的请求都需要经过这个接口进行通信。
    * 作为资源操作的唯一入口，它提供了认证、授权、访问控制、API注册和发现等机制。
* 客户端与k8s群集及K8s内部组件的通信，都要通过Api Server这个组件；
* 结果状态被持久存储在etcd当中，是所有资源增删改查的唯一入口，支持的数据类型包括JSON和YAML。

## ETCD

ETCD负责保存k8s集群的配置信息和各种资源的状态信息，当数据发生变化时，ETCD会快速通知k8s相关组件。

* ETCD是一个独立的服务组件，并不隶属于kubernetes集群。
* 在实际生产环境中，ETCD应该在k8s集群外部以集群方式运行，以确保服务的可用性。
* ETCD不仅仅用于提供键值数据存储，而且还为其提供了监听（watch）机制，用于监听和推送变更。
* 在kubernetes集群系统中，etcd的键值发生变化会通知倒API Server，并由其通过watch API向客户端输出。

## Controller-manager

Controller Manager：以守护进程的形式运行着kubernetes几个核心的控制循环（也就是控制器），管理集群各种资源，保证资源处于预期的状态。

* 控制器包括deployment，replicaset，namespace，serviceaccount，node等等，
* 通过调用Api Server 的 list watch接口来监控自己负责的资源的配置变化.



Contorller manager完成的主要功能主要包括：

* 生命周期功能包括：
    * Namespace创建和生命周期维护、
    * Event垃圾回收、
    * pod终止相关的垃圾回收、
    * 级联垃圾回收
    * Node垃圾回收等；
* API业务逻辑：
    * 由ReplicaSet执行的pod扩展等。

Controller manager由多种controller组成

* replication controller、
* endpoints controller、
* namespace controller

## Scheduler

资源调度，负责决定将Pod放到哪个Node上运行。

* Scheduler在调度时会对集群的结构进行分析，当前各个节点的负载，以及应用对高可用、性能等方面的需求。



kube-scheduler 给一个 pod 做调度选择包含两个步骤：

- `预选（Predicates）`：输入是所有节点，输出是满足预选条件的节点。kube-scheduler根据预选策略过滤掉不满足策略的Nodes。例如，如果某节点的资源不足或者不满足预选策略的条件如“Node的label必须与Pod的Selector一致”时则无法通过预选。
- `优选（Priorities）`：输入是预选阶段筛选出的节点，优选会根据优先策略为通过预选的Nodes进行打分排名，选择得分最高的Node。例如，资源越富裕、负载越小的Node可能具有越高的排名。

---



# Worker

## Kubelet

这是一个代理服务，它在每个节点上运行，并使从服务器与主服务器通信。因此，

kubelet是Node上的Pod管理服务，负责创建和维护pod的生命周期。

* 当Scheduler确定在某个node上运行pod后，会将pod的具体配置信息（image、volume等）发送给该node的kubelet，
* kubelet会根据这些信息，处理PodSpec中提供给它的容器的描述，并确保PodSpec中描述的容器运行正常。
* 创建和运行容器成功后向master报告其运行状态。

## Kube-proxy

每个node都会运行kube-proxy服务，负责将访问的service的TCP/UDP数据流转发到后端的容器。

* 基本是一个网络代理，它反映了每个节点上Kubernetes API中配置的服务。
* kube-proxy是实现kubernetes service的通信与负载均衡机制的重要组件。

* service接收到请求后，需要kube-proxy转发到pod。
    * 如果有多个副本，kube-proxy会通过Iptables或LVS实现负载均衡。

# Network

## Ingress

![image-20210228222609831](http://haoimg.hifool.cn/img/image-20210228222609831.png)

**Ingress 是 对集群中服务的外部访问 进行管理的 API 对象**，典型的访问方式是 HTTP。

* Ingress 公开了从集群外部到集群内服务的 HTTP 和 HTTPS 路由。 
    * 流量路由由 Ingress 资源上定义的规则控制。
* Ingress 将对集群中服务的外部访问 配置为通过可访问的URL
* 可以将 Ingress 配置为服务提供外部可访问的 URL、负载均衡流量、终止 SSL/TLS，以及提供基于名称的虚拟主机等能力。
    * Ingress 控制器 通常负责通过负载均衡器来实现 Ingress，尽管它也可以配置边缘路由器或其他前端来帮助处理流量。
* Ingress 不会公开任意端口或协议。 
    * 将 HTTP 和 HTTPS 以外的服务公开到 Internet 时，通常使用 Service.Type=NodePort 或 Service.Type=LoadBalancer 类型的服务。
* 因此，Ingress是一个API对象，通常通过HTTP管理集群中服务的外部访问，是暴露服务的最有效方式。



## Service

k8s集群外流量怎么访问Pod？



pod如何和node网络通信的？

* Flannel：使用vxlan技术为各节点创建一个可以互通的Pod网络，使用的端口为UDP 8472（需要开放该端口，如公有云AWS等）。
    * flanneld第一次启动时，从etcd获取配置的Pod网段信息，为本节点分配一个未使用的地址段，然后创建flannedl.1网络接口（也可能是其它名称，如flannel1等）。
    * flannel将分配给自己的Pod网段信息写入 /run/flannel/subnet.env 文件，docker后续使用这个文件中的环境变量设置docker0网桥，从而从这个地址段为本节点的所有Pod容器分配IP。
* Calico：在宿主机部署calico作为虚拟路由器，容器流量通过veth pair到达宿主机的网络命名空间上。



# Pod

**pod的生命周期**

| `Pending`（悬决）   | Pod 已被 Kubernetes 系统接受，但有一个或者多个容器尚未创建亦未运行。此阶段包括等待 Pod 被调度的时间和通过网络下载镜像的时间， |
| ------------------- | ------------------------------------------------------------ |
| `Running`（运行中） | Pod 已经绑定到了某个节点，Pod 中所有的容器都已被创建。至少有一个容器仍在运行，或者正处于启动或重启状态。 |
| `Succeeded`（成功） | Pod 中的所有容器都已成功终止，并且不会再重启。               |
| `Failed`（失败）    | Pod 中的所有容器都已终止，并且至少有一个容器是因为失败终止。也就是说，容器以非 0 状态退出或者被系统终止。 |
| `Unknown`（未知）   | 因为某些原因无法取得 Pod 的状态。这种情况通常是因为与 Pod 所在主机通信失败。 |





**对外提供服务的pod暴露方式有哪些**？

- `hostNetwork`：在pod中使用该配置，在这种Pod中运行的应用程序可以直接看到pod启动的主机的网络接口
- `hostPort`：直接将容器的端口与所调度的节点上的端口路由，这样用户就可以通过主机的IP来访问Pod
- `NodePort`：是K8s里一个广泛应用的服务暴露方式。K8s中的service默认情况都是使用Cluster IP这种类型，会产生一个只能在内部访问的Cluster IP，如果想能够直接访问service，需要将service type修改为nodePort。同时给改service指定一个nodeport值(30000-32767)，用 `--service-node-port-range` 定义。
- `LoadBalancer`：只能在service上定义，是公有云提供的负载均衡器
- `Ingress`：ingress controller是由K8s管理的负载均衡容器，它的镜像包含一个nginx或HAProxy负载均衡器和一个控制器守护进程。



Kubernetes 将 Pod 调度到指定的节点上：

- 指定节点 nodeName
    - nodeName 是 PodSpec 当中的一个字段。如果该字段非空，调度程序直接将其指派到 nodeName 对应的节点上运行。
    - 局限性：
        - 如果 nodeName 对应的节点不存在，Pod 将不能运行
        - 如果 nodeName 对应的节点没有足够的资源，Pod 将运行失败，可能的原因有：OutOfmemory /OutOfcpu
        - 集群中的 nodeName 通常是变化的（新的集群中可能没有该 nodeName 的节点，指定的 nodeName 的节点可能从集群中移除）
- 节点选择器 nodeSelector 
    - **Kubernetes 推荐用法**
    - nodeSelector 是 PodSpec 中的一个字段。指定了一组名值对。节点的 labels 中必须包含 Pod 的 nodeSelector 中所有的名值对，该节点才可以运行此 Pod。
- Node isolation/restriction
    - 向节点对象添加标签后，可以将 Pod 指定到特定（一个或一组）的节点，以便确保某些 Pod 只在具备某些隔离性、安全性或符合管理规定的节点上运行。
- Affinity and anti-affinity
    - nodeAffinity：用于规定pod可以部署在哪个node或者不能部署在哪个节点上。解决pod和主机的问题。
    - podAffinity：用于规定pod可以和哪些pod部署在同一拓扑结构下。
    - podAntiAffinity：用于规定pod不可以和哪些pod部署在同一拓扑结构下，与podAffinity一起解决pod和pod之间的关系。

---

# 数据持久化

[参考](https://www.hi-linux.com/posts/14136.html#vip-container)

**k8s数据非持久化的方式有哪些？**

- emptyDir：
    - emptryDir，顾名思义是一个空目录，它的生命周期和所属的 Pod 是完全一致的。
    - 缓存空间，例如基于磁盘的归并排序。
    - 为耗时较长的计算任务提供检查点，以便任务能方便地从崩溃前状态恢复执行。
    - 在 Web 服务器容器服务数据时，保存内容管理器容器获取的文件。
- hostPath
    - `hostPath`卷能将主机节点文件系统上的文件或目录挂载到您的 Pod 中。 
    - 虽然这不是大多数 Pod 需要的，但是它为一些应用程序提供了强大的逃逸机会。
    - 缺点：
        - 由于不同节点的hostPath文件/目录内容不同，因此同一模板创建的Pod在不同节点上的行为可能有所不同
        - 用hostPath在主机上创建的文件或目录只能由root写入。这意味着您要么需要以root用户身份运行容器进程，要么将主机上的文件权限修改为非root用户可写，这可能会导致安全问题
        - 不应该将hostPath卷类型用于StatefulSets
- Local Persistent Volume
    - **利用机器上的磁盘来存放业务需要持久化的数据**，和远端存储类似，此时数据依然独立于 Pod 的生命周期，即使业务 Pod 被删除，数据也不会丢失。
    - **Local PV 和 hostPath区别**
        - 最大的区别是Kubernetes调度程序了解本地持久卷属于哪个节点。
        - 对于HostPath卷，调度程序可能会将引用HostPath卷的pod移至其他节点，从而导致数据丢失。
        - 但是对于本地持久卷，Kubernetes调度程序可确保始终将使用本地持久卷的容器调度到同一节点。

----

**Kubernetes 持久化存储方式**

* PersistentVolume
* PersistentVolumeClaim
* StorageClass
    * 由于不同的应用程序对于存储性能的要求也不尽相同，比如：读写速度、并发性能、存储大小等。如果只能通过 PVC 对 PV 进行静态申请，显然这并不能满足任何应用对于存储的各种需求。
    * 通过 StorageClass 的定义，集群管理员可以先将存储资源定义为不同类型的资源，比如快速存储、慢速存储等。

----