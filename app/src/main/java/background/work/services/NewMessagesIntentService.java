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
import support.source.classes.Validator;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NewMessagesIntentService extends IntentService {

    private static final String TAG = NewMessagesIntentService.class.getSimpleName();

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_NEW_MESSAGES = "background.work.services.action.NEW_MESSAGES";
    private static final String ACTION_ALL_MESSAGES = "background.work.services.action.ALL_MESSAGES";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM_USER = "background.work.services.extra.USER";
    private static final String EXTRA_PARAM_DIRECTION = "background.work.services.extra.DIRECTION";

    public NewMessagesIntentService() {
        super("NewMessagesIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionNewMessages(Context context, String user, String direction) {
        Intent intent = new Intent(context, NewMessagesIntentService.class);
        intent.setAction(ACTION_NEW_MESSAGES);
        intent.putExtra(EXTRA_PARAM_USER, user);
        intent.putExtra(EXTRA_PARAM_DIRECTION, direction);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionAllMessages(Context context, String user, String direction) {
        Intent intent = new Intent(context, NewMessagesIntentService.class);
        intent.setAction(ACTION_ALL_MESSAGES);
        intent.putExtra(EXTRA_PARAM_USER, user);
        intent.putExtra(EXTRA_PARAM_DIRECTION, direction);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NEW_MESSAGES.equals(action)) {
                final String user = intent.getStringExtra(EXTRA_PARAM_USER);
                final String direction = intent.getStringExtra(EXTRA_PARAM_DIRECTION);
                getNewMessages(user, direction);
            } else if (ACTION_ALL_MESSAGES.equals(action)) {
                final String user = intent.getStringExtra(EXTRA_PARAM_USER);
                final String direction = intent.getStringExtra(EXTRA_PARAM_DIRECTION);
                getAllMessages(user, direction);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void getNewMessages(String user, String direction) {
        // TODO: Handle action Foo
        try {
            String url = "http://api.chatndate.com/web/api/chats?users=" + user + "&direction=" + direction;
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG, "onFailure: ");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String json_result = response.body().string();
                    Log.d(TAG, "onResponse: result\n" + json_result);
                    if (json_result != null) {
                        if (!json_result.contentEquals("")) {
                            try {
                                MyResponse myResponse = (new Gson()).fromJson(json_result, MyResponse.class);
                                if (myResponse.isSuccess()) {
                                    TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
                                    List<RealmObject> realmObjects = List.class.cast(myResponse.getData());
                                    realm.saveToRealmDatabase(realmObjects);
                                    realm.closeRealm();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                stopSelf();
                            } finally {

                            }

                        }
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void getAllMessages(String user, String direction) {
        // TODO: Handle action Baz

        getMessagesFromURL(user, direction, 1);
    }

    private void getMessagesFromURL(final String user, final String direction, final int page) {
        try {
            final TransactionMessageRealm realm = new TransactionMessageRealm(getApplicationContext());
            final int messageCount = realm.messagesCount(Integer.parseInt(user));
            realm.closeRealm();

            OkHttpClient okHttpClient = new OkHttpClient();
            Request request =
                    new Request.Builder()
                            .url("http://api.chatndate.com/web/api/chats?users=" + user + "&direction=backward" + "&page=" + page)
                            .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG, "onFailure: ");
                }

                @Override
                public void onResponse(Response response) {
                    try {
                        String json_result = response.body().string();
                        Log.d(TAG, "onResponse: json_result: \n" + json_result);
                        MyResponse myResponse = (new Gson()).fromJson(json_result, MyResponse.class);
                        if (myResponse.isSuccess()) {
                            TransactionMessageRealm messageRealm = new TransactionMessageRealm(getApplicationContext());
                            List<RealmObject> realmObjects = List.class.cast(myResponse.getData());
                            messageRealm.saveToRealmDatabase(realmObjects);
                            messageRealm.closeRealm();
                            String totalMessages = response.header("X-Pagination-Total-Count");
                            String currentPage = response.header("X-Pagination-Current-Page");

                            if (Validator.getNumber(totalMessages) > messageCount) {
                                int nextPage = Validator.getNumber(currentPage);
                                nextPage++;
                                getMessagesFromURL(user, direction, nextPage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
