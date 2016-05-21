package source.app.chat.chatapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

import source.app.chat.chatapp.Utility.Constant;
import source.app.chat.chatapp.consumer.MessageConsumer;

public class MainActivity extends Activity {

    private MessageConsumer mConsumer;
    private TextView mOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// The output TextView we'll use to display messages
        mOutput = (TextView) findViewById(R.id.output);

        // Create the consumer
        mConsumer = new MessageConsumer(Constant.Server.IP_ADDRESS, Constant.Server.EXCHANGE_NAME, Constant.Server.EXCHANGE);

        // register for messages
        mConsumer.setOnReceiveMessageHandler(new MessageConsumer.OnReceiveMessageHandler() {

            public void onReceiveMessage(byte[] message) {
                String text = "";
                try {
                    text = new String(message, "UTF8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
                Log.v("mLastMessage ", text);

                mOutput.append("\n" + text);
            }
        });

    }

    private class consumerconnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {

                // Connect to broker
                mConsumer.connectToRabbitMQ();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new consumerconnect().execute();
    }
}
