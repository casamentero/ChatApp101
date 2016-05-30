package message.activity.source;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import gson.source.model.Message;
import realm.source.model.CurrentUserRealm;
import source.app.chat.chatapp.R;
import support.source.classes.StartUp;

/**
 * Created by Pankaj Nimgade on 25-05-2016.
 */
public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.ViewHolder> {

    private static final String TAG = MessageRecyclerAdapter.class.getSimpleName();

    private Context context;
    private List<Message> messages;
    private LayoutInflater layoutInflater;
    private CurrentUserRealm currentUserRealm;

    public MessageRecyclerAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.layoutInflater = LayoutInflater.from(this.context);
        currentUserRealm = StartUp.getCurrentUserRealm();
        Log.d(TAG, "MessageRecyclerAdapter: messages.size(): "+messages.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.single_item_message, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messages.get(position);
        Log.d(TAG, "onBindViewHolder: message: "+message.getChat_message()+", position: "+position);
        if (message.getFrom_id() == currentUserRealm.getId()) {
            holder.right_callout_LinearLayout.setVisibility(View.VISIBLE);
            holder.left_callout_LinearLayout.setVisibility(View.GONE);
            holder.right_callout_TextView.setText("" + message.getChat_message());
        } else {
            holder.right_callout_LinearLayout.setVisibility(View.GONE);
            holder.left_callout_LinearLayout.setVisibility(View.VISIBLE);
            holder.left_callout_TextView.setText("" + message.getChat_message());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout left_callout_LinearLayout;
        private LinearLayout right_callout_LinearLayout;
        private TextView left_callout_TextView;
        private TextView right_callout_TextView;

        public ViewHolder(View itemView) {
            super(itemView);
            left_callout_LinearLayout = (LinearLayout) itemView.findViewById(R.id.single_item_message_left_linearLayout);
            right_callout_LinearLayout = (LinearLayout) itemView.findViewById(R.id.single_item_message_right_linearLayout);
            left_callout_TextView = (TextView) itemView.findViewById(R.id.call_out_left_message_textView);
            right_callout_TextView = (TextView) itemView.findViewById(R.id.call_out_right_message_textView);
        }
    }
}
