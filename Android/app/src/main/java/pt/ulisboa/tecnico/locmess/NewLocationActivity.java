package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by nca on 20-03-2017.
 */

public class NewLocationActivity extends ActivityWithDrawer {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_location);
        super.onCreate(savedInstanceState);
    }

    public void gpsClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        LinearLayout l = (LinearLayout) findViewById(R.id.chose_gps);
        l.setVisibility(View.VISIBLE);

        l = (LinearLayout) findViewById(R.id.chose_wifi);
        l.setVisibility(View.GONE);
    }

    public void wifiClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        LinearLayout l = (LinearLayout) findViewById(R.id.chose_gps);
        l.setVisibility(View.GONE);

        l = (LinearLayout) findViewById(R.id.chose_wifi);
        l.setVisibility(View.VISIBLE);
    }
}
