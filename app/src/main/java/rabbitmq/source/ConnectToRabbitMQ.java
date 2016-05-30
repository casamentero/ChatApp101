package rabbitmq.source;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

import constants.app.source.Constants;

/**
 * Created by Pankaj Nimgade on 27-05-2016.
 */
public abstract class ConnectToRabbitMQ {

    private static final String TAG = ConnectToRabbitMQ.class.getSimpleName();

    protected String EXCHANGE_NAME; // exchange which is broadcasting the messages
    protected String EXCHANGE_TYPE_NAME; //topic, fanout, direct
    protected String ROUTING_BINDING_KEY;

    protected boolean mRunning;

    protected Channel mChannel = null;
    protected Connection mConnection;


    public ConnectToRabbitMQ(String EXCHANGE_NAME, String EXCHANGE_TYPE_NAME, String ROUTING_BINDING_KEY) {
        Log.d(TAG, "ConnectToRabbitMQ: EXCHANGE_NAME: "+EXCHANGE_NAME+"\nEXCHANGE_TYPE_NAME: "+EXCHANGE_TYPE_NAME+
        "\nROUTING_BINDING_KEY: "+ROUTING_BINDING_KEY);
        this.EXCHANGE_NAME = EXCHANGE_NAME;
        this.EXCHANGE_TYPE_NAME = EXCHANGE_TYPE_NAME;
        this.ROUTING_BINDING_KEY = ROUTING_BINDING_KEY;
    }

    /**
     * Connect to the broker and create the exchange
     *
     * @return success
     */
    public boolean connectToRabbitMQ() {
        Log.d(TAG, "connectToRabbitMQ: ");
        if (mChannel != null && mChannel.isOpen())//already declared
            return true;
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(Constants.RabbitMqCredentials.IP_ADRRESS);
            connectionFactory.setUsername(Constants.RabbitMqCredentials.USERNAME);
            connectionFactory.setPassword(Constants.RabbitMqCredentials.PASSWORD);
            connectionFactory.setPort(Constants.RabbitMqCredentials.PORT);

            mConnection = connectionFactory.newConnection();
            mChannel = mConnection.createChannel();
            mChannel.exchangeDeclare(this.EXCHANGE_NAME, this.EXCHANGE_TYPE_NAME, true);

            Log.d(TAG, "connectToRabbitMQ: successfully connected to RabbitMQ server");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            mRunning = false;
            return false;
        }
    }

    public void dispose() {
        mRunning = false;

        try {
            if (mConnection != null)
                mConnection.close();
            if (mChannel != null)
                mChannel.abort();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
