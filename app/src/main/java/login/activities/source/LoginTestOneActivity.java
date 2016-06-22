package login.activities.source;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import background.work.services.RunRabbitMQ;
import gson.source.model.User;
import io.realm.RealmObject;
import realm.source.model.CurrentUserRealm;
import realm.source.model.transaction.TransactionCurrentUserRealm;
import source.app.chat.chatapp.R;
import source.app.chat.chatapp.databinding.ActivityLoginTestOneBinding;
import support.source.classes.StartUp;
import support.source.classes.TextWatcherAdapter;
import user.list.activities.source.UserListActivity;

public class LoginTestOneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginTestOneActivity.class.getSimpleName();
    public static final String KEY_USER_DATA = "USER_DATA";
    private Context context;

    private UserLoginScreen userLoginScreen;
    private ProgressDialog progressDialog;
    private FloatingActionButton call_FloatingActionButton;
    private CurrentUserRealm mCurrentUserRealm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginTestOneBinding dataBindingUtil = DataBindingUtil.setContentView(this, R.layout.activity_login_test_one);
        userLoginScreen = new UserLoginScreen();
        dataBindingUtil.setLoginUser(userLoginScreen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeUI();

    }

    private void initializeUI() {

        context = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        call_FloatingActionButton = (FloatingActionButton) findViewById(R.id.LoginTestOneActivity_call_FloatingActionButton);
        call_FloatingActionButton.setOnClickListener(this);

        mCurrentUserRealm = StartUp.getCurrentUserRealm();
        if (mCurrentUserRealm != null) {
            Intent my_Intent = new Intent(getApplicationContext(), UserListActivity.class);
            startActivity(my_Intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LoginTestOneActivity_call_FloatingActionButton:

                Log.d(TAG, "onClickSend: " + userLoginScreen.getUser_ID().get());
                Toast.makeText(getApplicationContext(), "click" + userLoginScreen.getUser_ID().get(), Toast.LENGTH_SHORT).show();
                if (userLoginScreen.getUser_ID().get() == null) {
                    Toast.makeText(getApplicationContext(), "Enter a number", Toast.LENGTH_SHORT).show();
                    return;
                }

                OkHttpClient okHttpClient = new OkHttpClient();
                final Request request = new Request.Builder().url("http://api.chatndate.com/web/api/users/" + userLoginScreen.getUser_ID().get()).build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String json_result = response.body().string();
                        Log.d(TAG, "onResponse: \n" + json_result);
                        if (json_result == null)
                            return;
                        if (json_result.contentEquals(""))
                            return;

                        try {
                            MyResponse myResponse = (new Gson()).fromJson(json_result, MyResponse.class);
                            if (!myResponse.isSuccess())
                                return;
                            String result = (new Gson()).toJson(myResponse.getData());
                            CurrentUserRealm currentUserRealm = (new Gson()).fromJson(result, CurrentUserRealm.class);
                            Log.d(TAG, "onResponse: email: " + currentUserRealm.getEmail());
                            TransactionCurrentUserRealm realm = new TransactionCurrentUserRealm(getApplicationContext());
                            List<CurrentUserRealm> currentUserRealms = new ArrayList<CurrentUserRealm>();
                            currentUserRealms.add(currentUserRealm);
                            List<RealmObject> realmObjects = List.class.cast(currentUserRealms);
                            realm.saveToRealmDatabase(realmObjects);
                            realm.closeRealm();
                            mCurrentUserRealm = currentUserRealm;
                            StartUp.setCurrentUserRealm(mCurrentUserRealm);

                            Log.d(TAG, "onResponse: relam gson instance\n" + (new Gson()).toJson(currentUserRealm));
                            Intent my_Intent = new Intent(getApplicationContext(), UserListActivity.class);
                            startActivity(my_Intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                break;
        }
    }

    private void startReceiveIncomingServices(CurrentUserRealm item) {
        if (item != null) {
            RunRabbitMQ runRabbitMQ = RunRabbitMQ.getInstance(getApplicationContext());
            runRabbitMQ.runThread(item);
        }
    }

    public class UserLoginScreen {

        public ObservableField<String> user_ID = new ObservableField<>();

        public TextWatcher watcher = new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (s != null) {
                    if (s.toString() != null) {
                        user_ID.set(s.toString());
                    }
                }
            }
        };

        public ObservableField<String> getUser_ID() {
            return user_ID;
        }

        public void setUser_ID(ObservableField<String> user_ID) {
            this.user_ID = user_ID;
        }

        public TextWatcher getWatcher() {
            return watcher;
        }

        public void setWatcher(TextWatcher watcher) {
            this.watcher = watcher;
        }
    }


    private class MyResponse {
        @SerializedName("success")
        private boolean success;

        @SerializedName("data")
        private User data;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public User getData() {
            return data;
        }

        public void setData(User data) {
            this.data = data;
        }
    }


}
