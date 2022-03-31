# Kafka

## 一、消息队列简介

为什么使用消息队列？

**1. 解耦**

消息生产者和消息消费者分离，降低系统间耦合度

**2. 异步**

使用异步方式将非必要的业务逻辑异步处理，让上游快速响应，明显提升系统吞吐量

**3. 削峰**

在高并发场景下，由于服务端来不及同步处理数量过多的请求，可能导致请求阻塞，甚至出现"too many connections"错误。通过使用消息队列，异步处理这些请求，从而缓解系统压力

**消息队列的通信模式**

消息队列的通信模式可以分为点对点模式、发布订阅模式，Kafka为点对点模式

> 点对点模式：基于拉取，发送到队列的消息被一个且只有一个消费者进行处理
>
> 发布订阅模式：基于推送，发布到topic的消息会被所有订阅者消费

**消息队列解决的具体问题——通信问题**

## 二、消息队列的流派

目前消息队列的中间件选型有很多种：

rabbitMQ：内部的可玩性(功能性)是非常强

rocketMQ：阿里开源，根据Kafka的内部执行原理编写，性能与Kafka比肩，封装了更多的功能

kafka：全球消息处理性能最快的MQ

zeroMQ

这些消息队列中间件有什么区别？

### １.有broker

- 重topic：Kafka、RocketMQ、ActiveMQ

  整个broker依据topic来进行消息的中转，在重topic的消息队列里必须有topic的存在

- 轻topic：rabbitMQ

  topic只是一种消息中转模式

### ２.无broker

在生产者和消费者之间没有broker，例如zeroMQ，直接使用socket进行通信

## 三、Kafka基本知识

Kafka是一个分布式、支持分区的、多副本的，基于zookeeper协调的分布式消息系统，可以实时处理大量数据，由Scala语言编写

### 1.Kafka安装

```console
$ docker run --restart always -d --name kafka-server1 \
    --network zookeeper \
    -p 9092:9092 \
    -e KAFKA_BROKER_ID=1\
    -e KAFKA_HEAP_OPTS=-Xmx256M \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://yarda.top:9092 \
    -e ALLOW_PLAINTEXT_LISTENER=yes \
    -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181 \
    bitnami/kafka:2.4.1
```

### 2.Kafka的基本概念

| 名称          | 含义                                                         |
| ------------- | :----------------------------------------------------------- |
| Broker        | 消息中间件处理节点，一个Kafka节点就是一个Broker，多个Broker可以组成一个Kafka集群 |
| Topic         | Kafka根据topic对消息进行归类，发布到集群的每条消息都需要指定topic |
| Producer      | 消息生产者，向Broker发送消息的客户端                         |
| Consumer      | 消息消费者，从Broker读取消息的客户端                         |
| ConsumerGroup | Consumer group是kafka提供的可扩展且具有容错性的消费者机制，每个消费者属于一个特定的消费者组 |
| Partition     | 分区，物理上的概念，一个topic可以分为多个分区，每个分区内消息是有序的 |

### 3.Kafka的基本操作

#### 1.创建topic

通过Kafka命令创建主题

```console
./kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic cli_test
```

通过Kafka命令查询主题列表

```console
./kafka-topics.sh --list --zookeeper zookeeper:2181
```

#### 2.发送消息

通过Kafka命令行生产者客户端发送消息

```console
./kafka-console-producer.sh --broker-list localhost:9092 --topic cli_test
```

#### 3.消费消息

通过Kafka命令行消费者客户端消费消息

方式一：从当前主题的最后一条消息的offset + 1位置开始消费

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cli_test
```

方式二：从当前主题的第一条消息开始消费

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic cli_test
```

#### 4.查看消费者组信息

```
# 查看消费者组列表
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
# 查看消费者组详细信息
./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group testGroup1
```

![image-20220326143103493](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220326143103493.png)

PARTITION：分区

CURRENT-OFFSET：分区当前消费偏移量

LOG-END-OFFSET：分区日志末端偏移量

LAG：未消费的消息数量

### 4.Kafka的消费模式

#### 1.集群消费

Kafka默认集群消费，同一个topic下的消息只会被组内一个消费者实例所消费

```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --consumer-property group.id=testGroup --topic cli_test
```

#### 2.广播消费

同一个topic下的消息被多个消费者消费称为广播消费。由于Kafka默认是集群消费模式，所以广播消费的实现方式就是为多个应用实例都设置不同的消费组

## 四、Kafka中的主题、分区和副本

### 1.主题Topic

topic是Kafka中的一个逻辑概念，Kafka通过topic将消息进行分类，topic会被订阅该主题的消费者消费

