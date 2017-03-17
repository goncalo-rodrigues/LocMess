package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;

/**
 * Created by goncalo on 17-03-2017.
 */

public class BaseMessageFragment extends Fragment implements MessagesAdapter.Callback{
    private MessagesAdapter messagesAdapter;
    private List<MessagesAdapter.Message> messages = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private TextView emptyView;
    private RecyclerView mRecyclerView;
    //Overriden method onCreateView

    public BaseMessageFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        return inflater.inflate(R.layout.activity_main_tab_new, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (messagesAdapter == null) {
            messagesAdapter = new MessagesAdapter(messages, this);
        }

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.main_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(messagesAdapter);

        emptyView = (TextView)  getView().findViewById(R.id.main_empty_tv);
        super.onActivityCreated(savedInstanceState);
    }

    public MessagesAdapter getMessagesAdapter() {
        return messagesAdapter;
    }

    public void setMessagesAdapter(MessagesAdapter messagesAdapter) {
        this.messagesAdapter = messagesAdapter;
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(messagesAdapter);
            messagesAdapter.notifyDataSetChanged();
        }

    }

    public List<MessagesAdapter.Message> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagesAdapter.Message> messages) {
        this.messages = messages;
        if (messagesAdapter != null) {
            messagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRemoveClicked(int position) {
        if (getActivity() instanceof Callback) {
            ((Callback) getActivity()).onRemove(messages.get(position));
        }
        messages.remove(position);
        messagesAdapter.notifyDataSetChanged();
        if (messages.size() == 0)
            emptyView.setVisibility(View.VISIBLE);
    }

    public static interface Callback {
        void onRemove(MessagesAdapter.Message message);
    }
}
