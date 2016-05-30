package realm.source.model.transaction;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Pankaj Nimgade on 24-05-2016.
 */
public abstract class Transaction {

    public Realm realm;

    public abstract List<RealmObject> readFromRealmDatabase();
    public abstract void saveToRealmDatabase(List<RealmObject> realmObjects);
    public abstract void createItem(RealmObject realmObject);
    public abstract void updateItem(RealmObject realmObject);
    public abstract boolean doesExists(RealmObject realmObject);

    public void closeRealm() {
        if (realm != null) {
            realm.close();
        }
    }
}
