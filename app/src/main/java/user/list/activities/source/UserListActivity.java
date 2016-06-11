package user.list.activities.source;

import android.content.Intent;
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
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import background.work.services.SendOutboxMessages;
import gson.source.model.User;
import io.realm.RealmObject;
import login.activities.source.LoginTestOneActivity;
import message.activity.source.MessageActivity;
import realm.source.model.CurrentUserRealm;
import realm.source.model.UserRealm;
import realm.source.model.transaction.TransactionCurrentUserRealm;
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

        } else {
            Log.d(TAG, "getIntentInformation: it is null");
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
