package realm.source.model.transaction;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gson.source.model.Message;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import realm.source.model.MessageRealm;

/**
 * Created by Pankaj Nimgade on 25-05-2016.
 */
public class TransactionMessageRealm extends Transaction {

    private static final String TAG = TransactionMessageRealm.class.getSimpleName();

    public TransactionMessageRealm(Context context) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(context).name("MessageRealm.realm").build();
        realm = Realm.getInstance(realmConfiguration);
    }

    public List<Message> readMessages(int from_id, int to_id) {
        Log.d(TAG, "readMessages: from_id: " + from_id);
        List<Message> messages = new ArrayList<>();
        RealmResults<MessageRealm> all = realm.where(MessageRealm.class)
                .equalTo("from_id", from_id)
                .equalTo("to_id", to_id)
                .notEqualTo("chat_message_id", -128)
                .or()
                .equalTo("from_id", to_id)
                .equalTo("to_id", from_id)
                .notEqualTo("chat_message_id", -128)
                .findAllSorted("chat_message_id", Sort.ASCENDING);

        Log.d(TAG, "readMessages: all.size(): " + all.size());
        for (MessageRealm messageRealm : all) {
            Message message = new Message();
            message.setFrom_id(messageRealm.getFrom_id());
            message.setTo_id(messageRealm.getTo_id());
            message.setChat_message_en(messageRealm.getChat_message_en());
            message.setChat_message_es(messageRealm.getChat_message_es());
            message.setChat_message_id(messageRealm.getChat_message_id());
            message.setLanguages_id(messageRealm.getLanguages_id());
            message.setCreated_at(messageRealm.getCreated_at());
            message.setRabbitmq_routing_key(messageRealm.getRabbitmq_routing_key());
            message.setRabbitmq_exchange_name(messageRealm.getRabbitmq_exchange_name());
            message.setRabbitmq_queue_name(messageRealm.getRabbitmq_queue_name());
            Log.d(TAG, "readMessages: message:" + message.toString());
            messages.add(message);
        }

        RealmResults<MessageRealm> all_127 = realm.where(MessageRealm.class)
                .equalTo("from_id", from_id)
                .equalTo("to_id", to_id)
                .equalTo("chat_message_id", -127)
                .or()
                .equalTo("from_id", to_id)
                .equalTo("to_id", from_id)
                .equalTo("chat_message_id", -127).findAll();
        for (MessageRealm messageRealm : all_127) {
            Message message = new Message();
            message.setFrom_id(messageRealm.getFrom_id());
            message.setTo_id(messageRealm.getTo_id());
            message.setChat_message_en(messageRealm.getChat_message_en());
            message.setChat_message_es(messageRealm.getChat_message_es());
            message.setChat_message_id(messageRealm.getChat_message_id());
            message.setLanguages_id(messageRealm.getLanguages_id());
            message.setCreated_at(messageRealm.getCreated_at());
            message.setRabbitmq_routing_key(messageRealm.getRabbitmq_routing_key());
            message.setRabbitmq_exchange_name(messageRealm.getRabbitmq_exchange_name());
            message.setRabbitmq_queue_name(messageRealm.getRabbitmq_queue_name());
            Log.d(TAG, "readMessages: message:" + message.toString());
            messages.add(message);
        }

        RealmResults<MessageRealm> all_128 = realm.where(MessageRealm.class)
                .equalTo("from_id", from_id)
                .equalTo("to_id", to_id)
                .equalTo("chat_message_id", -128)
                .or()
                .equalTo("from_id", to_id)
                .equalTo("to_id", from_id)
                .equalTo("chat_message_id", -128).findAll();
        for (MessageRealm messageRealm : all_128) {
            Message message = new Message();
            message.setFrom_id(messageRealm.getFrom_id());
            message.setTo_id(messageRealm.getTo_id());
            message.setChat_message_en(messageRealm.getChat_message_en());
            message.setChat_message_es(messageRealm.getChat_message_es());
            message.setChat_message_id(messageRealm.getChat_message_id());
            message.setLanguages_id(messageRealm.getLanguages_id());
            message.setCreated_at(messageRealm.getCreated_at());
            message.setRabbitmq_routing_key(messageRealm.getRabbitmq_routing_key());
            message.setRabbitmq_exchange_name(messageRealm.getRabbitmq_exchange_name());
            message.setRabbitmq_queue_name(messageRealm.getRabbitmq_queue_name());
            Log.d(TAG, "readMessages: message:" + message.toString());
            messages.add(message);
        }

        Log.d(TAG, "readMessages: messages.size(): " + messages.size());
        return messages;
    }

    /**
     * This finds the latest chat_message_id available in the realm database for chat
     */
    public long findLatestBetweenTwoUsers(int from_id, int to_id) {
        RealmResults<MessageRealm> allSorted = realm.where(MessageRealm.class)
                .equalTo("from_id", from_id)
                .equalTo("to_id", to_id)
                .notEqualTo("chat_message_id", -128)
                .or()
                .equalTo("from_id", to_id)
                .equalTo("to_id", from_id)
                .notEqualTo("chat_message_id", -128)
                .findAllSorted("chat_message_id", Sort.ASCENDING);
        if (allSorted != null) {
            if (allSorted.size() > 0) {
                MessageRealm messageRealm = allSorted.last();
                if (messageRealm != null) {
                    Log.d(TAG, "findLatestBetweenTwoUsers: first: Chat_message_id: " + messageRealm.getChat_message_id());
                    return messageRealm.getChat_message_id();
                }
            }
        }
        return 0;
    }

    /**
     * This would find the last chat_message_id available in the realm Database
     */
    public long findLastBetweenTowUsers(int from_id, int to_id) {
        RealmResults<MessageRealm> allSorted = realm.where(MessageRealm.class)
                .equalTo("from_id", from_id)
                .equalTo("to_id", to_id)
                .notEqualTo("chat_message_id", -128)
                .or()
                .equalTo("from_id", to_id)
                .equalTo("to_id", from_id)
                .notEqualTo("chat_message_id", -128)
                .findAllSorted("chat_message_id", Sort.ASCENDING);
        if (allSorted != null) {
            if (allSorted.size() > 0) {
                MessageRealm messageRealm = allSorted.first();
                if (messageRealm != null) {
                    Log.d(TAG, ": last:  Chat_message_id: " + messageRealm.getChat_message_id());
                    return messageRealm.getChat_message_id();
                }
            }
        }
        return 0;
    }

    public List<MessageRealm> readUnSentMessages() {
        List<MessageRealm> messageRealms = new ArrayList<>();
        RealmResults<MessageRealm> all = realm.where(MessageRealm.class)
                .equalTo("chat_message_id", -128)
                .findAll();
        return messageRealms;
    }

    public int messagesCount(int for_user_id) {
        RealmResults<MessageRealm> all = realm.where(MessageRealm.class)
                .equalTo("from_id", for_user_id)
                .notEqualTo("chat_message_id", -128)
                .or()
                .equalTo("to_id", for_user_id)
                .notEqualTo("chat_message_id", -128)
                .findAllSorted("chat_message_id", Sort.ASCENDING);
        return all.size();
    }

    public void writeToRealm(MessageRealm messageRealm) {
        realm.beginTransaction();
        realm.copyToRealm(messageRealm);
        realm.commitTransaction();
    }

    @Override
    public List<RealmObject> readFromRealmDatabase() {
        List<RealmObject> realmObjects = new ArrayList<>();
        RealmResults<MessageRealm> all = realm.where(MessageRealm.class).findAll();
        for (MessageRealm messageRealm : all) {
            realmObjects.add(messageRealm);
        }
        Log.d(TAG, "readFromRealmDatabase: all.size(): " + all.size());
        return realmObjects;
    }

    @Override
    public void saveToRealmDatabase(List<RealmObject> realmObjects) {
        Log.d(TAG, "saveToRealmDatabase: " + realmObjects.size());
        if (realmObjects != null) {
            for (RealmObject messageRealm : realmObjects) {
                if (doesExists(messageRealm)) {
                    updateItem(messageRealm);
                } else {
                    createItem(messageRealm);
                }
            }
        }
    }


    @Override
    public void createItem(RealmObject message) {
        MessageRealm messageRealm = (MessageRealm) message;
        Log.d(TAG, "createItem: chat_message_id: " + messageRealm.getChat_message_id());
        realm.beginTransaction();
        realm.copyToRealm(messageRealm);
        realm.commitTransaction();
    }

    @Override
    public void updateItem(RealmObject message) {
        MessageRealm messageRealm = (MessageRealm) message;
        Log.d(TAG, "updateItem: chat_message_id: " + messageRealm.getChat_message_id());
        realm.beginTransaction();
        MessageRealm item = realm.where(MessageRealm.class).equalTo("chat_message_id", messageRealm.getChat_message_id()).findFirst();
        if (item != null) {
            item.setId(messageRealm.getId());
            item.setFrom_id(messageRealm.getFrom_id());
            item.setTo_id(messageRealm.getTo_id());
            item.setChat_message_en(messageRealm.getChat_message_en());
            item.setChat_message_es(messageRealm.getChat_message_es());
            item.setChat_message_id(messageRealm.getChat_message_id());
            item.setLanguages_id(messageRealm.getLanguages_id());
            item.setRabbitmq_exchange_name(messageRealm.getRabbitmq_exchange_name());
            item.setRabbitmq_queue_name(messageRealm.getRabbitmq_queue_name());
            item.setRabbitmq_routing_key(messageRealm.getRabbitmq_routing_key());
        }
        realm.commitTransaction();
    }

    @Override
    public boolean doesExists(RealmObject message) {
        MessageRealm messageRealm = (MessageRealm) message;
        MessageRealm item = realm.where(MessageRealm.class).equalTo("chat_message_id", messageRealm.getChat_message_id()).findFirst();
        if (item == null) {
            Log.d(TAG, "doesExists: messageRealm.getChat_message_id(): " + messageRealm.getChat_message_id() + " exists?: " + false);
            return false;
        } else {
            Log.d(TAG, "doesExists: messageRealm.getChat_message_id(): " + messageRealm.getChat_message_id() + " exists?: " + true);
            return true;
        }
    }
}
