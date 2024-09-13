package com.ink.backend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AIGCinitMain {
    public static void main(String[] args) {
        createMQ(MQConstant.VIDEO_QUEUE_NAME,MQConstant.VIDEO_EXCHANGE_NAME,MQConstant.VIDEO_ROUTING_KEY);
        createMQ(MQConstant.VOICE_QUEUE_NAME,MQConstant.VOICE_EXCHANGE_NAME,MQConstant.VOICE_ROUTING_KEY);
    }

    public static void createMQ(String queueName,String exchangeName,String routingKey){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName,"direct");

            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName,exchangeName,routingKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        log.info("创建队列成功~~~~~~~~~");
    }
}
