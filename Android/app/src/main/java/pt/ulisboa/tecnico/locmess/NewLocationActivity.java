package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by nca on 20-03-2017.
 */

public class NewLocationActivity extends ActivityWithDrawer {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_location);
        super.onCreate(savedInstanceState);
    }
}
