package ru.nsu.fit.g20203.sinyukov.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.nsu.fit.g20203.sinyukov.lib.YamlPropertySourceFactory;

@Configuration
@ComponentScan
@PropertySource(value = "classpath:rabbit-application.yml", factory = YamlPropertySourceFactory.class)
@PropertySource(value = "classpath:rabbit-application-${spring.profiles.active:}.yml", factory = YamlPropertySourceFactory.class)
public class RabbitMQHashConfig {

    private final Logger logger = LoggerFactory.getLogger(RabbitMQHashConfig.class);

    private final String host;
    private final int port;

    private final String tasksExchangeName;
    private final String tasksQueueName;
    private final String tasksBindingKeyName;
    private final String resultsExchangeName;
    private final String resultsQueueName;
    private final String resultsBindingKeyName;

    private final static long BACKOFF_INITIAL_INTERVAL = 500;
    private final static double BACKOFF_MULTIPLIER = 10.0;
    private final static long BACKOFF_MAX_INTERVAL = 10000;

    public RabbitMQHashConfig(@Value("${spring.rabbitmq.host}") String host,
                              @Value("${spring.rabbitmq.port}") int port,
                              @Value("${spring.rabbitmq.tasks.exchange}") String tasksExchangeName,
                              @Value("${spring.rabbitmq.tasks.queue}") String tasksQueueName,
                              @Value("${spring.rabbitmq.tasks.binding-key}") String tasksBindingKeyName,
                              @Value("${spring.rabbitmq.results.exchange}") String resultsExchangeName,
                              @Value("${spring.rabbitmq.results.queue}") String resultsQueueName,
                              @Value("${spring.rabbitmq.results.binding-key}") String resultsBindingKeyName) {
        this.host = host;
        this.port = port;
        
        this.tasksExchangeName = tasksExchangeName;
        this.tasksQueueName = tasksQueueName;
        this.tasksBindingKeyName = tasksBindingKeyName;
        this.resultsExchangeName = resultsExchangeName;
        this.resultsQueueName = resultsQueueName;
        this.resultsBindingKeyName = resultsBindingKeyName;
    }

    @Bean
    public ConnectionFactory connectionFactory(RabbitMQHashConnectionListener connectionListener) {
        final var connectionFactory = new CachingConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);
        connectionFactory.addConnectionListener(connectionListener);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final var template = new RabbitTemplate(connectionFactory);
        final var retryTemplate = new RetryTemplate();
        final var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(BACKOFF_INITIAL_INTERVAL);
        backOffPolicy.setMultiplier(BACKOFF_MULTIPLIER);
        backOffPolicy.setMaxInterval(BACKOFF_MAX_INTERVAL);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retryTemplate);
        template.setMandatory(true);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        template.setReturnsCallback(returned -> logger.error(String.format("Message returned. Exchange: %s. Reply code: %d. Reply text: %s",
                returned.getExchange(),
                returned.getReplyCode(),
                returned.getReplyText())));
        return template;
    }

    @Bean
    public Exchange tasksExchange() {
        return new DirectExchange(tasksExchangeName, true, false);
    }

    @Bean
    public Queue tasksQueue() {
        return new Queue(tasksQueueName);
    }

    @Bean
    public Binding tasksBinding() {
        return BindingBuilder.bind(tasksQueue()).to(tasksExchange()).with(tasksBindingKeyName).noargs();
    }

    @Bean
    public Exchange resultsExchange() {
        return new DirectExchange(resultsExchangeName, true, false);
    }

    @Bean
    public Queue resultsQueue() {
        return new Queue(resultsQueueName);
    }

    @Bean
    public Binding resultsBinding() {
        return BindingBuilder.bind(resultsQueue()).to(resultsExchange()).with(resultsBindingKeyName).noargs();
    }
}

