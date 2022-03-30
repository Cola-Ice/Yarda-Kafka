package com.yarda.kafka.model;

import lombok.Data;

import java.util.Date;

/**
 * 订单模型
 * @author xuezheng
 * @date 2022/3/28-12:39
 */
@Data
public class Order {
    private Long id;
    private String name;
    private String desc;

    public Order(Long id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }
}
