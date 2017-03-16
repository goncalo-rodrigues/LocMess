package pt.ulisboa.tecnico.locmess;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;

public class MainActivity extends ActivityWithDrawer{

    private MessagesAdapter messagesAdapter;
    private List<MessagesAdapter.Message> messages = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        messages.add(new MessagesAdapter.Message("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd", "xyz", new Date(), "arco do cego"));
        messages.add(new MessagesAdapter.Message("cba", "xyz", new Date(), "ist"));

        messagesAdapter = new MessagesAdapter(messages);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.main_list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(messagesAdapter);

        super.onCreate(savedInstanceState);
    }
}
