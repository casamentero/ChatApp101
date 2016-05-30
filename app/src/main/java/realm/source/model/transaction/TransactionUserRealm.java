package realm.source.model.transaction;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gson.source.model.User;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import realm.source.model.UserRealm;

/**
 * Created by Pankaj Nimgade on 24-05-2016.
 */
public class TransactionUserRealm extends Transaction {

    private static final String TAG = TransactionUserRealm.class.getSimpleName();
    int currentUser;

    public TransactionUserRealm(Context context) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(context).name("UserRealm.realm").build();
        realm = Realm.getInstance(realmConfiguration);
    }

    public List<User> readUser(int currentUser) {
        this.currentUser = currentUser;
        List<User> users = new ArrayList<>();
        List<RealmObject> realmObjects = readFromRealmDatabase();
        Log.d(TAG, "readUser: realmObjects.size(): " + realmObjects.size());
        for (RealmObject realmObject : realmObjects) {
            UserRealm userRealm = (UserRealm) realmObject;
            User user = new User();
            user.setId(userRealm.getId());
            user.setUsername(userRealm.getUsername());
            user.setEmail(userRealm.getEmail());
            user.setPassword_hash(userRealm.getPassword_hash());
            user.setAuth_key(userRealm.getAuth_key());
            user.setConfirmed_at(userRealm.getConfirmed_at());
            user.setUnconfirmed_email(userRealm.getUnconfirmed_email());
            user.setBlocked_at(userRealm.getBlocked_at());
            user.setRegistration_ip(userRealm.getRegistration_ip());
            user.setCreated_at(userRealm.getCreated_at());
            user.setUpdated_at(userRealm.getUpdated_at());
            user.setFlags(userRealm.getFlags());
            user.setRabbitmq_exchange_name(userRealm.getRabbitmq_exchange_name());
            user.setRabbitmq_queue_name(userRealm.getRabbitmq_queue_name());
            user.setRabbitmq_routing_key(userRealm.getRabbitmq_routing_key());
            users.add(user);
        }
        Log.d(TAG, "readUser: users.size(): " + users.size());
        return users;
    }

    @Override
    public List<RealmObject> readFromRealmDatabase() {
        List<RealmObject> realmObjects = new ArrayList<>();
        RealmResults<UserRealm> all = realm.where(UserRealm.class)
                .notEqualTo("id", currentUser)
                .findAll();
        for (UserRealm userRealm : all) {
            realmObjects.add(userRealm);
        }
        Log.d(TAG, "readFromRealmDatabase: realmObjects.size(): " + realmObjects.size());
        return realmObjects;
    }

    @Override
    public void saveToRealmDatabase(List<RealmObject> realmObjects) {
        Log.d(TAG, "saveToRealmDatabase: " + realmObjects.size());
        if (realmObjects != null) {
            for (RealmObject userRealm : realmObjects) {
                if (doesExists(userRealm)) {
                    updateItem(userRealm);
                } else {
                    createItem(userRealm);
                }
            }
        }
    }

    @Override
    public void createItem(RealmObject realmObject) {
        UserRealm userRealm = (UserRealm) realmObject;
        Log.d(TAG, "createItem: Id: " + userRealm.getId());
        realm.beginTransaction();
        realm.copyToRealm(userRealm);
        realm.commitTransaction();
    }

    @Override
    public void updateItem(RealmObject realmObject) {
        UserRealm userRealm = (UserRealm) realmObject;
        Log.d(TAG, "updateItem: Id: " + userRealm.getId());
        realm.beginTransaction();
        UserRealm item = realm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
        if (item != null) {
            item.setId(userRealm.getId());
            item.setUsername(userRealm.getUsername());
            item.setEmail(userRealm.getEmail());
            item.setPassword_hash(userRealm.getPassword_hash());
            item.setAuth_key(userRealm.getAuth_key());
            item.setConfirmed_at(userRealm.getConfirmed_at());
            item.setUnconfirmed_email(userRealm.getUnconfirmed_email());
            item.setBlocked_at(userRealm.getBlocked_at());
            item.setRegistration_ip(userRealm.getRegistration_ip());
            item.setCreated_at(userRealm.getCreated_at());
            item.setUpdated_at(userRealm.getUpdated_at());
            item.setFlags(userRealm.getFlags());
            item.setRabbitmq_exchange_name(userRealm.getRabbitmq_exchange_name());
            item.setRabbitmq_queue_name(userRealm.getRabbitmq_queue_name());
            item.setRabbitmq_routing_key(userRealm.getRabbitmq_routing_key());
        }
        realm.commitTransaction();
    }

    @Override
    public boolean doesExists(RealmObject realmObject) {
        UserRealm userRealm = (UserRealm) realmObject;
        UserRealm item = realm.where(UserRealm.class).equalTo("id", userRealm.getId()).findFirst();
        if (item == null) {
            Log.d(TAG, "doesExists: userRealm.getId(): " + userRealm.getId() + " exists?: " + false);
            return false;
        }
        Log.d(TAG, "doesExists: userRealm.getId(): " + userRealm.getId() + " exists?: " + true);
        return true;
    }


}
