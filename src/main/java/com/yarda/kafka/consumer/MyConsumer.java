package com.yarda.kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * kafka 消费者客户端
 * @author xuezheng
 * @date 2022/3/28-15:56
 */
public class MyConsumer {
    private final static String TOPIC = "test_replication";
    private final static String CONSUMER_GROUP_ID = "consumer_group12345";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties prop = new Properties();
        // 集群地址
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "xxx.xxx.x.xx:9092,xxx.xxx.x.xx:9093,xxx.xxx.x.xx:9094");
        // 消费者组
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        // 消费者心跳上报时间间隔
        prop.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        // 消费者session超时时间（如果该时间段内没收到心跳，就会被踢出消费者组）
        prop.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10*1000);


        // 消息key的反序列化方式
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 消息value的反序列化方式
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        // 是否自动提交offset
//        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//        // 自动提交offset间隔
//        prop.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        // 每次poll的消息数量
        prop.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        // 两次poll的时间间隔超出30s，Kafka认为其消费能力弱，剔出消费者组
        prop.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30*1000);
        // 消费者启动时，可以通过设置该参数，确定重新开始消费的策略
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 创建生产者
        Consumer<String, String> consumer = new KafkaConsumer<String, String>(prop);
        // 订阅主题
        consumer.subscribe(Arrays.asList(TOPIC));
//        // 指定分区消费
//        consumer.assign(Arrays.asList(new TopicPartition(TOPIC, 0)));
//        // 指定偏移量消费
//        consumer.seek(new TopicPartition(TOPIC, 0), 3);

        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("收到消息：" + record.topic() + "|" + record.offset() + "|" + record.key() + "|" + record.value());
            }

            // 手动同步提交offset
            consumer.commitSync();
//            // 手动异步提交offset
//            consumer.commitAsync(new OffsetCommitCallback() {
//                @Override
//                public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
//                    if (e != null){
//                        System.out.println("异步提交offset失败：" + map);
//                    }
//                }
//            });
        }
    }
}
