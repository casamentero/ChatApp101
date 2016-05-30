package realm.source.model;

import io.realm.RealmObject;

/**
 * Created by Pankaj Nimgade on 21-05-2016.
 */
public class UserRealm extends RealmObject{

    private int id;
    private String username;
    private String email;
    private String password_hash;
    private String auth_key;
    private String confirmed_at;
    private String unconfirmed_email;
    private String blocked_at;
    private String registration_ip;
    private int created_at;
    private int updated_at;
    private int flags;
    private String rabbitmq_exchange_name;
    private String rabbitmq_queue_name;
    private String rabbitmq_routing_key;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getAuth_key() {
        return auth_key;
    }

    public void setAuth_key(String auth_key) {
        this.auth_key = auth_key;
    }

    public String getConfirmed_at() {
        return confirmed_at;
    }

    public void setConfirmed_at(String confirmed_at) {
        this.confirmed_at = confirmed_at;
    }

    public String getUnconfirmed_email() {
        return unconfirmed_email;
    }

    public void setUnconfirmed_email(String unconfirmed_email) {
        this.unconfirmed_email = unconfirmed_email;
    }

    public String getBlocked_at() {
        return blocked_at;
    }

    public void setBlocked_at(String blocked_at) {
        this.blocked_at = blocked_at;
    }

    public String getRegistration_ip() {
        return registration_ip;
    }

    public void setRegistration_ip(String registration_ip) {
        this.registration_ip = registration_ip;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String getRabbitmq_exchange_name() {
        return rabbitmq_exchange_name;
    }

    public void setRabbitmq_exchange_name(String rabbitmq_exchange_name) {
        this.rabbitmq_exchange_name = rabbitmq_exchange_name;
    }

    public String getRabbitmq_queue_name() {
        return rabbitmq_queue_name;
    }

    public void setRabbitmq_queue_name(String rabbitmq_queue_name) {
        this.rabbitmq_queue_name = rabbitmq_queue_name;
    }

    public String getRabbitmq_routing_key() {
        return rabbitmq_routing_key;
    }

    public void setRabbitmq_routing_key(String rabbitmq_routing_key) {
        this.rabbitmq_routing_key = rabbitmq_routing_key;
    }
}
