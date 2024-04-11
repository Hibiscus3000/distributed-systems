package ru.nsu.fit.g20203.sinyukov.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.stereotype.Component;
import ru.nsu.fit.g20203.sinyukov.rabbit.connection.ConnectionState;

@Component
public class RabbitMQHashConnectionListener implements ConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(RabbitMQHashConnectionListener.class);

    private final ConnectionState connectionState;

    public RabbitMQHashConnectionListener(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    @Override
    public void onCreate(Connection connection) {
        connectionState.connectionOpened();
        logger.info(String.format("Connection \"%s\" created", connection.getDelegate()));
    }

    @Override
    public void onShutDown(ShutdownSignalException signal) {
        final Object signalFirer = signal.getReference();
        final String signalFirerId = getSignalFirerId(signalFirer);
        logger.debug(String.format("%s + is shutted down", signalFirerId));
    }

    @Override
    public void onClose(Connection connection) {
        connectionState.connectionClosed();
        logger.info(String.format("Connection \"%s\" closed", connection.getDelegate()));
    }

    @Override
    public void onFailed(Exception exception) {
        logger.warn(String.format("Connection failed: %s", exception.getMessage()));
    }

    private String getSignalFirerId(Object signalFirer) {
        if (signalFirer instanceof Connection connection) {
            return getConnectionIdStr(connection.getDelegate());
        } else {
            final Channel channel = (Channel) signalFirer;
            return getConnectionIdStr(channel.getConnection()) + " channel #" + channel.getChannelNumber();
        }
    }

    private String getConnectionIdStr(com.rabbitmq.client.Connection connection) {
        if (null == connection) {
            return "unknown connection";
        }
        return "connection \"" + connection.getId() + "\"";
    }
}
