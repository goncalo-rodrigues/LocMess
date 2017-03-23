package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;
import pt.ulisboa.tecnico.locmess.adapters.Pager;

public class MainActivity extends ActivityWithDrawer implements BaseMessageFragment.Callback, TabLayout.OnTabSelectedListener{

    private MessagesAdapter newMessagesAdapter;
    private MessagesAdapter createdMessagesAdapter;
    private List<MessagesAdapter.Message> newMessages = new ArrayList<>();
    private List<MessagesAdapter.Message> createdMessages = new ArrayList<>();
    private Pager adapter;
//    private LinearLayoutManager mLayoutManager;
//    private TextView emptyView;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

//        messages.add(new MessagesAdapter.Message("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd", "xyz", new Date(), "arco do cego"));
//        messages.add(new MessagesAdapter.Message("cba", "xyz", new Date(), "ist"));
//
//        messagesAdapter = new MessagesAdapter(messages, this);
//
//        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.main_list);
//
//        mLayoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//
//        mRecyclerView.setAdapter(messagesAdapter);
//
//        emptyView = (TextView) findViewById(R.id.main_empty_tv);

        tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.main_view_pager);

        newMessages.add(new MessagesAdapter.Message("Gonçalo, eu estou quase a conseguir as coordenadas GPS. So me falta um danoninho. Se conseguires ajudar, esta no activity_new_location! abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd", "Nuno Bebe", new Date(), "arco do cego"));
        createdMessages.add(new MessagesAdapter.Message("cba", "xyz", new Date(), "ist"));
        //Creating our pager adapter

        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount(), newMessages, createdMessages, this);

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fabMain);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage(v);
            }
        });



        super.onCreate(savedInstanceState);
    }

    protected void sendMessage(View v){
        Intent intent = new Intent(this, PostMessageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onRemove(MessagesAdapter.Message message) {

    }
}
