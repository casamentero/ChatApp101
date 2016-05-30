package realm.source.model.transaction;

import android.content.Context;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import realm.source.model.CurrentUserRealm;

/**
 * Created by Pankaj Nimgade on 29-05-2016.
 */
public class TransactionCurrentUserRealm extends Transaction {

    private static final String TAG = TransactionCurrentUserRealm.class.getSimpleName();

    public TransactionCurrentUserRealm(Context context) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(context).name("CurrentUserRealm.realm").build();
        realm = Realm.getInstance(realmConfiguration);
    }

    public CurrentUserRealm readCurrentUser() {
        CurrentUserRealm currentUserRealm = realm.where(CurrentUserRealm.class).findFirst();
        if (currentUserRealm != null) {
            CurrentUserRealm item = new CurrentUserRealm();
            item.setId(currentUserRealm.getId());
            item.setUsername(currentUserRealm.getUsername());
            item.setEmail(currentUserRealm.getEmail());
            item.setPassword_hash(currentUserRealm.getPassword_hash());
            item.setAuth_key(currentUserRealm.getAuth_key());
            item.setConfirmed_at(currentUserRealm.getConfirmed_at());
            item.setUnconfirmed_email(currentUserRealm.getUnconfirmed_email());
            item.setBlocked_at(currentUserRealm.getBlocked_at());
            item.setRegistration_ip(currentUserRealm.getRegistration_ip());
            item.setCreated_at(currentUserRealm.getCreated_at());
            item.setUpdated_at(currentUserRealm.getUpdated_at());
            item.setFlags(currentUserRealm.getFlags());
            item.setRabbitmq_exchange_name(currentUserRealm.getRabbitmq_exchange_name());
            item.setRabbitmq_queue_name(currentUserRealm.getRabbitmq_queue_name());
            item.setRabbitmq_routing_key(currentUserRealm.getRabbitmq_routing_key());
            return item;
        }
        return null;
    }

    @Override
    public List<RealmObject> readFromRealmDatabase() {
        Log.d(TAG, "readFromRealmDatabase: ");
        return null;
    }

    @Override
    public void saveToRealmDatabase(List<RealmObject> realmObjects) {
        if (realmObjects != null) {
            if (realmObjects.size() > 0) {
                realm.beginTransaction();
                realm.where(CurrentUserRealm.class).findAll().deleteAllFromRealm();
                for (RealmObject realmObject : realmObjects) {
                    CurrentUserRealm currentUserRealm = (CurrentUserRealm) realmObject;
                    realm.copyToRealm(currentUserRealm);
                }
                realm.commitTransaction();
            }
        }
    }

    @Override
    public void createItem(RealmObject realmObject) {

    }

    @Override
    public void updateItem(RealmObject realmObject) {

    }

    @Override
    public boolean doesExists(RealmObject realmObject) {
        return false;
    }

    public void deleteCurrentUser() {
        realm.beginTransaction();
        realm.where(CurrentUserRealm.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

}