### 2.partition分区

一个主题可以对应多个分区，当主题中的消息量非常大时，可以通过设置分区，来分布式存储这些消息

```
# 创建一个多分区的主题
./kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 3 --topic test_partition
```

**分区的作用**

- 分区存储，可以解决存储文件过大的问题
- 提高读写吞吐量，读和写可以同时在多个分区进行

**分区和消费者关系**

![image-20220328113625911](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220328113625911.png)

1.一个分区只能被同一消费者组里的一个消费者消费，一个消费者可以消费多个分区，目的是保证消息消费的局部顺序性

2.消费者组内消费者数量不能大于主题中的分区数，否则多出来的消费者消费不到消息

3.如果消费者数量发生变化，那么会触发rebalance机制

### 3.副本replication

![image-20220326175013804](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220326175013804.png)

在Kafka中，主题可以有多个分区，分区又可以有多个副本。其中一个为leader，其他的都是follower，仅有leader对外提供服务

#### 1.Kafka副本的作用

在Kafka中，副本的目的仅仅是冗(rong)余备份，所有的读写请求都是由leader副本处理。follower副本仅有一个功能，那就是从leader副本拉取消息，尽量让自己跟leader副本保持一致

#### 2.follower副本为什么不对外提供服务？

这个问题本质上是对性能和一致性的取舍。如果follower副本对外提供服务，性能肯定会有所提升，但同时会出现其他问题，例如数据库事务中的幻读、脏读

#### 3.leader副本挂掉后，如何选举新leader？

从结果上来讲，Kafka分区副本选举类似Zookeeper，都是选择最新的follower副本作为leader，但它是通过一个ISR副本集合实现的（同步副本集合）

1.Kafka会将与leader副本保持同步的副本放到ISR副本集合中。当然leader也存在于ISR集合

2.当leader失联后，Kafka通过Zookeeper感知这一情况，在ISR集合选取新副本作为leader

3.如果出现leader失联后，ISR集合为空，Kafka就会在非同步的副本中，选取副本成为leader，但这意味着部分消息会丢失

#### ４.ISR副本集合保存副本的条件是什么？

跟该参数有关：replica.lag.time.max.ms

如果副本拉取消息的速度慢于leader副本写入的速度，时差超过上述参数设定值，就会变为"非同步副本"，踢出ISR集合，如果后续速度提上来，就会重新回到ISR集合

### 4.Kafka的位移主题

**位移主题**：consumer_offsets

Kafka 集群中的第一个 Consumer 程序启动时，Kafka 会自动创建位移主题，该主题包含50个分区(提高并发性)，３个副本，用来存放消费者消费某个主题的偏移量

- 每个消费者都会主动维护自己消费的主题的偏移量，消费者提交位移的方式有两种：**自动提交位移和手动提交位移**

- 提交到哪个分区：通过hash函数，hash(consumerGroupId)%_consumer_offsets主题分区数
- 提交到主题的内容：key是consumerGroupId + topic + 分区号，value就是当前的offset值

## 五、Kafka的数据存储结构

Kafka将数据保存在磁盘中，以主题 + 分区的形式组织文件目录

![image-20220326152236877](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220326152236877.png)

> 我们的一般认知里，写入磁盘是比较耗时的操作，不适合这种高并发组件。但是Kafka初始化时会单独开辟一块磁盘空间，顺序写入数据（效率比随机写入高）

### 1.Partition结构

![image-20220326133346708](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220326133346708.png)

- 分区在服务器上的表现形式就是一个个文件夹，每个partition文件夹下面会有多组segment文件，每组segment文件又包含.index文件、.log文件、.timeindex文件

- log文件是实际存储message的文件，index和timeindex文件是索引文件，用于消息检索

- 分区内的消息是有序保存的，每组segment文件以该segment组中保存最小的offset命名，Kafka就是利用分段+索引的方式来解决查询效率的问题

### 2.Message结构

消息主要包含消息体、消息大小、offset、压缩类型等，重点关注的有三部分：

1. offset：offset的是一个8字节的有序id，它可以唯一确定每条消息在partition中的位置
2. 消息大小：消息大小占用4字节，用于描述消息大小
3. 消息体：存放的实际消息数据（被压缩过）

### 3.存储策略

无论消息是否被消费，Kafka都会保存所有的消息，对于topic中旧数据的删除策略有两种：

1. 基于时间，默认保存7天
2. 基于大小，默认配置是-1，表示无穷大

需要注意的是，Kafka读取特定消息的时间复杂度是O(1)，所以删除过期的文件并不会提高Kafka性能

