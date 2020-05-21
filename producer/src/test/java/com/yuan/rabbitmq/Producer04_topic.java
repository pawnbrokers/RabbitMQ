package com.yuan.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer04_topic {
    //声明两个队列名称和交换机名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_TOPIC_INFORM = "exchange_topic_inform";
    private static final String ROUTINGKEY_EMAIL = "inform.#.email.#";
    private static final String ROUTINGKEY_SMS = "inform.#.sms.#";




    public static void main(String[] args) throws IOException, TimeoutException {

        //队列


        //和MQ建立连接
        //通过连接工厂来创建连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);//端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        //设置虚拟机
        //一个mq的服务可以设置多个虚拟机，每个虚拟机都相当于一个mq
        connectionFactory.setVirtualHost("/");

        //建立新连接
        Connection connection = null;
        Channel channel = null;
        try {
            connection = connectionFactory.newConnection();
            //创建绘画通道,生产者和mq服务的所有通信都在通道中完成
            channel = connection.createChannel();
            //声明队列（有默认的交换机）


            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            channel. exchangeDeclare(EXCHANGE_TOPIC_INFORM, BuiltinExchangeType.TOPIC);

            //绑定队列以及交换机
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_TOPIC_INFORM, ROUTINGKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPIC_INFORM, ROUTINGKEY_SMS);
            for (int i = 0; i < 5; i++) {
                String message = "send email message to user";
                channel.basicPublish(EXCHANGE_TOPIC_INFORM, "inform.email", null, message.getBytes());
                System.out.println("send to email MQ " + message);
            }
            for (int i = 0; i < 5; i++) {
                String message = "send sms message to user";
                channel.basicPublish(EXCHANGE_TOPIC_INFORM, "inform.sms", null, message.getBytes());
                System.out.println("send to sms MQ " + message);
            }
            for (int i = 0; i < 5; i++) {
                String message = "send email and sms message to user";
                channel.basicPublish(EXCHANGE_TOPIC_INFORM, "inform.email.sms", null, message.getBytes());
                System.out.println("send to email and sms MQ " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭连接,先关闭通道
            channel.close();
            connection.close();
        }
    }


}
