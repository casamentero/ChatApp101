package gson.source.model;

/**
 * Created by Pankaj Nimgade on 25-05-2016.
 */
public class Message {

    private long id;
    private long from_id;
    private long to_id;
    private String chat_message_en;
    private String chat_message_es;
    private long chat_message_id = -128;
    private int languages_id;
    private long created_at;
    private long updated_at;
    private int is_read;
    private String rabbitmq_exchange_name;
    private String rabbitmq_queue_name;
    private String rabbitmq_routing_key;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFrom_id() {
        return from_id;
    }

    public void setFrom_id(long from_id) {
        this.from_id = from_id;
    }

    public long getTo_id() {
        return to_id;
    }

    public void setTo_id(long to_id) {
        this.to_id = to_id;
    }

    public String getChat_message_en() {
        return chat_message_en;
    }

    public void setChat_message_en(String chat_message_en) {
        this.chat_message_en = chat_message_en;
    }

    public String getChat_message_es() {
        return chat_message_es;
    }

    public void setChat_message_es(String chat_message_es) {
        this.chat_message_es = chat_message_es;
    }

    public long getChat_message_id() {
        return chat_message_id;
    }

    public void setChat_message_id(long chat_message_id) {
        this.chat_message_id = chat_message_id;
    }

    public int getLanguages_id() {
        return languages_id;
    }

    public void setLanguages_id(int languages_id) {
        this.languages_id = languages_id;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public int getIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
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

    @Override
    public String toString() {
        return "Message: "+" created_at: "+created_at+" from_id: "+from_id+", to_id: "+to_id+ ", chat_message_en: "+ chat_message_en+ ", chat_message_id: "+ chat_message_id;
    }
}
