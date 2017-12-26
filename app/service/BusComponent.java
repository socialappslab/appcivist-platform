package service;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.pa.PlayAuthenticate;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import enums.SubscriptionTypes;
import models.Config;
import models.User;
import models.transfer.NotificationSignalTransfer;
import play.Logger;
import play.Play;
import play.libs.Json;
import providers.MyUsernamePasswordAuthProvider;

import javax.inject.Inject;
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

    public static void sendToRabbit(NotificationSignalTransfer notificationSignalTransfer, List<Long> notifiedUsers,
                                    String richText) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();
        Map<String, String> toSend = new HashMap<>();
        toSend.put("title", notificationSignalTransfer.getTitle());
        toSend.put("text", notificationSignalTransfer.getText());
        toSend.put("resourceSpaceUUID", notificationSignalTransfer.getSpaceId());
        String message;
        for (Long user: notifiedUsers) {
            if (notificationSignalTransfer.getSignalType().equals(SubscriptionTypes.NEWSLETTER.name())) {
                sendNewsletterMail(user, richText);
            }
            message = Json.toJson(toSend).toString();
            channel.exchangeDeclare(EXCHANGE, "direct");
            channel.queueDeclare(user.toString(), false, false, false, null);
            channel.basicPublish(EXCHANGE, user.toString(), null, message.getBytes());
            Logger.info(" [x] Sent '" + message + "'");

        }

    }

    private static void sendNewsletterMail(Long userId, String body) {
        User fullUser = User.findByUserId(userId);
        boolean send = false;
        String mail = null;
        List<Config> configs = Config.findByUser(fullUser.getUuid());
        for (Config config: configs) {
            if (config.getKey().equals("notifications.preference.newsletter.service") &&
                    config.getValue().equals("email")) {
                send = true;
            }
            if (config.getKey().equals("notifications.service.email.identity")) {
                mail = config.getValue();
            }
        }
        if(send) {
            MyUsernamePasswordAuthProvider.sendNewsletterEmail(mail, body);
        }
    }
}
