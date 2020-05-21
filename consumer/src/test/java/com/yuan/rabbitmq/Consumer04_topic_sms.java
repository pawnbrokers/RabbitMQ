package com.yuan.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer04_topic_sms {
    //声明两个队列名称和交换机名称


    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_TOPIC_INFORM = "exchange_topic_inform";
    private static final String ROUTINGKEY_SMS = "inform.#.sms.#";

    public static void main(String[] args) throws IOException, TimeoutException {
        //1. 建立连接和通道
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

        //建立新连接和通道
        Connection connection = null;
        connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        //声明队列
        channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
        //声明交换机
        channel.exchangeDeclare(EXCHANGE_TOPIC_INFORM, BuiltinExchangeType.TOPIC);
        //绑定交换机
        channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPIC_INFORM, ROUTINGKEY_SMS);

        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
            //当接收到消息后，此方法被调用

            /**
             * @param consumerTag 消费者标签=，可以在监听消费者的时候设置
             * @param envelope    信封，通过enelope可以获得很多信息
             * @param properties  消息的属性，发布消息的时候可以带有消息属性
             * @param body        消息内容
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //交换机
                String exchange = envelope.getExchange();
                //long类型的消息id，mq在我们的通道中用来标识消息的id，可用来手动回复消息已收到
                long deliveryTag = envelope.getDeliveryTag();
                String message = new String(body, "utf-8");
                System.out.println("receive " + message);

            }
        };


        // 3. 监听队列
        /*
         * basicConsume(String queue, boolean autoAck,  Consumer callback)
         * 参数列表：
         * 1. queue 队列名称
         *2. autoAck 自动回复，消费者接收到消息后告诉mq消息已接受，如果涉及为true，表示为自动回复mq，如果false，就要编程实现回复,否则他一直存在消息队列中
         * 3. callback 消费方法 ，消费者接收到消息执行的方法
         * */
        channel.basicConsume(QUEUE_INFORM_SMS, true, defaultConsumer);

        //消费者不用关闭
    }
}
