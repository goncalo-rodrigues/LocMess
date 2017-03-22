package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.view.View;
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

        Toast toast = Toast.makeText(this, "GPS", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void wifiClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        Toast toast = Toast.makeText(this, "Wifi", Toast.LENGTH_SHORT);
        toast.show();
    }
}
