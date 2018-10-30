package service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import enums.SubscriptionTypes;
import models.Config;
import models.Subscription;
import models.User;
import models.transfer.NotificationSignalTransfer;
import play.Logger;
import play.Play;
import play.libs.Json;
import providers.MyUsernamePasswordAuthProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class BusComponent {

    private static final String HOST = Play.application().configuration().getString("appcivist.services.rabbitmq.host");
    private static final int PORT = Play.application().configuration().getInt("appcivist.services.rabbitmq.port");
    private static final String USER = Play.application().configuration().getString("appcivist.services.rabbitmq.user");
    private static final String PASS = Play.application().configuration().getString("appcivist.services.rabbitmq.password");
    private static final String EXCHANGE = Play.application().configuration().getString("appcivist.services.rabbitmq.exchange");

    private static Connection connection = null;

    private static Connection getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USER);
            factory.setPassword(PASS);
            factory.setConnectionTimeout(0);
            factory.setHandshakeTimeout(20000);
            Logger.info("Trying to connect to rabbit:  " + HOST + ":" + PORT + " User: " + USER + " PASS: " + PASS);
            connection = factory.newConnection();
            Logger.info("Connection successful");
        }
        return connection;
    }

    public static void sendToRabbit(NotificationSignalTransfer notificationSignalTransfer, List<Long> notifiedUsers,
                                    String richText, boolean ownTitle, boolean alwaysSendMail) throws IOException, TimeoutException {
        Channel channel = getConnection().createChannel();
        Map<String, String> toSend = new HashMap<>();
        toSend.put("title", notificationSignalTransfer.getTitle());
        toSend.put("text", notificationSignalTransfer.getText());
        toSend.put("resourceSpaceUUID", notificationSignalTransfer.getSpaceId());
        String message;
        for (Long user: notifiedUsers) {
            if(ownTitle) {
                sendSignalMail(user, richText, notificationSignalTransfer, notificationSignalTransfer.getTitle(), alwaysSendMail);
            } else {
                sendSignalMail(user, richText, notificationSignalTransfer, null, alwaysSendMail);
            }
            message = Json.toJson(toSend).toString();
            channel.exchangeDeclare(EXCHANGE, "direct");
            channel.queueDeclare(user.toString(), false, false, false, null);
            channel.basicPublish(EXCHANGE, user.toString(), null, message.getBytes());
            Logger.info(" [x] Sent '" + message + "'");

        }

    }

    private static void sendSignalMail(Long userId, String body,
                                       NotificationSignalTransfer notificationSignalTransfer, String title,
                                       boolean alwaysSendMail) {
        User fullUser = User.findByUserId(userId);

        Logger.info("Sending mail to "+ fullUser.getEmail());
        boolean send = false;
        String mail = null;
        String subject = "[Appcivist] New Notification";
        if(notificationSignalTransfer.getSpaceType().equals(SubscriptionTypes.NEWSLETTER.name())) {
            subject = "[Appcivist] New Newsletter";
            List<Config> configs = Config.findByUser(fullUser.getUuid());
            for (Config config : configs) {
                if (config.getKey().equals("notifications.preference.newsletter.service") &&
                        config.getValue().equals("email")) {
                    send = true;
                }
                if (config.getKey().equals("notifications.service.email.identity")) {
                    mail = config.getValue();
                }
            }
        } else if (notificationSignalTransfer.getSpaceType().equals(SubscriptionTypes.REGULAR.name())) {
            Subscription subscription = Subscription.findBySignalAndUser(notificationSignalTransfer, fullUser.getUuidAsString());
            if(subscription !=null && subscription.getDisabledServices() != null
                    && subscription.getDisabledServices().get("email") != null) {
                send = !subscription.getDisabledServices().get("email");
            }
        }
        if(title != null) {
            subject = title;
        }
        if(alwaysSendMail) {
            mail = fullUser.getEmail();
        }
        if((send || alwaysSendMail) && mail != null) {
            MyUsernamePasswordAuthProvider.sendNewsletterEmail(subject,mail, body);
        }
    }
}
