package com.yuan.rabbitmq;


import com.yuan.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
