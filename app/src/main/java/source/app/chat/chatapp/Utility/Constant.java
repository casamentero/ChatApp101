package source.app.chat.chatapp.Utility;

public class Constant {

    public static class Server {
        public static final String IP_ADDRESS = "162.144.209.190";
      //  public static final String IP_ADDRESS = "10.0.2.2";
        public static final String QUEUE_NAME = "chat.message.user.1";
    	public static final String EXCHANGE_NAME = "chat.message.exchange";
        public static final String ROUTING_KEY = "chat.message.user.1";
        public static final int PORT = 5672;
    	public static final String EXCHANGE = "topic";
//        5672
    }

    public static class Credentials {
        public static final String USERNAME = "test";
        public static final String PASSWORD = "test";
    }
}