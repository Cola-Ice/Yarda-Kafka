package com.yarda.kafka.producer;

import com.alibaba.fastjson.JSON;
import com.yarda.kafka.model.Order;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Kafka生产者客户端
 * @author xuezheng
 * @date 2022/3/28-12:32
 */
public class MyProducer {
    private final static String TOPIC = "test_replication";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties prop = new Properties();
        // 集群地址
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "xxx.xxx.x.xx:9092,xxx.xxx.x.xx:9093,xxx.xxx.x.xx:9094");
        // ACK配置
        prop.put(ProducerConfig.ACKS_CONFIG, "1");
        // 本地缓冲区，如果设置了该缓冲区，消息会先发送到本地缓冲区，可以提升消息发送性能，默认33554432，即32MB
        prop.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        // 设置批量发送消息的大小，默认16384，即16kb
        prop.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        // 默认0，消息必须立即发出，但这样影响性能
        // 一般设置10毫秒，消息发送时会先进本地的batch，如果10毫秒这个batch满了16kb就会随batch一起发出
        // 如果10毫秒内，batch没满，那么也会将消息发送出去
        prop.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        // 发送消息key的序列化方式
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 发送消息value的序列化方式
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 创建生产者
        Producer<String, String> producer = new KafkaProducer<String, String>(prop);

        // 构建生产者记录对象
        Order order = new Order(1L, "订单1", "描述1");
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC, order.getId().toString(), JSON.toJSONString(order));

        // 同步发送消息
        RecordMetadata metadata = producer.send(record).get();
        if(metadata != null){
            System.out.println("同步发送消息成功：" + metadata.topic() + "|" + metadata.partition() + "|" +metadata.offset());
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 异步发送消息
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(recordMetadata != null){
                    System.out.println("异步发送消息成功：" + recordMetadata.topic() + "|" + recordMetadata.partition() + "|" + recordMetadata.offset());
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(5, TimeUnit.SECONDS);
        // 关闭生产者
        producer.close();
    }
}
