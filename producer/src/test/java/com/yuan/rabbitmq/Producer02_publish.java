package com.yuan.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer02_publish {
    //声明两个队列名称和交换机名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_FANOUT_INFORM = "exchange_fanout_inform";


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
            /*
            如果队列在mq中没有，则会创建
            1. String queue 队列名
            2. boolean durable 是否持久化
            3. boolean exclusive 是否排他（独占），队列只允许在该连接中访问，如果连接关闭后，队列也就自动删除了,如果设置为true，可以作为临时队列
            4. boolean autoDelete 是否自动删除，如果此参数和排他参数均设置为true，可设置为临时队列
            5. Map<String, Object> arguments 队列参数 可以设置一些对垒的扩展参数，比如存活时间等
            queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        */
            //这里需要声明两个队列

            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            /*
             * exchangeDeclare(String exchange, String type)
             * 参数： 1. 交换机的名称
             * 2. 交换机的类型fanout 对应的rabbitmq的 工作模式为发布订阅模式publish/scribe
             *
             * fanout: 发布订阅
             * direct: 对应于路由的工作模式
             * topic: 通配符模式
             * headers: 对应headers工作模式
             * */
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //发送消息，指定队列和交换机
            /*1. exchange 交换机，不指定选择默认,设置为空串
            2. routingKey 路由key，作用是交换机根据路由key来讲消息转发到指定的队列,如果使用默认交换机，routingkey要设置为队列名称
            3. props 可以额外设置属性
            4. body 消息内容
            * basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body
            * */

            //交换机和队列进行绑定
            /*queueBind(String queue, String exchange, String routingKey)
            1. 队列名称
            2. 交换机名称
            3. 路由key，在发布订阅模式中我们设置为空串，作用是交换机会根据路由key的值转发到指定的队列
            * */
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_FANOUT_INFORM, "");
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_FANOUT_INFORM, "");
            for (int i = 0; i < 5; i++) {
                String message = "send message to user";
                channel.basicPublish(EXCHANGE_FANOUT_INFORM, "", null, message.getBytes());
                System.out.println("send to MQ " + message);
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
