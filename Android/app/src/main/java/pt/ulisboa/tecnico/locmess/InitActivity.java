package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static pt.ulisboa.tecnico.locmess.R.layout.activity_init;

/**
 * Created by nca on 19-03-2017.
 */

public class InitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSharedPreferences(Utils.PREFS_NAME, 0).getString(Utils.SESSION, null) != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        setContentView(activity_init);
    }


    public void login(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signup(View v) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}