### 4.消息检索流程

![image-20220330164731312](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220330164731312.png)

Kafka在分区内根据消息的offset检索消息

1.先找到分区文件夹下当前offset对应的segment文件（二分查找）

2.打开segment文件中的.index文件，该文件采用稀疏索引存储相对offset及对应message物理偏移量的关系，通过二分查找相对offset小于或等于指定的相对offset的索引条目中最大的那个相对offset，取其物理偏移量

> 相对offset，即相对segment文件中最小的offset的偏移量

3.打开.log文件，从上边找到的物理偏移量开始，顺序扫描值找找到指定offset的那条消息

这套机制建立在offset有序的基础上，利用segment + 有序offset + 稀疏索引 + 二分查找 + 顺序查找，实现高效检索

## 六、搭建Kafka集群

启动三个broker，注册到同一zookeeper即可，注意需要配置不同的broker.id

### 1.向Kafka集群发送消息

```console
# 向Kafka集群发送消息
./kafka-console-producer.sh --broker-list localhost:9092,localhost:9093,localhost:9094 --topic test_replication
```

### 2.从Kafka集群消费消息

```console
# 从Kafka集群消费消息
./kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --consumer-property group.id=testGroup4 --topic test_replication
```

## 七、Java客户端-生产者

### 1.生产者实现

Kafka客户端依赖

```xml
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.4.1</version>
        </dependency>
```

详见代码

消息发送流程：

