package com.yuan.mq;

import com.yuan.config.RabbitmqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 *
 */
@Component
public class ReceiveHandler {


    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})
    public void send_email(String msg){
        System.out.println("接收到的email消息为 "+ msg);
    }


}
