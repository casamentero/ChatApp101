package support.source.classes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Pankaj Nimgade on 27-05-2016.
 */
public class MySharedPreferences {

    private static final String SHARED_PREFERENCES_FILE = "SharedPreferences.xml";

    private static SharedPreferences sharedPreferences;

    private static SharedPreferences.Editor editor;

    private static final String KEY_LANGUAGE = "key_language";// ENGLISH(en) = 1, SPANISH(es) = 2
    private static final int DEFAULT_LANGUAGE = 1;

    private MySharedPreferences() {
    }

    private static class SingletonHolder {
        private static final MySharedPreferences INSTANCE = new MySharedPreferences();
    }

    public static MySharedPreferences getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return SingletonHolder.INSTANCE;
    }

    public int getLanguage() {
        return sharedPreferences.getInt(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }


    public void setLanguage(int languages_id) {
        if (languages_id == 1 || languages_id == 2) {
            editor.putInt(KEY_LANGUAGE, languages_id).commit();
        }
    }
}
