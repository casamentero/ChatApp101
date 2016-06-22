package background.work.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import constants.app.source.Constants;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import rabbitmq.source.ListenToRabbitMQ;
import rabbitmq.source.OnReceiveMessageHandler;
import realm.source.model.CurrentUserRealm;
import realm.source.model.MessageRealm;

/**
 * Created by Pankaj Nimgade on 29-05-2016.
 */
public class RunRabbitMQ {

    private static final String TAG = RunRabbitMQ.class.getSimpleName();

    private static boolean isRunning;
    private static Context context;
    private static ListenToRabbitMQ listenToRabbitMQ;
    private CurrentUserRealm mCurrentUserRealm;

    private RunRabbitMQ() {
    }

    private static class SingletonHolder {
        private static final RunRabbitMQ INSTANCE = new RunRabbitMQ();
    }

    public static RunRabbitMQ getInstance(Context mContext) {
        context = mContext;
        return SingletonHolder.INSTANCE;
    }

    public void runThread(CurrentUserRealm mCurrentUserRealm) {
        Log.d(TAG, "runThread: " + isRunning);
        if (!isRunning) {
            this.mCurrentUserRealm = mCurrentUserRealm;
            listenToRabbitMQ = new ListenToRabbitMQ(this.mCurrentUserRealm.getRabbitmq_exchange_name(),
                    Constants.RabbitMqCredentials.EXCHANGE_TYPE_TOPIC,
                    this.mCurrentUserRealm.getRabbitmq_routing_key());

            if (!this.thread.isAlive()) {
                if (this.thread.getState() == Thread.State.NEW ) {
                    this.thread.start();
                }
            }

        } else {
            Log.d(TAG, "runThread: isRunning: " + isRunning);
        }
    }


    private static Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run method: thread: " + isRunning);
            isRunning = listenToRabbitMQ.readMessages();

            listenToRabbitMQ.setOnReceiveMessageHandler(new OnReceiveMessageHandler() {
                @Override
                public void onReceiveMessage(byte[] message) {
                    try {
                        String text = new String(message, "UTF8");
                        Log.d(TAG, "onReceiveMessage: \n" + text);
                        if (text != null) {
                            if (!text.contentEquals("")) {
                                try {
                                    MessageRealm messageRealm = new MessageRealm();
                                    JSONObject jsonObject = new JSONObject(text);
                                    messageRealm.setFrom_id(Long.parseLong(jsonObject.getString("from_id")));
                                    messageRealm.setTo_id(Long.parseLong(jsonObject.getString("to_id")));
                                    messageRealm.setChat_message(jsonObject.getString("chat_message"));
                                    messageRealm.setChat_message_id(Long.parseLong(jsonObject.getString("chat_message_id")));
                                    messageRealm.setLanguages_id(Integer.parseInt(jsonObject.getString("languages_id")));
                                    messageRealm.setCreated_at(jsonObject.getLong("created_at"));
                                    RealmConfiguration realmConfiguration =
                                            new RealmConfiguration.Builder(context).name("MessageRealm.realm").build();
                                    Realm realm = Realm.getInstance(realmConfiguration);
                                    realm.beginTransaction();
                                    realm.copyToRealm(messageRealm);
                                    realm.commitTransaction();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    isRunning = listenToRabbitMQ.isRunning();
                                    if (!isRunning) {
                                        return;
                                    }
                                }
                            }
                        }

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        isRunning = listenToRabbitMQ.isRunning();
                    }
                }
            });
        }
    });
}
