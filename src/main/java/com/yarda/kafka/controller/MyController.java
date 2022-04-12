package com.yarda.kafka.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * spring-boot 向Kafka发送消息
 * @author xuezheng
 * @date 2022/3/29-17:03
 */
@RestController
@RequestMapping("/control")
public class MyController {
    private final static String TOPIC = "test_replication";
    @Resource
    private KafkaTemplate kafkaTemplate;

    /**
     * 生产者发送消息到Kafka
     */
    @PostMapping("/sendMsg")
    public String sendMsg() throws ExecutionException, InterruptedException {
        Object result = kafkaTemplate.send(TOPIC, "key", "message").get();
        return "send success";
    }
}
