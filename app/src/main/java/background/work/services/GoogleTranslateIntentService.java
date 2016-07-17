package background.work.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import realm.source.model.MessageRealm;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GoogleTranslateIntentService extends IntentService {

    private static final String TAG = "GoogleTranslate";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_ENGLISH_TO_SPANISH = "background.work.services.action.ENGLISH_TO_SPANISH";
    private static final String ACTION_SPANISH_TO_ENGLISH = "background.work.services.action.SPANISH_TO_ENGLISH";
    //    private static final String API_KEY = "AIzaSyCb47xXjpyCdV9xDRftgCBeYGlCm1g2QSI";
    private static final String API_KEY = "AIzaSyBvW5Bye05zI1zeIwVErVeD1Ze-5NKUh0I";

    private static final String url = "https://www.googleapis.com/language/translate/v2?key=" + API_KEY;

    // TODO: Rename parameters
    private static final String EXTRA_MESSAGE = "background.work.services.extra.MESSAGE";

    public GoogleTranslateIntentService() {
        super("GoogleTranslateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionEnglishToSpanish(Context context, String param1) {
        Intent intent = new Intent(context, GoogleTranslateIntentService.class);
        intent.setAction(ACTION_ENGLISH_TO_SPANISH);
        intent.putExtra(EXTRA_MESSAGE, param1);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSpanishToEnglish(Context context) {
        Intent intent = new Intent(context, GoogleTranslateIntentService.class);
        intent.setAction(ACTION_SPANISH_TO_ENGLISH);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!thread.isAlive()) {
            Log.d(TAG, "onHandleIntent: start the thread");
            thread.start();
        } else {
            Log.d(TAG, "onHandleIntent: thread is already started");
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private synchronized void handleActionEnglishToSpanish() {
        // TODO: Handle action Foo

    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

            OkHttpClient okHttpClient = new OkHttpClient();

            RealmConfiguration realmConfiguration =
                    new RealmConfiguration.Builder(getApplicationContext()).name("MessageRealm.realm").build();
            Realm realm = Realm.getInstance(realmConfiguration);
            RealmResults<MessageRealm> messageRealms =
                    realm.where(MessageRealm.class).equalTo("chat_message_id", -127).findAll();
            Log.d(TAG, "run: messageRealms.size(): " + messageRealms.size());

            for (MessageRealm messageRealm : messageRealms) {
                StringBuilder url_stringBuilder = new StringBuilder(url);
                if (messageRealm.getLanguages_id() == 1) {
                    // 1 == English (en)
                    url_stringBuilder.append("&q=" + messageRealm.getChat_message_en().replace(" ", "+"));
                    url_stringBuilder.append("&source=en&target=es");
                } else if (messageRealm.getLanguages_id() == 2) {
                    // 2 == Spanish (es)
                    url_stringBuilder.append("&q=" + messageRealm.getChat_message_es().replace(" ", "+"));
                    url_stringBuilder.append("&source=es&target=en");
                } else {
                    return;
                }
                Log.d(TAG, "run: \n" + url_stringBuilder.toString());
                Request request = new Request.Builder().url(url_stringBuilder.toString()).build();
                try {
                    Response execute = okHttpClient.newCall(request).execute();
                    String result = execute.body().string();
                    Log.d(TAG, "run: translated text: " + readJsonData(result));
                    realm.beginTransaction();
                    if (messageRealm.getLanguages_id() == 1) {
                        // message was in english and so translated to spanish
                        messageRealm.setChat_message_es("" + readJsonData(result));
                        messageRealm.setChat_message_id(-128);
                    } else if (messageRealm.getLanguages_id() == 2) {
                        // message was in spanish and so translated to english
                        messageRealm.setChat_message_en("" + readJsonData(result));
                        messageRealm.setChat_message_id(-128);
                    }
                    realm.commitTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });


    private String readJsonData(String jsonData) {
        String result = "";
        JsonElement jsonElement = (new JsonParser()).parse(jsonData);
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        JsonObject data = asJsonObject.getAsJsonObject("data");
        JsonArray translations = data.getAsJsonArray("translations");
        JsonObject language_JsonObject = translations.get(0).getAsJsonObject();
        JsonElement jsonElement1 = language_JsonObject.get("translatedText");
        result = jsonElement1.getAsString();
        return result;
    }

}
