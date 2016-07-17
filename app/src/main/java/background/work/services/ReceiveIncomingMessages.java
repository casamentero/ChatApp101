package background.work.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

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
public class ReceiveIncomingMessages extends IntentService {

    private static final String TAG = ReceiveIncomingMessages.class.getSimpleName();
    private ListenToRabbitMQ listenToRabbitMQ;
    private CurrentUserRealm mCurrentUserRealm;
    private static boolean isRunning;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ReceiveIncomingMessages() {
        super("ReceiveIncomingMessages");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: this is called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        Log.d(TAG, "onStartCommand: this is called");
        String data = intent.getStringExtra("CurrentUserRealm");
        if (data != null) {
            mCurrentUserRealm = (new Gson()).fromJson(data, CurrentUserRealm.class);
            if (mCurrentUserRealm != null) {
                Log.d(TAG, "onStartCommand: mCurrentUserRealm is been retrieved'");
                listenToRabbitMQ =
                        new ListenToRabbitMQ(mCurrentUserRealm.getRabbitmq_exchange_name(),
                                Constants.RabbitMqCredentials.EXCHANGE_TYPE_TOPIC,
                                mCurrentUserRealm.getRabbitmq_routing_key());
                if (listenToRabbitMQ != null) {
                    if (!listenToRabbitMQ.isRunning()) {
                        Log.d(TAG, "onHandleIntent: start the thread");
                        if (!thread.isAlive()) {
                            thread.start();
                        }
                    } else {
                        Log.d(TAG, "onHandleIntent: already started, So no need to start");
                    }
                }
            }
        }
    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: thread");

            listenToRabbitMQ.setOnReceiveMessageHandler(new OnReceiveMessageHandler() {
                @Override
                public void onReceiveMessage(String message) {
                    try {
                        String text = message;
                        Log.d(TAG, "onReceiveMessage: \n" + text);
                        if (text != null) {
                            if (!text.contentEquals("")) {
                                try {
                                    MessageRealm messageRealm = new MessageRealm();
                                    JSONObject jsonObject = new JSONObject(text);
                                    messageRealm.setId(jsonObject.getLong("id"));
                                    messageRealm.setFrom_id(jsonObject.getLong("from_id"));
                                    messageRealm.setTo_id(jsonObject.getLong("to_id"));
                                    messageRealm.setChat_message_en(jsonObject.getString("chat_message_en"));
                                    messageRealm.setChat_message_es(jsonObject.getString("chat_message_es"));
                                    messageRealm.setChat_message_id(jsonObject.getLong("chat_message_id"));
                                    messageRealm.setLanguages_id(Integer.parseInt(jsonObject.getString("languages_id")));
                                    messageRealm.setCreated_at(Integer.parseInt(jsonObject.getString("created_at")));
                                    RealmConfiguration realmConfiguration =
                                            new RealmConfiguration.Builder(getApplicationContext()).name("MessageRealm.realm").build();
                                    Realm realm = Realm.getInstance(realmConfiguration);
                                    realm.beginTransaction();
                                    realm.copyToRealm(messageRealm);
                                    realm.commitTransaction();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    isRunning = listenToRabbitMQ.isRunning();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        isRunning = false;
                    }
                }
            });

            isRunning = listenToRabbitMQ.readMessages();

        }
    });



}
