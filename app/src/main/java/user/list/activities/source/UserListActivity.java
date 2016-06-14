package user.list.activities.source;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import background.work.services.NewMessagesIntentService;
import background.work.services.SendOutboxMessages;
import constants.app.source.Constants;
import gson.source.model.User;
import io.realm.RealmObject;
import login.activities.source.LoginTestOneActivity;
import message.activity.source.MessageActivity;
import realm.source.model.CurrentUserRealm;
import realm.source.model.MessageRealm;
import realm.source.model.UserRealm;
import realm.source.model.transaction.TransactionCurrentUserRealm;
import realm.source.model.transaction.TransactionMessageRealm;
import realm.source.model.transaction.TransactionUserRealm;
import source.app.chat.chatapp.R;
import support.source.classes.StartUp;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = UserListActivity.class.getSimpleName();
    public static final String TO_USER_KEY = "to_user";

    private RecyclerView userList_RecyclerView;
    private CurrentUserRealm currentUserRealm;
    private List<User> users;
    private UserRecyclerAdapter userRecyclerAdapter;
    private MyRabbitMqReceiver myRabbitMqReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeUI();
    }

    private void initializeUI() {
        userList_RecyclerView = (RecyclerView) findViewById(R.id.UserListActivity_user_list_RecyclerView);
        userList_RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setTitle("User List");
        getIntentInformation();
    }


    public void getIntentInformation() {
        currentUserRealm = StartUp.getCurrentUserRealm();
        if (currentUserRealm != null) {
            Log.d(TAG, "getIntentInformation: \n" + (new Gson()).toJson(currentUserRealm));
            NewMessagesIntentService
                    .startActionAllMessages(getApplicationContext(), "" + currentUserRealm.getId(), "" + 1);

        } else {
            Log.d(TAG, "getIntentInformation: it is null");
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
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
    protected void onResume() {
        super.onResume();
        if (currentUserRealm != null) {
            loadRecyclerView();
        }
        loadUserList();
        Intent intent = new Intent(getApplicationContext(), SendOutboxMessages.class);
        startService(intent);
        myRabbitMqReceiver = new MyRabbitMqReceiver();
        myRabbitMqReceiver.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myRabbitMqReceiver.cancel(true);
    }

    private void loadUserList() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://api.chatndate.com/web/api/users").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "onResponse: \n" + result);
                if (result == null)
                    return;
                if (result.length() == 0)
                    return;
                MyResponse myResponse = (new Gson()).fromJson(result, MyResponse.class);
                if (!myResponse.isSuccess())
                    return;
                String data = (new Gson()).toJson(myResponse.getData());
                Type type = new com.google.gson.reflect.TypeToken<ArrayList<UserRealm>>() {
                }.getType();
                List<UserRealm> userRealms = (new Gson()).fromJson(data, type);
                for (UserRealm realm : userRealms) {
                    Log.d(TAG, "onResponse: realm.getEmail(): " + realm.getEmail());
                }
                TransactionUserRealm transaction = new TransactionUserRealm(getApplicationContext());
                List<RealmObject> realmObjects = List.class.cast(userRealms);
                transaction.saveToRealmDatabase(realmObjects);
                Log.d(TAG, "onResponse: transaction.readUser().size(): " + transaction.readUser(currentUserRealm.getId()).size());
                transaction.closeRealm();
                if (currentUserRealm != null) {
                    loadRecyclerView();
                }
            }
        });
    }

    private void loadRecyclerView() {
        TransactionUserRealm transaction = new TransactionUserRealm(getApplicationContext());
        users = transaction.readUser(currentUserRealm.getId());
        userRecyclerAdapter = new UserRecyclerAdapter(getApplicationContext(), users);
        UserListActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userList_RecyclerView.setAdapter(userRecyclerAdapter);
                userList_RecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, float x, float y) {
                        User user = users.get(position);
                        Toast.makeText(getApplicationContext(), "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                        Intent messageActivity_Intent = new Intent(getApplicationContext(), MessageActivity.class);
                        messageActivity_Intent.putExtra(TO_USER_KEY, (new Gson()).toJson(user));
                        startActivity(messageActivity_Intent);
                    }
                }));
                userRecyclerAdapter.notifyDataSetChanged();
                userList_RecyclerView.invalidate();
            }
        });
        transaction.closeRealm();
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
                channel.queueBind(queueName, Constants.RabbitMqCredentials.EXCHANGE_NAME, currentUserRealm.getRabbitmq_routing_key());

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


    private class MyResponse {
        @SerializedName("success")
        private boolean success;

        @SerializedName("data")
        private ArrayList<User> data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public ArrayList<User> getData() {
            return data;
        }

        public void setData(ArrayList<User> data) {
            this.data = data;
        }
    }
}
