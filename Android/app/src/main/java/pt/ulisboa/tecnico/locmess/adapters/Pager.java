package pt.ulisboa.tecnico.locmess.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import pt.ulisboa.tecnico.locmess.R;
import java.util.List;

import pt.ulisboa.tecnico.locmess.BaseMessageFragment;
import pt.ulisboa.tecnico.locmess.MainActivity;

/**
 * Created by goncalo on 17-03-2017.
 */

public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;
    private BaseMessageFragment tab1;
    private BaseMessageFragment tab2;

    private List<MessagesAdapter.Message> l1;
    private List<MessagesAdapter.Message> l2;


    private String[] tabTitles;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount, List<MessagesAdapter.Message> newMsgs,
                 List<MessagesAdapter.Message> createdMsgs, Context context) {
        super(fm);
        //Initializing tab count
        this.tabCount = tabCount;
        l1 = newMsgs;
        l2 = createdMsgs;
        tabTitles = context.getResources().getStringArray(R.array.tab_names);
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                tab1 = new BaseMessageFragment();
                tab1.setMessages(l1);
                return tab1;
            case 1:
                tab2 = new BaseMessageFragment();
                tab2.setMessages(l2);
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position < tabTitles.length)
            return tabTitles[position];
        else
            return "";
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }

    public BaseMessageFragment getNewMessagesTab() {
        return tab1;
    }
    public BaseMessageFragment getSavedMessagesTab() {
        return tab2;
    }
}