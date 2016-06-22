package support.source.classes;

import android.app.Application;
import android.content.Intent;

import background.work.services.RunRabbitMQ;
import background.work.services.SendOutboxMessages;
import realm.source.model.CurrentUserRealm;
import realm.source.model.transaction.TransactionCurrentUserRealm;

/**
 * Created by Pankaj Nimgade on 27-05-2016.
 */
public class StartUp extends Application {

    private static final String TAG = StartUp.class.getSimpleName();

    private static CurrentUserRealm item;

    @Override
    public void onCreate() {
        super.onCreate();

        readCurrentUserRealm();
        startDraftServices();
        startReceiveIncomingServices();
    }

    private void startReceiveIncomingServices() {
        if (item != null) {
            RunRabbitMQ runRabbitMQ = RunRabbitMQ.getInstance(getApplicationContext());
            runRabbitMQ.runThread(item);
        }
    }

    private void startDraftServices() {
        Intent intent = new Intent(getApplicationContext(), SendOutboxMessages.class);
        startService(intent);
    }

    private void readCurrentUserRealm() {
        TransactionCurrentUserRealm realm = new TransactionCurrentUserRealm(getApplicationContext());
        item = realm.readCurrentUser();
        realm.closeRealm();
    }

    public static CurrentUserRealm getCurrentUserRealm() {
        return item;
    }

    public static void setCurrentUserRealm(CurrentUserRealm currentUserRealm) {
        if (currentUserRealm == null) {
            item = null;
            return;
        }
        item = new CurrentUserRealm();
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
    }
}
