package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.locmess.serverrequests.LoginTask;

import static pt.ulisboa.tecnico.locmess.R.layout.activity_login;

/**
 * Created by nca on 19-03-2017.
 */

public class LoginActivity extends AppCompatActivity implements LoginTask.LoginTaskCallBack ,View.OnClickListener{
    EditText usernameEt;
    EditText passwordEt;
    Button loginBt;
    TextView errorViewTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(activity_login);

        usernameEt = (EditText) findViewById(R.id.username);
        passwordEt = (EditText) findViewById(R.id.password);
        loginBt = (Button) findViewById(R.id.button);
        loginBt.setOnClickListener(this);
        errorViewTv = (TextView) findViewById(R.id.error_text_view_login);
        errorViewTv.setText("");



        super.onCreate(savedInstanceState);

    }


    @Override
    public void OnLoginComplete(String id) {
        //TODO some logic may be needed here
        //this is the method that will be caled when the reponse from the server is received
        Toast.makeText(this, "Loged in,server answer:"+id, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void OnWrongCredentials(String error){
        errorViewTv.setText(getString(R.string.wrong_credentials));
    }

    @Override
    public void OnNoInternetConnection(String error){
        errorViewTv.setText(getString(R.string.no_internet));
    }

    @Override
    public void onClick(View v) {
        //TODO use the real input
        switch (v.getId()) {
            case R.id.button:


                String username =usernameEt.getText().toString();
                if(username==null || username.length()==0){
                    errorViewTv.setText(getString(R.string.empty_user));
                    break;
                }

                String password = passwordEt.getText().toString();
                if(password==null || password.length()==0){
                    errorViewTv.setText(getString(R.string.empty_pass));
                    break;
                }
                Toast.makeText(this, "Waiting for server", Toast.LENGTH_SHORT).show();
                new LoginTask(this).execute(username,password);
                break;
            default:
                break;
        }
    }


}
