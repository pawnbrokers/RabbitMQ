# 1. 消息队列

## 1.1 什么是MQ

消息队列（Message Queue，简称MQ），从字面意思上看，本质是个队列，FIFO先入先出，只不过队列中存放的内容是message而已。
其主要用途：**不同进程Process/线程Thread之间通信。**



## 1.2 为什么需要MQ

+ 不同进程（process）之间传递消息时，**两个进程之间耦合程度过高**，改动一个进程，引发必须修改另一个进程，为了隔离这两个进程，在两进程间抽离出一层（一个模块），所有两进程之间传递的消息，都必须通过消息队列来传递，单独修改某一个进程，不会影响另一个；

+ 不同进程（process）之间传递消息时，为了实现标准化，将消息的格式规范化了，并且，**某一个进程接受的消息太多，一下子无法处理完**，并且也有先后顺序，必须对收到的消息进行排队，因此诞生了事实上的消息队列；



# 2. RabbitMQ

## 2.1 RabbitMQ简介

![image-20200521195347295](https://i.loli.net/2020/05/21/RN1kr8AfBcCIUQG.png)

## 2.2 ErLang语言

![image-20200521195543722](https://i.loli.net/2020/05/21/xP4yLU3iC1jVq86.png)



## 2.3 AMQP

**AMQP是消息队列的一个协议。**

![image-20200521195611850](https://i.loli.net/2020/05/21/2qtB4cOUJPMXxIK.png)



# 3. 五种队列

![img](https://img-blog.csdn.net/20180805223801450?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3pwY2FuZHpoag==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)



## 3.1 端口

**监控** 默认端口为15672

**连接** 默认端口为5672

用户名密码默认为 guest



# 4. Hello World

![image-20200521195928228](https://i.loli.net/2020/05/21/jZcV8b7THEYavN3.png)

## 4.1 Producer

```java
package com.yuan.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * rabbitMQ的入门
 */
public class Producer01 {

    private static final String QUEUE = "helloworld";

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
            channel.queueDeclare(QUEUE, true, false, false, null);
            //发送消息，指定队列和交换机
            /*1. exchange 交换机，不指定选择默认,设置为空串
            2. routingKey 路由key，作用是交换机根据路由key来讲消息转发到指定的队列,如果使用默认交换机，routingkey要设置为队列名称
            3. props 可以额外设置属性
            4. body 消息内容
            * basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body
            * */

            String message = "Hello RabbitMQ";
            channel.basicPublish("", QUEUE, null, message.getBytes());
            System.out.println("send to MQ " + message);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭连接,先关闭通道
            channel.close();
            connection.close();
        }

    }
}

```



步骤：

1. 建立连接，通过连接工厂来完成
2. 设置虚拟机
3. 建立新连接

4. 创建通道
5. 声明队列
6. 通过通道发送消息



这里我们主要看一下**声明队列和发送消息。**



### 4.1.1 声明队列（queueDeclare）

对应的方法为`chanel.queueDeclare()`

![image-20200521200418644](https://i.loli.net/2020/05/21/LeVwkZJfz2AYvoP.png)

```java
//声明队列（有默认的交换机）
queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
            /*
            如果队列在mq中没有，则会创建
            1. String queue 队列名
            2. boolean durable 是否持久化
            3. boolean exclusive 是否排他（独占），队列只允许在该连接中访问，如果连接关闭后，队列也就自动删除了,如果设置为true，可以作为临时队列
            4. boolean autoDelete 是否自动删除，如果此参数和排他参数均设置为true，可设置为临时队列
            5. Map<String, Object> arguments 队列参数 可以设置一些对垒的扩展参数，比如存活时间等
```



## 4.1.2 发送消息，需要指定队列和交换机（basicPublish）

![image-20200521200551804](https://i.loli.net/2020/05/21/8ARcOCvgop2F9V4.png)

```java
//发送消息，指定队列和交换机
basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body
          /*1. exchange 交换机，不指定选择默认,设置为空串
            2. routingKey 路由key，作用是交换机根据路由key来讲消息转发到指定的队列,如果使用默认交换机，routingkey要设置为队列名称
            3. props 可以额外设置属性
            4. body 消息内容
            * 
            */
```



这里我们发送一个字符串消息到RabbitMQ

![image-20200521200636981](https://i.loli.net/2020/05/21/7w5CqWuPRZlXmzs.png)

## 4.2 consumer

```java
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
```

对于消费者而言：

1. 建立连接
2. 创建通道
3. 声明队列（consumer中同样需要声明队列，作用是RabbitMQ中不存在这样的队列则创建，否则如果我们的消费者先打开的话，就会出现找不到队列的错误）
4. **监听队列**
5. **处理消息**
6. **回复消息**

这里我们主要看一下**监听队列和处理消息**的方法。



### 4.2.1 监听队列(basicConsume)

![image-20200521201159884](https://i.loli.net/2020/05/21/cevWzsHgKitNyZL.png)

```java
 // 3. 监听队列
        /*
         * basicConsume(String queue, boolean autoAck,  Consumer callback)
         * 参数列表：
         * 1. queue 队列名称
         *2. autoAck 自动回复，消费者接收到消息后告诉mq消息已接受，如果涉及为true，表示为自动回复mq，如果false，就要编程实现回复,否则他一直存在消息队列中
         * 3. callback 消费方法 ，消费者接收到消息执行的方法
         * */
        channel.basicConsume(QUEUE, true, defaultConsumer);
```

这里的这个**defaultConsumer就是我们的消息接收者。**



### 4.2.2 消费方法的实现 （DefaultConsumer 和 handleDelivery ）

![image-20200521201418850](https://i.loli.net/2020/05/21/tz95N8MK1VDxPCu.png)

首先创建一个`DefaultConsumer`的类，传入的参数为我们的channel，然后覆写`handleDelivery()`方法，实现具体的消息处理方法的。

![image-20200521201539604](https://i.loli.net/2020/05/21/hAjw3G4MKSWVy9C.png)



## 4.3 测试

开启Producer测试，打开[localhost：127.0.0.1:15672]()

![image-20200521201811804](https://i.loli.net/2020/05/21/UDJL8yFikvoVQKf.png)

在队列一栏中出现了名为helloeworld的消息队列，且有一条消息未被消费。

![image-20200521201923188](https://i.loli.net/2020/05/21/uZjpfQYzxUSAGt6.png)

运行consumer，**显示被消费，且消息队列中无消息堆积。**

![image-20200521201950158](https://i.loli.net/2020/05/21/TtcXSGsa9nKmkVU.png)





# 5. 工作模式（FANOUT）

主要应用场景是，**生产者发送消息的速度快于消费者处理的速度，为了避免消息堆积，我们需要使用多个消费者处理同样的业务。**

![image-20200521202040288](https://i.loli.net/2020/05/21/Igy6eGzCKXZxV7k.png)

**消息不可以被重复消费，默认采用轮询的方式。**

我们发送5条消息，开启两个consumer，观察效果。

![image-20200521202233200](https://i.loli.net/2020/05/21/pjfhZnPl2N5RYbX.png)



两个消费者一个消费3条，一个消费两条

![image-20200521202302402](https://i.loli.net/2020/05/21/R98QaskpYDvo3U4.png)

![image-20200521202307297](https://i.loli.net/2020/05/21/fAEXgBLSzmY8PKk.png)



# 6. 发布订阅模式

发布订阅模式是可以兼容工作模式的。

## 6.1 Producer

![image-20200521202538138](https://i.loli.net/2020/05/21/dVLc3tQFvAnIZ4X.png)

最常见的一个场景为：

注册账号的时候，我们既需要邮箱通知，也需要短信通知。



### 6.1.1 步骤

1. 建立连接
2. 建立通道
3. 声明多个队列
4. 声明交换机
5. 关联交换机和队列

```java
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
```



### 6.1.2 声明交换机和队列名称

![image-20200521202805611](https://i.loli.net/2020/05/21/8ZcWCLdUTNrja51.png)

### 6.1.3 声明队列

![image-20200521202900755](https://i.loli.net/2020/05/21/dmf6KcktGps5y2D.png)



### 6.1.4 声明交换机

![image-20200521202923326](https://i.loli.net/2020/05/21/472m8GhEcjYwpTl.png)

这里的交换机建立方式选择`FANOUT`，订阅发布模式。

### 6.1.5 交换机和队列绑定

![image-20200521203006467](https://i.loli.net/2020/05/21/JDw8ukYZ4RGe3VU.png)

这里我们不指定`routingKey`的，**交换机向所有的队列进行发布**。



### 6.1.6 发布消息

![image-20200521203140520](https://i.loli.net/2020/05/21/iLUt1Du49P5Iw2l.png)

这里发布消息的时候我们就**需要指定传递的交换机**，routingKey依旧暂时为空串。



### 6.1.7 关闭连接



## 6.2 Consumer

这里我们拥有两个`Consumer`，分别监听邮件队列和SMS短信队列

```java
package com.yuan.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer02_subscribe_email {
    //声明两个队列名称和交换机名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";

    private static final String EXCHANGE_FANOUT_INFORM = "exchange_fanout_inform";

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
        channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
        //声明交换机
        channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
        //绑定交换机
        channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_FANOUT_INFORM, "");

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
        channel.basicConsume(QUEUE_INFORM_EMAIL, true, defaultConsumer);

        //消费者不用关闭
    }
}
```

前面 声明队列交换机及其绑定与生产者相同。

### 6.2.1 监听消息队列

![image-20200521203429607](https://i.loli.net/2020/05/21/uXMdxrTHaZWLONJ.png)

**这里我们选择自动回复，否则消息会一直堆积在消息队列中。**



## 6.2.2 实现消费方法

![image-20200521203524638](https://i.loli.net/2020/05/21/OIaPnxDVjo91Hcb.png)



### 6.2.3 测试

开启生产者后，监控面板出**现对应的交换机**。

![image-20200521203633346](https://i.loli.net/2020/05/21/zLxJIcC75MV8gkw.png)

<img src="https://i.loli.net/2020/05/21/YIBJQPemxk52M9i.png" alt="image-20200521203646397" style="zoom:150%;" />

![image-20200521203659957](https://i.loli.net/2020/05/21/FySHaper4ZjhMdz.png)

这个交换机绑定了两条消息队列，都没有自己的`routingKey`。



打开对应的队列面板：实现了各向两条队列发布了消息。

![image-20200521203756006](https://i.loli.net/2020/05/21/7u4GzjZ9fMSxIhU.png)

**当SMS和Email都处理完消息后，队列内无堆积消息。**

![image-20200521203903637](https://i.loli.net/2020/05/21/8IwLfjp5XKdTBik.png)

![image-20200521203911535](https://i.loli.net/2020/05/21/uDJf3Umq1yR6sln.png)



# 7. 路由模式

路由模式是可以兼容发布订阅模式的。

![image-20200521204101729](https://i.loli.net/2020/05/21/1LIFeC2nqduOHSp.png)

## 7.1 Producer

这里我们每一条队列都有自己接受的类型，这种类型就是`routingKey`，而每一条消息发布的时候，都要带有`routingKey`，路由器将其发送给带有指定`routingKey`的队列（可以有多个）。



### 7.1.1 声明RoutingKey

![image-20200521204243838](https://i.loli.net/2020/05/21/qEPRdnplU5aTwJz.png)



### 7.1.2 声明消息队列

![image-20200521204304000](https://i.loli.net/2020/05/21/FctAmK3EOQSnuPU.png)



### 7.1.3 声明交换机

![image-20200521204315961](https://i.loli.net/2020/05/21/Ph9rSw7vsNj6J1q.png)

**这里的发布模式我们改为`BuiltinExchangeType.DIRECT`**



### 7.1.4 绑定队列及其交换机

![image-20200521204355693](https://i.loli.net/2020/05/21/oFXg8ytJT2eMHwu.png)

这里我们**绑定的时候就需要指定每一条队列拥有的**`routingKey`



### 7.1.5 发布消息并指定routingKey

![image-20200521204537867](https://i.loli.net/2020/05/21/5CKYJgOiaVXAI8Z.png)



## 7.2 Consumer

变化的也只是绑定的时候，注明`routingKey`

![image-20200521204643721](https://i.loli.net/2020/05/21/pBNyKCiU7JSaZmD.png)



## 7.2 测试

开启Prodecer，对应的路由器绑定了两条消息队列，且均带有`routingKey`。

![image-20200521204747859](https://i.loli.net/2020/05/21/s5LNKEO37kYHDp2.png)

由于我们发送的消息的`routingKey`为email的，只有email对应的队列接收到了消息。

![image-20200521204923207](https://i.loli.net/2020/05/21/OqRHEpVUZozaGCm.png)



# 8. 通配符模式

![image-20200521205104756](https://i.loli.net/2020/05/21/Bq9mkE6N57p1TGb.png)

只是对应的`routingKey`修改为通配符模式。

![image-20200521205209053](https://i.loli.net/2020/05/21/jRcIQgJqHz8aGr7.png)

像是`inform.email`就只有第一个接收，`inform.sms`至于第二个能接受，而对应`inform.sms.email`和`inform.email.sms`就二者都能接收到。





# 9. SpringBoot整合

## 9.1 导入依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.7.RELEASE</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>test-rabbitmq</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>producer</module>
        <module>consumer</module>
    </modules>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>

        </dependency>


    </dependencies>


</project>
```

## 9.2 application.yml

```yml
server:
  port: 8888
spring:
  application:
    name: test_rabbitmq-producer
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```



## 9.3 Config

我们用Config文件来完成对了，交换机的声明，以及队列与交换机的绑定。

```java
package com.yuan.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class RabbitmqConfig {
    //绑定键
    public static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    public static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    public static final String EXCHANGE_TOPIC_INFORM = "exchange_topic_inform";
    public static final String ROUTINGKEY_EMAIL = "inform.#.email.#";
    public static final String ROUTINGKEY_SMS = "inform.#.sms.#";
    @Bean(QUEUE_INFORM_EMAIL)
    public Queue QUEUE_INFORM_EMAIL() {
        return new Queue(QUEUE_INFORM_EMAIL);
    }
    @Bean(QUEUE_INFORM_SMS)
    public Queue QUEUE_INFORM_SMS() {
        return new Queue(QUEUE_INFORM_SMS);
    }
    //声明交换机。durable（true）之后，mq重启之后队列还在
    @Bean(EXCHANGE_TOPIC_INFORM)
    TopicExchange EXCHANGE_TOPIC_INFORM() {
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPIC_INFORM).durable(true).build();
    }

    @Bean
    Binding BINDING_QUEUE_INFORM_EMAIL(@Qualifier(QUEUE_INFORM_EMAIL)Queue queue, @Qualifier(EXCHANGE_TOPIC_INFORM)Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_EMAIL).noargs();
    }
    @Bean
    Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_INFORM_SMS)Queue queue, @Qualifier(EXCHANGE_TOPIC_INFORM)Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_SMS).noargs();
    }
}
```



## 9.4 启动类

```java
package com.yuan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestRabbitMQApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestRabbitMQApplication.class, args);
    }
}
```



## 9.5 Producer

```java
@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_topic_springboot {


    @Autowired
    RabbitTemplate rabbitTemplate;


    /*
    convertAndSend(String exchange, String routingKey, Object message, CorrelationData correlationData)
    1. exchange 交换机
    2. routingkey 指定路由key
    3. message 具体的消息
    * */
    @Test
    public void testSendEmail(){
        String message = "send email message to user";
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPIC_INFORM,"inform.email",message);
    }
}
```

利用一个`RabbitTemplate`来完成消息的发布，具体的方法为`convertAndSend`

![image-20200521205910913](https://i.loli.net/2020/05/21/w6GVTYzKvCWy5DX.png)



## 9.6 Consumer

消费者的配置类和启动类与生产者一致。

只需要在方法上加注解即可。

```java
@Component
public class ReceiveHandler {


    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})
    public void send_email(String msg){
        System.out.println("接收到的email消息为 "+ msg);
    }
}
```

注解中指定坚挺的队列数组，在方法体内进行逻辑处理。**当消费者的主启动类启动后**，就会自动监听消息队列。



## 9.7 测试

![image-20200521210159487](https://i.loli.net/2020/05/21/G8NBVijhYHbyacu.png)

![image-20200521210210959](https://i.loli.net/2020/05/21/tThJEs1bGY3IMyl.png)

![image-20200521210219262](https://i.loli.net/2020/05/21/Y2vx7hfA613jaZl.png)



**开启消费者的主启动类后：**

![image-20200521210307624](https://i.loli.net/2020/05/21/WLY5rso3TjBmp6U.png)

![image-20200521210314493](https://i.loli.net/2020/05/21/rv6a8HnZxJwOElT.png)