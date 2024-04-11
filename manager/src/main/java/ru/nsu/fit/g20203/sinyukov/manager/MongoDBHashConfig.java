package ru.nsu.fit.g20203.sinyukov.manager;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoDBHashConfig {

    private final static String databaseName = "hashCrackDB";

    private final int port;
    private final String host;

    public MongoDBHashConfig(@Value("${spring.mongodb.port}") int port,
                             @Value("${spring.mongodb.host}") String host) {
        this.port = port;
        this.host = host;
    }

    @Bean
    public MongoClientFactoryBean mongoClientFactory() {
        MongoClientFactoryBean mongo = new MongoClientFactoryBean();
        mongo.setHost(host);
        mongo.setPort(port);
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        mongo.setMongoClientSettings(mongoClientSettings);
        return mongo;
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
    }

    @Bean
    public MongoOperations mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}
