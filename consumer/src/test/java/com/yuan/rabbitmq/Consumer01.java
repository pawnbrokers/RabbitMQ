package com.yuan.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer01 {
    private static final String QUEUE = "helloworld";

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

        // 2. 监听队列
        //声明队列（有默认的交换机）如果队列在mq中没有，则会创建
            /*

            1. String queue 队列名
            2. boolean durable 是否持久化
            3. boolean exclusive 是否排他（独占），队列只允许在该连接中访问，如果连接关闭后，队列也就自动删除了,如果设置为true，可以作为临时队列
            4. boolean autoDelete 是否自动删除，如果此参数和排他参数均设置为true，可设置为临时队列
            5. Map<String, Object> arguments 队列参数 可以设置一些对垒的扩展参数，比如存活时间等
            queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        */
        channel.queueDeclare(QUEUE, true, false, false, null);

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
        channel.basicConsume(QUEUE, true, defaultConsumer);


        //
    }

}
