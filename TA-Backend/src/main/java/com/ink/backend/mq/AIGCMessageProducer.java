package com.ink.backend.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@Slf4j
public class AIGCMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param message
     */
    public void sendMessage(String exchangeName,String routingKey,String message){
        rabbitTemplate.convertAndSend(exchangeName,routingKey,message);
    }
}
