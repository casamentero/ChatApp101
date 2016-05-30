package user.list.activities.source;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import gson.source.model.User;
import source.app.chat.chatapp.BR;
import source.app.chat.chatapp.R;


/**
 * Created by Pankaj Nimgade on 24-05-2016.
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.BindingHolder> {

    private Context context;
    private List<User> users;
    private LayoutInflater layoutInflater;

    public UserRecyclerAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.single_item_user, parent, false);
        BindingHolder bindingHolder = new BindingHolder(view);
        return bindingHolder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        final User user = users.get(position);
        holder.getBinding().setVariable(BR.user, user);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class BindingHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public BindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }
}
