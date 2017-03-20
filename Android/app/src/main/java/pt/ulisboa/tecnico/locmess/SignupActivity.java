package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static pt.ulisboa.tecnico.locmess.R.layout.activity_signup;

/**
 * Created by nca on 19-03-2017.
 */

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_signup);
    }

    public void trySignup(View v){
        // TODO: Introduce some logic to see if the user can sign up

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}