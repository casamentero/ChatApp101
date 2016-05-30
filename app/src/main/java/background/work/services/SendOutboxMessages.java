package background.work.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import realm.source.model.MessageRealm;

/**
 * Created by Pankaj Nimgade on 25-05-2016.
 * <p>We need a started type of service so it will run indefinitely by calling {@link IntentService#onStartCommand(Intent, int, int)} </p>
 */
public class SendOutboxMessages extends IntentService {

    private static final String TAG = SendOutboxMessages.class.getSimpleName();

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public SendOutboxMessages() {
        super("SendOutboxMessages");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: this called ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: this is called");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!thread.isAlive()) {
            Log.d(TAG, "onHandleIntent: start the thread");
            thread.start();
        } else {
            Log.d(TAG, "onHandleIntent: thread is already started");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            OkHttpClient okHttpClient = new OkHttpClient();

            RealmConfiguration realmConfiguration =
                    new RealmConfiguration.Builder(getApplicationContext()).name("MessageRealm.realm").build();
            Realm realm = Realm.getInstance(realmConfiguration);
            RealmResults<MessageRealm> messageRealms =
                    realm.where(MessageRealm.class).equalTo("chat_message_id", "-128").findAll();
            Log.d(TAG, "run: messageRealms.size(): " + messageRealms.size());
            for (MessageRealm messageRealm : messageRealms) {
                Log.d(TAG, "run: send message: \n" + messageRealm.getChat_message());
                RequestBody formBody = new FormEncodingBuilder()
                        .add("from_id", ""+messageRealm.getFrom_id())
                        .add("to_id", ""+messageRealm.getTo_id())
                        .add("chat_message", messageRealm.getChat_message())
                        .add("languages_id", ""+messageRealm.getLanguages_id())
                        .add("rabbitmq_exchange_name", messageRealm.getRabbitmq_exchange_name())
                        .add("rabbitmq_queue_name", messageRealm.getRabbitmq_queue_name())
                        .add("rabbitmq_routing_key",messageRealm.getRabbitmq_routing_key())
                        .build();

                Request request = new Request.Builder()
                        .url("http://api.chatndate.com/web/api/chats")
                        .post(formBody)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
                    Log.d(TAG, "run: result\n" + result);

                    if (result != null) {
                        if (!result.contentEquals("")) {
                            MyResponse myResponse = (new Gson()).fromJson(result, MyResponse.class);
                            if (myResponse.isSuccess()) {
                                realm.beginTransaction();
                                if (myResponse.getData() != null) {
                                    if (!myResponse.getData().getChat_message_id().contentEquals("")) {
                                        Log.d(TAG, "run: make changes to "+myResponse.getData().getChat_message_id());
                                        messageRealm.setChat_message_id(myResponse.getData().getChat_message_id());
                                    }
                                }
                                realm.commitTransaction();
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            realm.close();
            stopSelf();
        }
    });


    RealmChangeListener<RealmResults<MessageRealm>> listener = new RealmChangeListener<RealmResults<MessageRealm>>() {
        @Override
        public void onChange(RealmResults<MessageRealm> element) {
            Log.d(TAG, "onChange: element.size(): " + element.size());
            MessageRealm messageRealm = element.last();
            Log.d(TAG, "onChange: messageRealm.getChat_message: " + messageRealm.getChat_message());
        }
    };

    private class MyResponse {

        @SerializedName("success")
        private boolean success;

        @SerializedName("data")
        private Data data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    private class Data {

        @SerializedName("chat_message_id")
        private String chat_message_id;

        public String getChat_message_id() {
            return chat_message_id;
        }

        public void setChat_message_id(String chat_message_id) {
            this.chat_message_id = chat_message_id;
        }
    }
}
