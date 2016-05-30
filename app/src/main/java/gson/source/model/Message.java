package gson.source.model;

/**
 * Created by Pankaj Nimgade on 25-05-2016.
 */
public class Message {

    private int from_id;
    private int to_id;
    private String chat_message;
    private String chat_message_id = "-128";
    private int languages_id;
    private int created_at;
    private String rabbitmq_exchange_name;
    private String rabbitmq_queue_name;
    private String rabbitmq_routing_key;

    public int getFrom_id() {
        return from_id;
    }

    public void setFrom_id(int from_id) {
        this.from_id = from_id;
    }

    public int getTo_id() {
        return to_id;
    }

    public void setTo_id(int to_id) {
        this.to_id = to_id;
    }

    public String getChat_message() {
        return chat_message;
    }

    public void setChat_message(String chat_message) {
        this.chat_message = chat_message;
    }

    public String getChat_message_id() {
        return chat_message_id;
    }

    public void setChat_message_id(String chat_message_id) {
        this.chat_message_id = chat_message_id;
    }

    public int getLanguages_id() {
        return languages_id;
    }

    public void setLanguages_id(int languages_id) {
        this.languages_id = languages_id;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
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
        return "Message: "+" from_id: "+from_id+", to_id: "+to_id+ ", chat_message: "+ chat_message+ ", chat_message_id: "+ chat_message_id;
    }
}
