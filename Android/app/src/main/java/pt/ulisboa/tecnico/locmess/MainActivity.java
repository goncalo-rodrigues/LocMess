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
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.Message;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;

public class MainActivity extends ActivityWithDrawer implements BaseMessageFragment.Callback, TabLayout.OnTabSelectedListener{

    private Pager adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.main_view_pager);

        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount(), this);

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        // Onclick for FAB
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fabMain);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        super.onCreate(savedInstanceState);
    }

    protected void sendMessage(View v){
        ReceivedMessage m = new ReceivedMessage("1", "text", "author", "loc", new Date(), new Date());
        m.save(this);
        CreatedMessage m2 = new CreatedMessage("2", "text", "author", "loc", new Date(), new Date());
        m2.save(this);
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
    public void onRemove(Message message) {

    }
}
