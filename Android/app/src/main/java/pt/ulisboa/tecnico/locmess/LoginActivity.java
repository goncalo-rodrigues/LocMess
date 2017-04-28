package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.serverrequests.LoginTask;
import pt.ulisboa.tecnico.locmess.serverrequests.LogoutTask;

import static pt.ulisboa.tecnico.locmess.R.layout.activity_login;

/**
 * Created by nca on 19-03-2017.
 */

public class LoginActivity extends AppCompatActivity implements LoginTask.LoginTaskCallBack ,View.OnClickListener, LogoutTask.LogoutCallBack {
    EditText usernameEt;
    EditText passwordEt;
    Button loginBt;
    TextView errorViewTv;
    ProgressBar waitingBallPb;
    Boolean loginResquested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(activity_login);

        usernameEt = (EditText) findViewById(R.id.username);
        passwordEt = (EditText) findViewById(R.id.password);
        loginBt = (Button) findViewById(R.id.button);
        loginBt.setOnClickListener(this);
        errorViewTv = (TextView) findViewById(R.id.error_text_view_login);
        errorViewTv.setText("");
        waitingBallPb = (ProgressBar) findViewById(R.id.waiting_ball);

        Utils.clearDatabase(this);

        super.onCreate(savedInstanceState);
    }


    @Override
    public void OnLoginComplete(String id) {
        waitingBallPb.setVisibility(View.GONE);
        loginBt.setVisibility(View.VISIBLE);
        loginResquested = false;
        //TODO some logic may be needed here
        //this is the method that will be caled when the reponse from the server is received
        Toast.makeText(this, "Loged in,server answer:"+id, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void OnWrongCredentials(String error){
        loginResquested = false;
        waitingBallPb.setVisibility(View.GONE);
        loginBt.setVisibility(View.VISIBLE);
        errorViewTv.setText(getString(R.string.wrong_credentials));
    }

    @Override
    public void OnNoInternetConnection(){
        loginResquested = false;
        waitingBallPb.setVisibility(View.GONE);
        loginBt.setVisibility(View.VISIBLE);
        errorViewTv.setText(getString(R.string.no_internet));
    }

    @Override
    public void onClick(View v) {
        //TODO use the real input
        switch (v.getId()) {
            case R.id.button:

                errorViewTv.setText("");

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
                loginResquested = true;
                loginBt.setVisibility(View.GONE);
                waitingBallPb.setVisibility(View.VISIBLE);

                NetworkGlobalState globalState = (NetworkGlobalState) this.getApplicationContext();
                if(globalState.getId()!=null && globalState.getUsername()==null)
                    new LogoutTask(this,this).execute();

                else
                    new LoginTask(this,this).execute(username,password);

                break;
            default:
                break;
        }
    }


    @Override
    public void logoutComplete() {
        String username =usernameEt.getText().toString();
        String password = passwordEt.getText().toString();
        new LoginTask(this,this).execute(username,password);
    }

    @Override
    public void logoutErrorResponse() {
        loginResquested = false;
        waitingBallPb.setVisibility(View.GONE);
        loginBt.setVisibility(View.VISIBLE);
        errorViewTv.setText(getString(R.string.no_internet));
    }

    @Override
    public void OnLogoutNoInternetConnection() {
        OnNoInternetConnection();
    }

}
