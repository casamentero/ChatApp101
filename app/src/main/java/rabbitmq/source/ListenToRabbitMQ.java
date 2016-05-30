package rabbitmq.source;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

/**
 * Created by Pankaj Nimgade on 27-05-2016.
 */
public class ListenToRabbitMQ extends ConnectToRabbitMQ {

    private static final String TAG = ListenToRabbitMQ.class.getSimpleName();

    //The Queue name for this consumer
    private String mQueue;
    private QueueingConsumer mQueueingConsumer;

    //A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;


    //last message to post back
    private byte[] mLastMessage;

    /**
     * @param EXCHANGE_NAME,       this is the name of the exchange, on which messages are being broadcasted
     * @param EXCHANGE_TYPE_NAME,       this is the name of the exchange type name, fanout, direct, topic
     * @param ROUTING_BINDING_KEY, this is the name of the routing (binding) key so your queue will only get messages you are interested in
     */
    public ListenToRabbitMQ(String EXCHANGE_NAME, String EXCHANGE_TYPE_NAME, String ROUTING_BINDING_KEY) {
        super(EXCHANGE_NAME, EXCHANGE_TYPE_NAME, ROUTING_BINDING_KEY);
    }


    /**
     * Set the callback for received messages
     *
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler) {
        mOnReceiveMessageHandler = handler;
    }

    public boolean readMessages() {
        Log.d(TAG, "readMessages: ");
        if (connectToRabbitMQ()) {
            String queueName = null;
            try {
                Log.d(TAG, "readMessages: ");
                queueName = mChannel.queueDeclare().getQueue();
                mChannel.queueBind(queueName, EXCHANGE_NAME, ROUTING_BINDING_KEY);
                mChannel.basicConsume(queueName, true, consumer);
                Log.d(TAG, "readMessages: waiting for messages");
            } catch (IOException e) {
                Log.d(TAG, "readMessages: error while creating queue and binding");
                e.printStackTrace();
                mRunning = false;
            }
        } else {
            Log.d(TAG, "readMessages: could not connect to RabbitMQ server");
        }

        return mRunning;
    }

    private Consumer consumer = new DefaultConsumer(mChannel) {

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            super.handleDelivery(consumerTag, envelope, properties, body);
            String message = new String(body, "UTF-8");
            Log.d(TAG, "handleDelivery:\n routing_Key" + envelope.getRoutingKey() + "\nMessage: " + message);
            mOnReceiveMessageHandler.onReceiveMessage(message.getBytes());
        }
    };

    public boolean isRunning(){
        return mRunning;
    }

}
