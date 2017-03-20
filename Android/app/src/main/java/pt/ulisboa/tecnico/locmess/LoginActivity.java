package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static pt.ulisboa.tecnico.locmess.R.layout.activity_login;

/**
 * Created by nca on 19-03-2017.
 */

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_login);
    }

    public void tryLogin(View v){
        // TODO: Introduce some logic to see if the user can log in

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
