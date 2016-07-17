package constants.app.source;

/**
 * Created by Pankaj Nimgade on 27-05-2016.
 */
public final class Constants {

//    https://github.com/casamentero/ChatApp101

    public static final class RabbitMqCredentials {
        public static final String IP_ADRRESS = "162.144.209.190";
        public static final int PORT = 5672;
        public static final String USERNAME = "test";
        public static final String PASSWORD = "test";
        public static final String EXCHANGE_NAME = "chat.message.exchange";
        public static final String EXCHANGE_TYPE_TOPIC = "topic";
        public static final String EXCHANGE_TYPE_DIRECT = "direct";
        public static final String EXCHANGE_TYPE_FANOUT = "fanout";
    }

    public static final class ChatAPI {
        public static final String DIRECTION_BACKWARD = "backward";
        public static final String DIRECTION_FORWARD = "forward";

    }


}