![image-20220328143809255](https://github.com/Cola-Ice/Yarda-Kafka/raw/master/doc/image/image-20220328143809255.png)

### 2.同步发送消息

如果生产者发送消息没有收到ACK，生产者会阻塞3s时间，如果还没有收到回复，会进行重试，重试次数为3

### 3.异步发送消息

异步发送，生产者发送完消息后就可以继续执行之后的业务逻辑，broker在收到消息后，异步调用生产者提供的callback回调方法

### 4.生产者ACK配置

对于ACK会有三种模式（默认acks=1）：

-acks=0：生产者只要通过网络把消息发送出去，不需要等集群返回，最容易丢消息，效率最高

-acks=1：leader已经收到消息，并把消息写入到本地的log中，才会返回ack给生产者，性能和安全性最均衡（leader挂掉，未同步的消息还会丢失）

-acks=all：只有ISR集合中所有副本写入成功，且ISR集合不小于最小规模，才会返回ack给客户端，最安全，性能最差

> min.insync.replicas=1(默认1，推荐配置大于等于2)：这个参数表示ISR集合中的最小副本数
>
> 如果ISR副本集合的数量小于最小规模，则生产者会收到错误响应，从而确保消息不丢失

### 5.发送消息的缓冲区机制

为提升效率，Kafka会尽可能批量发送和拉取消息

1.Kafka默认会创建一个消息缓冲区，消息会先发送到本地缓冲区，默认33554432，即32MB

2.Kafka会从消息缓冲区拉取一定数量的消息批量发送，默认16kb

3.如果拉不到16kb数据，也会在配置的时间间隔内将消息发出，默认0，通常设置为10ms

> 默认0，表示消息必须立即发出，但这样影响性能

### 6.消息发送的分区

如果一个topic存在多个分区，producer怎样知道消息发送到哪个分区？

1.写入的时候可以指定写入的分区，如果有指定则直接写入该分区

2.如果没有指定分区，但是设置了数据key，则会根据key值hash计算发到哪个分区

3.既没有指定分区，也没有指定key，则会通过轮询选出一个分区

## 八、Java客户端-消费者

### 1.消费者实现

详见代码

### 2.自动提交和手动提交offset

#### 1.提交的内容

消费者无论是自动提交还是手动提交，都需要把key(消费者组+topic+分区)，value(offset)，这样的信息提交到Kafka集群的__consumer_offsets主题

#### 2.自动提交

消费者poll消息下来以后就会自动提交offset

```java
// 是否自动提交offset
prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
// 自动提交offset间隔
prop.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
```

注意：自动提交可能会丢消息，因为消费者在消费前提交offset，有可能提交完offset还没消费时消费者挂掉

#### 3.手动提交

需要把自动提交的配置改为false

```java
prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
```

手动提交又分为两种：

- 手动同步提交

  在消费完消息后调用同步提交方法，当集群返回ACK之前一直阻塞，返回ACK后表示提交成功，执行之后的逻辑

- 手动异步提交

  在消息消费完后调用异步提交方法，不需要等待集群ACK，直接执行之后的逻辑，可以设置一个回调方法，供集群调用

### 3.长轮询poll消息

默认情况下，消费者一次会poll 500条消息

```java
// 每次poll的消息数量
prop.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
```

poll方法要求传入长轮询的时间，当前设置1s

```java
ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
```

poll方法拉取机制：

- 如果一次拉取到500条，就直接执行后边的消费逻辑
- 如果这一次没有拉取到500条，且时间在1s内，那么长轮询继续拉取，要么到500条，要么到1s
- 如果多次拉取都没达到500条，时间到1s，向下执行消费逻辑

另外，如果两次poll的时间间隔超过5分钟，集群就会认为该消费者消费能力过弱，将该消费者踢出消费者组，触发rebalance机制。rebalance机制会造成性能开销，可以设置一次poll的消息数量少一点

```java
// 每次poll的消息数量
prop.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
// 两次poll的时间间隔超出5分钟，Kafka认为其消费能力弱，剔出消费者组
prop.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300*1000);
```

### 4.消费者的健康状态检查

消费者每隔1s向Kafka集群发送心跳，集群如果发现有超过10s没有续约的消费者，将被踢出消费者组，触发该消费者组的rebalance机制，将该分区交给组内的其他消费者消费

```java
// 消费者心跳上报时间间隔
prop.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
// 消费者session超时时间（如果该时间段内没收到心跳，就会被踢出消费者组）
prop.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10*1000);
```

### 5.指定分区、偏移量和时间消费

消费者在消费时，还可指定分区、偏移量、时间进行消费

#### 1.指定分区消费

Kafka消费者可以指定消费的分区

```java
// 指定分区消费
consumer.assign(Arrays.asList(new TopicPartition(TOPIC, 0)));
```

#### 2.指定偏移量消费

Kafka消费者可以从分区的指定偏移量开始消费

```java
// 指定分区消费
consumer.assign(Arrays.asList(new TopicPartition(TOPIC, 0)));
// 指定偏移量消费
consumer.seek(new TopicPartition(TOPIC, 0), 3);
```

#### 3.指定时间消费

根据时间，查找所有分区中该时间对应的offset，然后根据分区对应的offset值开始消费，代码略

### 6.消费者启动时消费策略

当消费者启动时，Kafka提供了3种重新开始消费的策略，可以通过设置参数确定：auto.offset.reset

```java
prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
```

**earliest**
当各分区下有已提交的偏移量时，从提交的偏移量开始消费；无提交的偏移量时，从头开始消费
**latest**
当各分区下有已提交的偏移量时，从提交的偏移量开始消费；无提交的偏移量时，从新产生的该分区下的数据开始消费
**none**
topic各分区都存在已提交的偏移量时，从偏移量后开始消费；只要有一个分区不存在已提交的偏移量，则抛出异常

## 九、Spring Boot集成Kafka

Spring-Kafka依赖

```xml
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
```

详见代码

## 十、Kafka集群的Controller、Rebalance和HW

### 1.Kafka集群中的Controller

Kafka集群有多个broker，每个broker启动时会向zookeeper中创建一个临时序号节点，节点序号最小的那个broker将作为集群的controller，负责管理整个集群中分区和副本的状态

Controller的作用：

- 当集群中有broker新增减少时，Controller会将信息同步给其他broker
- 当集群中有分区新增减少时，Controller会将信息同步给其他broker
- 当集群中有leader副本挂掉，Controller会在ISR集合中重新选举一个leader（规则ISR集合从左到右）

> 为什么一定需要Controller？这些信息其他broker也可以通过zookeeper获取呀？
>
> 其实在Kafka早期版本中，对分区和副本的状态管理就是依赖于zookeeper，每个broker都会向zookeeper注册watch，导致zookeeper中出现大量的watch，zookeeper负担很重，极易产生羊群效应。新版本改变了这种设计，controller会向zookeeper上注册watch，其他broker几乎不用监听zookeeper的状态变化，controller负责将集群中分区和副本的状态信息同步给其他broker

### 2.Rebalance机制

前提：消费者组中的消费者没有指明消费的分区

触发条件：当消费者组中消费者和分区的关系发生变化时

GroupCoordinator(协调者)：协调者是协调消费者组完成Rebalance的重要组件，每个broker都会启动一个协调者，Kafka按照消费者组ID将其分配给对应的协调者管理，每个协调者只负责管理一部分消费者组

Rebalance机制触发时，消费者组中的所有消费者停止拉取消息。协调者会选择一个消费者来执行这个消费组分区的重新分配并将分配结果转发给消费组内所有的消费者，分区分配的策略有三种：

Range：默认策略，按照消费者总数和分区总数进行整除运算计算跨度，然后将分区按照跨度进行平均分配，以保证分区尽可能均匀的分配给所有的消费者

Round：将分区按顺序轮询分配给每个Consumer

Sticky：粘合策略，分区的分配尽量均衡，每一次重新分配的结果尽量与上一次分配结果保持一致

> 分配过程可以分为两步骤：加入组（Joining the Group）和同步组状态（Synchronizing Group State）
>
> 加入组：所有成员都向协调者发送加入组请求，当所有成员都发送了加入组请求，协调者从中选取一个消费者，并将其他足踏组员信息发送给该消费者
>
> 同步组状态：leader消费者重新分配分区，并将结果回传给协调者，协调者再将结果同步给其他消费者

### 3.HW和LEO

LEO：LOG-END-OFFSET的缩写，当前分区日志末端偏移量

HW：俗称高水位，消费者最大可见消息偏移量，ISR副本集合中最小的LEO即为分区的HW，消费者只能拉取到这个offset之前的消息

> 为什么会用HW限制?
>
> 防止消息丢失。如果没HW限制，当消费者拉取到了一批还没有同步的消息，此时leader副本挂掉，选举出新的leader副本，新写入了一批消息，这时消费者消费完上一批消息，提交偏移量，就会导致新写入的这批消息被标记为已消费，造成消息丢失

## 十一、Kafka线上问题优化

### 1.如何防止消息丢失

- 生产者：同步发送消息并等待发送结果，或异步发送回调错误时重新发送，并且acks设置为all，等待ISR副本集合全部写入成功才返回ACK
- Kafka集群：设置ISR副本集合最小规模>=2
- 消费者：自动提交改为手动提交，消费完成再提交偏移量

### 2.如何防止重复消费

**什么情况下会出现重复消费？（简要列举了几种）**

1.生产者重复发送消息（例如重试导致的消息重复）

2.消费者rebalance可能导致重复消费（消费后的数据，offset还没提交，partition断开连接，rebalance之后就会重复消费）

3.消费者消费速度很慢，被集群踢出消费者组，分区分配给其他消费者，也可能导致消息重复消费

4.强行kill线程，导致消费数据后，offset没有提交

**如何防止重复消费？**

1.通过Kafka的幂等性生产者机制，避免生产端消息重复写入broker

> 幂等性生产者(idempotent producer)，在这个机制中同一消息可能被producer发送多次，但是在broker端每一条消息编号去重只会写入一次，需要设置producer端的参数 enable.idempotence 开启

2.消费端接口提供幂等性保证，可通过设置唯一键等方式以达到单次与多次操作对系统的影响相同，这时候就不用考虑重复消费的问题

3.引入单独去重机制，例如生成消息时，在消息中加入唯一标识符如消息id，在消费消息时先通过前置去重后再进行消息的处理

### 3.顺序消费的实现

- 生产者：同步发送，将有业务逻辑顺序的消息，按照顺序发送到相同的分区
- 消费者：拉取分区中的消息，采用单线程顺序消费，或者消费者多线程消费时，采用一个worker线程对应一个阻塞队列，对具有相同key的消息取模，放入相同的队列中，实现顺序消费

### 4.事务消息的实现

Kafka从 0.11版本引入事务，通过事务协调者来管理事务，Kafka的事务解决的问题是，确保一个事务中发送的多条消息，要么都成功，要么都失败

> 而RocketMQ中的事务，解决的问题是，确保本地事务和发消息这两个操作，要么都成功，要么都失败

### 5.延迟消息的实现

开源版本中，只有RocketMQ支持延迟消息，且只支持18个特定级别的延迟。Kafka中若要实现，需要生产者发送消息时带上创建时间，消费者消费逻辑中实现延迟消费

### 6.消息积压问题

**消息积压问题的产生：**

消费者的消费速度远远赶不上生产者的发送速度，导致Kafka中存在大量没有被消费的消息，随着没有被消费的数据堆积越多，消费者寻址的性能会越差，最后导致整个Kafka集群性能变差，进而造成其他服务访问速度变慢，向上传递最终导致服务雪崩

**消息积压问题的解决方案：**

1.通过业务架构设计，提升消费者性能

2.单个消费者中，使用多线程消费消息

3.创建多个分区、多个消费者

4.消息通过多个主题多级发送，成倍扩增消费者——不常用

## 十二、Kafka-eagle监控平台搭建

Kafka Eagle 用于在 Topic 被消费的情况下监控 Kafka 集群，可参照官网安装：https://docs.kafka-eagle.org/











