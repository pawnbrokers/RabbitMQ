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
