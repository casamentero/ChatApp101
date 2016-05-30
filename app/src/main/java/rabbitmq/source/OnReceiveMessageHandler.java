package rabbitmq.source;

/**
 * Created by Pankaj Nimgade on 27-05-2016.
 * <p>An interface to be implemented by an object that is interested in messages(listener)</p>
 */
public interface OnReceiveMessageHandler {

    void onReceiveMessage(byte[] message);

}
