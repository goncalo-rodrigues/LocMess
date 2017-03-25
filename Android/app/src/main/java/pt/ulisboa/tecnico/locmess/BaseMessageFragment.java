package pt.ulisboa.tecnico.locmess;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;
import pt.ulisboa.tecnico.locmess.data.CustomCursorLoader;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.Message;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;

/**
 * Created by goncalo on 17-03-2017.
 */

public class BaseMessageFragment extends Fragment implements MessagesAdapter.Callback, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = BaseMessageFragment.class.getSimpleName();
    private MessagesAdapter messagesAdapter;
    private LinearLayoutManager mLayoutManager;
    private TextView emptyView;
    private RecyclerView mRecyclerView;
    private int mType;

    public static final String TYPE_ARG = "type";
    public static final int TYPE_ARG_RECEIVED = 0;
    public static final int TYPE_ARG_CREATED = 1;

    //Overriden method onCreateView

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_tab_new, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mType = getArguments().getInt(TYPE_ARG);

        if (messagesAdapter == null) {
                messagesAdapter = new MessagesAdapter(null, this, mType);
        }

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.main_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(messagesAdapter);

        emptyView = (TextView)  getView().findViewById(R.id.main_empty_tv);
        getLoaderManager().initLoader(0, null, this);
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

    @Override
    public void onRemoveClicked(int position, Message message) {
        if (getActivity() instanceof Callback) {
            ((Callback) getActivity()).onRemove(message);
        }
        message.delete(getContext());
        getLoaderManager().restartLoader(0, null, this);
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(getContext(), new CustomCursorLoader.Query() {
            @Override
            public Cursor query(Context context) {
                if (mType == TYPE_ARG_RECEIVED) {
                    return ReceivedMessage.getAll(context);
                } else {
                    return CreatedMessage.getAll(context);
                }
            }
        });
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        messagesAdapter.setData(data);
        messagesAdapter.notifyDataSetChanged();
        if (data.getCount() == 0)
            emptyView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        messagesAdapter.setData(null);
        messagesAdapter.notifyDataSetChanged();
    }

    public interface Callback {
        void onRemove(Message message);
    }
}
