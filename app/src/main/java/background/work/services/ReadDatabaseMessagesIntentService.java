package background.work.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import realm.source.model.MessageRealm;
import realm.source.model.transaction.TransactionMessageRealm;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReadDatabaseMessagesIntentService extends IntentService {

    private static final String TAG = "ReadDatabaseMessages";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FETCH_FROM_ALL_USERS = "background.work.services.action.ACTION_FETCH_FROM_ALL_USERS";
    public static final String ACTION_FETCH_FROM_SPECIFIC_USER = "background.work.services.action.ACTION_FETCH_FROM_SPECIFIC_USER";

    // TODO: Rename parameters
    private static final String ALL_USERS = "background.work.services.extra.ALL_USERS";
    private static final String SPECIFIC_USER = "background.work.services.extra.SPECIFIC_USER";
    private static final String ORDER_BY = "orderBy";

    public ReadDatabaseMessagesIntentService() {
        super("ReadDatabaseMessagesIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionAllUser(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ReadDatabaseMessagesIntentService.class);
        intent.setAction(ACTION_FETCH_FROM_ALL_USERS);
        intent.putExtra(ALL_USERS, param1);
        intent.putExtra(SPECIFIC_USER, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @param context   this will provide us the context
     * @param action    this will provide us the action we need to take, it's not in use
     *                  right now but it is kept for future changes.
     * @param usersIDs  this will provide us the users information between whom we have to retrive the
     *                  messages for.
     * @param direction This will be either asc|desc to retrieve new|old data (forward|reverse)
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSpecificUser(Context context, String action, String usersIDs, String direction) {
        Intent intent = new Intent(context, ReadDatabaseMessagesIntentService.class);
        intent.setAction(ACTION_FETCH_FROM_SPECIFIC_USER);
        intent.putExtra(ALL_USERS, action);
        intent.putExtra(SPECIFIC_USER, usersIDs);
        intent.putExtra(ORDER_BY, direction);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_FROM_ALL_USERS.equals(action)) {
                final String param1 = intent.getStringExtra(ALL_USERS);
                final String param2 = intent.getStringExtra(SPECIFIC_USER);
                handleActionReadAllUsers(param1, param2);
            } else if (ACTION_FETCH_FROM_SPECIFIC_USER.equals(action)) {
                final String param1 = intent.getStringExtra(ALL_USERS);
                final String userIDs = intent.getStringExtra(SPECIFIC_USER);
                final String direction = intent.getStringExtra(ORDER_BY);
                handleActionSpecificUser(param1, userIDs, direction);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionReadAllUsers(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSpecificUser(final String param1, final String betweenTwoUsers, final String direction) {
        // TODO: Handle action Baz
        Thread threadSpecificUser = new Thread(new Runnable() {
            @Override
            public void run() {
                if (betweenTwoUsers == null)
                    return;
                if (betweenTwoUsers.contentEquals(""))
                    return;
                if (direction == null)
                    return;

                OkHttpClient okHttpClient = new OkHttpClient();

                TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
                long chat_message_id = 0;
                String users[] = betweenTwoUsers.split(",");
                if (direction.contentEquals("forward")) {
                    Log.d(TAG, "run: direction: forward");
                    chat_message_id = realm.findLatestBetweenTwoUsers(Integer.parseInt(users[0]), Integer.parseInt(users[1]));
                } else if (direction.contentEquals("backward")) {
                    Log.d(TAG, "run: direction: backward");
                    chat_message_id = realm.findLastBetweenTowUsers(Integer.parseInt(users[0]), Integer.parseInt(users[1]));
                }
                realm.closeRealm();
                Request request = null;
                String url = null;
                if (chat_message_id == 0) {
                    Log.d(TAG, "run: chat_message_id: " + chat_message_id);
                    url = "http://api.chatndate.com/web/api/chats?users=" + betweenTwoUsers + "&direction=backward";
                } else {
                    Log.d(TAG, "run: chat_message_id: " + chat_message_id);
                    url = "http://api.chatndate.com/web/api/chats?users=" + betweenTwoUsers + "&direction=" + direction + "&startpoint=" + chat_message_id;
                }
                Log.d(TAG, "run: url\n" + url);
                request = new Request.Builder().url(url).build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.d(TAG, "onFailure: ");
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG, "onResponse: \n" + result);
                        if (result == null) {
                            stopSelf();
                            return;
                        }
                        if (result.contentEquals("")) {
                            stopSelf();
                            return;
                        }
                        try {
                            MyResponse myResponse = (new Gson()).fromJson(result, MyResponse.class);
                            if (myResponse != null) {
                                if (myResponse.isSuccess()) {
                                    List<RealmObject> realmObjects = List.class.cast(myResponse.getData());
                                    TransactionMessageRealm transactionMessageRealm
                                            = new TransactionMessageRealm(getApplicationContext());
                                    transactionMessageRealm.saveToRealmDatabase(realmObjects);
                                    transactionMessageRealm.closeRealm();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            stopSelf();
                        } finally {

                        }

                    }
                });
            }
        });

        threadSpecificUser.start();
    }

    private Thread threadAllUsers = new Thread(new Runnable() {
        @Override
        public void run() {

        }
    });


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
