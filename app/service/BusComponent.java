package service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import models.transfer.NotificationSignalTransfer;
import play.Logger;
import play.Play;
import play.libs.Json;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class BusComponent {

    private static final String HOST = Play.application().configuration().getString("appcivist.rabbitmq.host");
    private static final int PORT = Play.application().configuration().getInt("appcivist.rabbitmq.port");
    private static final String USER = Play.application().configuration().getString("appcivist.rabbitmq.user");
    private static final String PASS = Play.application().configuration().getString("appcivist.rabbitmq.password");
    private static final String EXCHANGE = Play.application().configuration().getString("appcivist.rabbitmq.exchange");

    private static Connection connection = null;

    private static Connection getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USER);
            factory.setPassword(PASS);
            connection = factory.newConnection();
        }
        return connection;
    }

    public static void sendToRabbit(NotificationSignalTransfer notificationSignalTransfer, List<String> notifiedUsers) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();
        Map<String, String> toSend = new HashMap<>();
        toSend.put("title", notificationSignalTransfer.getTitle());
        toSend.put("text", notificationSignalTransfer.getText());
        toSend.put("resourceSpaceUUID", notificationSignalTransfer.getSpaceId());
        String message;
        for (String user: notifiedUsers) {
            message = Json.toJson(toSend).toString();
            channel.queueDeclare(user, false, false, false, null);
            channel.basicPublish(EXCHANGE, user, null, message.getBytes());
            Logger.info(" [x] Sent '" + message + "'");
        }

    }
}
