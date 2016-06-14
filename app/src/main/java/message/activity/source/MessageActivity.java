package message.activity.source;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import background.work.services.NewMessagesIntentService;
import background.work.services.ReadDatabaseMessagesIntentService;
import background.work.services.SendOutboxMessages;
import constants.app.source.Constants;
import gson.source.model.Message;
import gson.source.model.User;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import login.activities.source.LoginTestOneActivity;
import realm.source.model.CurrentUserRealm;
import realm.source.model.MessageRealm;
import realm.source.model.transaction.TransactionCurrentUserRealm;
import realm.source.model.transaction.TransactionMessageRealm;
import source.app.chat.chatapp.R;
import source.app.chat.chatapp.databinding.ActivityMessageBinding;
import support.source.classes.StartUp;
import support.source.classes.TextWatcherAdapter;
import user.list.activities.source.UserListActivity;

public class MessageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MessageActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView message_RecyclerView;
    private List<Message> messages;
    private MessageRecyclerAdapter adapter;
    private MessageActivityScreen messageActivityScreen;
    private CurrentUserRealm from_CurrentUserRealm;
    private User to_User;

    private Realm realm;
    private RealmResults<MessageRealm> messageRealms;
    private MyRabbitMqReceiver myRabbitMqReceiver;

    // Receiver 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMessageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_message);
        messageActivityScreen = new MessageActivityScreen();
        binding.setMessageActivityScreen(messageActivityScreen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (messageActivityScreen.getMessage() != null) {
                    String text = messageActivityScreen.getMessage().get();
                    if (text != null) {
                        if (!text.contentEquals("")) {
                            writeToRealm(text);
                        }
                    }
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDraftServices();
        loadRecyclerView();
        myRabbitMqReceiver = new MyRabbitMqReceiver();
        myRabbitMqReceiver.execute();
    }

    private void initializeUI() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.MessageActivity_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        message_RecyclerView = (RecyclerView) findViewById(R.id.MessageActivity_messages_RecyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setStackFromEnd(true);
        message_RecyclerView.setLayoutManager(linearLayoutManager);


        getIntentInformation();
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        if ((to_User == null) && (from_CurrentUserRealm == null)) {
            Log.d(TAG, "getIntentInformation: to_User and from_CurrentUserRealm");
            return;
        }


        setTitle("" + to_User.getUsername());

        TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
        messages = realm.readMessages(from_CurrentUserRealm.getId(), to_User.getId());
        Log.d(TAG, "loadRecyclerView: reading messages from to_User.getId(): " + to_User.getId());
        realm.readFromRealmDatabase();
        MessageActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new MessageRecyclerAdapter(getApplicationContext(), messages);
                message_RecyclerView.setAdapter(adapter);
//                message_RecyclerView.smoothScrollToPosition(messages.size()-1);
            }
        });
        realm.closeRealm();

    }

    public void getIntentInformation() {
        Intent my_Intent = getIntent();
        to_User = (new Gson()).fromJson(my_Intent.getStringExtra(UserListActivity.TO_USER_KEY), User.class);
        from_CurrentUserRealm = StartUp.getCurrentUserRealm();
        if ((to_User == null) && (from_CurrentUserRealm == null)) {
            Log.d(TAG, "getIntentInformation: to_User and from_CurrentUserRealm");
            return;
        }

        ReadDatabaseMessagesIntentService
                .startActionSpecificUser(getApplicationContext(),
                        ReadDatabaseMessagesIntentService.ACTION_FETCH_FROM_SPECIFIC_USER,
                        to_User.getId() + "," + from_CurrentUserRealm.getId(), "forward");
        // ase == forward == new data
        // desc == reverse == old data

        NewMessagesIntentService
                .startActionAllMessages(getApplicationContext(), "" + from_CurrentUserRealm.getId(), "" + 1);

        RealmConfiguration realmConfiguration =
                new RealmConfiguration.Builder(getApplicationContext()).name("MessageRealm.realm").build();
        realm = Realm.getInstance(realmConfiguration);

        messageRealms = realm.where(MessageRealm.class)
                .equalTo("from_id", from_CurrentUserRealm.getId())
                .or()
                .equalTo("to_id", from_CurrentUserRealm.getId())
                .findAll();
        messageRealms.addChangeListener(listener);
    }

    private void writeToRealm(String text) {
        if ((to_User == null) && (from_CurrentUserRealm == null)) {
            Log.d(TAG, "getIntentInformation: to_User and from_CurrentUserRealm");
            return;
        }
        Log.d(TAG, "writeToRealm: text: " + text);
        TransactionMessageRealm transaction = new TransactionMessageRealm(getApplicationContext());
        MessageRealm messageRealm = new MessageRealm();
        messageRealm.setChat_message_id(-128);
        messageRealm.setChat_message("" + text);
        messageRealm.setLanguages_id(1);
        messageRealm.setTo_id(to_User.getId());
        messageRealm.setFrom_id(from_CurrentUserRealm.getId());
        messageRealm.setRabbitmq_exchange_name(to_User.getRabbitmq_exchange_name());
        messageRealm.setRabbitmq_queue_name(to_User.getRabbitmq_queue_name());
        messageRealm.setRabbitmq_routing_key(to_User.getRabbitmq_routing_key());
        transaction.writeToRealm(messageRealm);
        messageActivityScreen.getMessage().set("");
        loadRecyclerView();
        startDraftServices();
        NewMessagesIntentService.
                startActionNewMessages(getApplicationContext(),
                        "" + from_CurrentUserRealm.getId(), Constants.ChatAPI.DIRECTION_BACKWARD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        if (id == R.id.home) {
            if (realm != null) {
                realm.close();
            }
            myRabbitMqReceiver.cancel(true);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        TransactionCurrentUserRealm realm = new TransactionCurrentUserRealm(getApplicationContext());
        realm.deleteCurrentUser();
        realm.closeRealm();
        StartUp.setCurrentUserRealm(null);
        Intent intent = new Intent(getApplicationContext(), LoginTestOneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (realm != null) {
            realm.close();
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startDraftServices() {
        Intent intent = new Intent(getApplicationContext(), SendOutboxMessages.class);
        startService(intent);
    }

    @Override
    public void onRefresh() {
        downloadPreviousPages();
    }

    private void downloadPreviousPages() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
        long last_Chat_message_id = realm.findLastBetweenTowUsers(from_CurrentUserRealm.getId(), to_User.getId());
        String url = null;
        if (last_Chat_message_id == 0) {
            url = "http://api.chatndate.com/web/api/chats?users=" +
                    from_CurrentUserRealm.getId() + "," + to_User.getId() + "&direction=backward";
        } else {
            url = "http://api.chatndate.com/web/api/chats?users=" +
                    from_CurrentUserRealm.getId() + "," + to_User.getId() + "&direction=backward" +
                    "&startpoint=" + last_Chat_message_id;
        }
        Log.d(TAG, "downloadPreviousPages: url\n" + url);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request =
                new Request.Builder()
                        .url(url)
                        .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: ");
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) {
                try {
                    String json_result = response.body().string();
                    Log.d(TAG, "onResponse: \n" + json_result);
                    if (json_result == null)
                        return;
                    MyResponse myResponse = (new Gson()).fromJson(json_result, MyResponse.class);
                    if (myResponse.isSuccess()) {
                        TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
                        List<RealmObject> realmObjects = List.class.cast(myResponse.getData());
                        realm.saveToRealmDatabase(realmObjects);
                        messages = realm.readMessages(from_CurrentUserRealm.getId(), to_User.getId());
                        final int count = messages.size();
                        Log.d(TAG, "onResponse: reading messages from to_User.getId(): " + to_User.getId());
                        realm.readFromRealmDatabase();
                        realm.closeRealm();
                        MessageActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new MessageRecyclerAdapter(getApplicationContext(), messages);
                                message_RecyclerView.setAdapter(adapter);
                                if (count > 0) {
                                    linearLayoutManager.setStackFromEnd(false);
                                }
                                if (swipeRefreshLayout.isRefreshing()) {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                                message_RecyclerView.invalidate();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    MessageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });

    }

    private class MyRabbitMqReceiver extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground: ");
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(Constants.RabbitMqCredentials.IP_ADRRESS);
                factory.setUsername(Constants.RabbitMqCredentials.USERNAME);
                factory.setPassword(Constants.RabbitMqCredentials.PASSWORD);
                factory.setPort(Constants.RabbitMqCredentials.PORT);
                Connection connection = factory.newConnection();

                Channel channel = connection.createChannel();
                channel.exchangeDeclare(Constants.RabbitMqCredentials.EXCHANGE_NAME, "topic", true);

                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, Constants.RabbitMqCredentials.EXCHANGE_NAME, from_CurrentUserRealm.getRabbitmq_routing_key());

                Log.d(TAG, " [*] Waiting for messages. To exit press CTRL+C");
                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(" [x] Received '" + message + "'");
                        Log.d(TAG, "handleDelivery: \n" + message);
                        try {
                            RabbitMqResponse rabbitMqResponse = (new Gson()).fromJson(message, RabbitMqResponse.class);
                            MessageRealm messageRealm = new MessageRealm();
                            messageRealm.setFrom_id(rabbitMqResponse.getFrom_id());
                            messageRealm.setTo_id(rabbitMqResponse.getTo_id());
                            messageRealm.setChat_message(rabbitMqResponse.getChat_message());
                            messageRealm.setChat_message_id(rabbitMqResponse.getChat_message_id());
                            messageRealm.setLanguages_id(rabbitMqResponse.getLanguages_id());
                            messageRealm.setCreated_at(rabbitMqResponse.getCreated_at());
                            TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
                            List<RealmObject> realmObjects = new ArrayList<>();
                            realmObjects.add(messageRealm);
                            realm.saveToRealmDatabase(realmObjects);
                            realm.closeRealm();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                channel.basicConsume(queueName, true, consumer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "onCancelled: ");
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            Log.d(TAG, "onCancelled: ");
        }

        private class RabbitMqResponse {
            private long from_id;
            private long to_id;
            private String chat_message;
            private long chat_message_id;
            private int languages_id;
            private long created_at;

            public long getFrom_id() {
                return from_id;
            }

            public void setFrom_id(long from_id) {
                this.from_id = from_id;
            }

            public long getTo_id() {
                return to_id;
            }

            public void setTo_id(long to_id) {
                this.to_id = to_id;
            }

            public String getChat_message() {
                return chat_message;
            }

            public void setChat_message(String chat_message) {
                this.chat_message = chat_message;
            }

            public long getChat_message_id() {
                return chat_message_id;
            }

            public void setChat_message_id(long chat_message_id) {
                this.chat_message_id = chat_message_id;
            }

            public int getLanguages_id() {
                return languages_id;
            }

            public void setLanguages_id(int languages_id) {
                this.languages_id = languages_id;
            }

            public long getCreated_at() {
                return created_at;
            }

            public void setCreated_at(long created_at) {
                this.created_at = created_at;
            }
        }
    }

    public class MessageActivityScreen {

        public ObservableField<String> message = new ObservableField<>();

        public TextWatcher watcher = new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (s != null) {
                    if (s.toString() != null) {
                        message.set(s.toString());
                    }
                }
            }
        };

        public ObservableField<String> getMessage() {
            return message;
        }

        public void setMessage(ObservableField<String> message) {
            this.message = message;
        }

        public TextWatcher getWatcher() {
            return watcher;
        }

        public void setWatcher(TextWatcher watcher) {
            this.watcher = watcher;
        }
    }

    private RealmChangeListener<RealmResults<MessageRealm>>
            listener = new RealmChangeListener<RealmResults<MessageRealm>>() {
        @Override
        public void onChange(RealmResults<MessageRealm> element) {
            loadRecyclerView();
        }
    };

    private class MyResponse {
        @SerializedName("success")
        private boolean success;

        @SerializedName("data")
        private ArrayList<MessageRealm> data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public ArrayList<MessageRealm> getData() {
            return data;
        }

        public void setData(ArrayList<MessageRealm> data) {
            this.data = data;
        }
    }
}

    /*

    {
  "from_id": "1",
  "to_id": "2",
  "chat_message": "a text message",
  "chat_message_id": "9121465492332",
  "languages_id": "1",
  "created_at": 1465492332
    }
    * */