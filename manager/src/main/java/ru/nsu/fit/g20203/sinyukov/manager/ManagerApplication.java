package ru.nsu.fit.g20203.sinyukov.manager;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.nsu.fit.g20203.sinyukov.rabbit.RabbitMQHashConfig;

@SpringBootApplication
@OpenAPIDefinition
@Import({RabbitMQHashConfig.class, MongoDBHashConfig.class})
public class ManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(@Value("${spring.rabbitmq.prefetch-count}") int prefetchCount,
                                                                               ConnectionFactory connectionFactory) {
        final var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO); // береженого бог бережет
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setPrefetchCount(prefetchCount);
        return factory;
    }
}
