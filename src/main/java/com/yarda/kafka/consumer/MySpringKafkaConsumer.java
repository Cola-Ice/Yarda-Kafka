package com.yarda.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * spring boot 集成 Kafka 消费者客户端
 * @author xuezheng
 * @date 2022/3/29-17:09
 */
@Component
public class MySpringKafkaConsumer {

    @KafkaListener(topics = "test_replication", groupId = "spring-kafka-group", concurrency = "2")
    public void listenGroup(ConsumerRecord<String,String> record, Acknowledgment ack){
        System.out.println("收到消息：" + record.topic() + "|" + record.offset() + "|" + record.key() + "|" + record.value());
        // 手动提交offset
        ack.acknowledge();
    }
}
