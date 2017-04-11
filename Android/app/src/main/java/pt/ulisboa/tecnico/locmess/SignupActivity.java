package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.serverrequests.LogoutTask;
import pt.ulisboa.tecnico.locmess.serverrequests.RegisterTask;

import static pt.ulisboa.tecnico.locmess.R.layout.activity_signup;

/**
 * Created by nca on 19-03-2017.
 */

public class SignupActivity extends AppCompatActivity implements RegisterTask.RegisteTaskCallBack ,View.OnClickListener, LogoutTask.LogoutCallBack {
    EditText usernameEt;
    EditText passwordEt;
    EditText retryEt;
    Button registerBt;
    TextView errorViewTv;
    ProgressBar waitingBallPb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(activity_signup);

        usernameEt = (EditText) findViewById(R.id.username);
        passwordEt = (EditText) findViewById(R.id.password);
        retryEt = (EditText) findViewById(R.id.retry_password);
        registerBt = (Button) findViewById(R.id.button);
        registerBt.setOnClickListener(this);
        errorViewTv = (TextView) findViewById(R.id.error_text_view);
        errorViewTv.setText("");
        waitingBallPb = (ProgressBar) findViewById(R.id.waiting_ball);

        super.onCreate(savedInstanceState);
    }


    @Override
    public void OnRegisterComplete(String id) {
        //TODO some logic may be needed here
        waitingBallPb.setVisibility(View.GONE);
        registerBt.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Registed ,server answer:"+id, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public void OnUserAlreadyExists(String error){
        waitingBallPb.setVisibility(View.GONE);
        registerBt.setVisibility(View.VISIBLE);
        errorViewTv.setText(getString(R.string.user_already_exists));
    }

    @Override
    public void OnNoInternetConnection(){
        waitingBallPb.setVisibility(View.GONE);
        registerBt.setVisibility(View.VISIBLE);
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

                String repeat = retryEt.getText().toString();
                if(!repeat.equals(password)){
                    errorViewTv.setText(getString(R.string.error_pass_not_equal));
                    passwordEt.setText("");
                    retryEt.setText("");
                    break;
                }

                registerBt.setVisibility(View.GONE);
                waitingBallPb.setVisibility(View.VISIBLE);

                NetworkGlobalState globalState = (NetworkGlobalState) this.getApplicationContext();
                if(globalState.getId()!=null && globalState.getUsername()==null)
                    new LogoutTask(this,this).execute();

                else
                    new RegisterTask(this,this).execute(username,password);
                break;
            default:
                break;
        }
    }

    @Override
    public void logoutComplete() {
        String username =usernameEt.getText().toString();
        String password = passwordEt.getText().toString();
        new RegisterTask(this,this).execute(username,password);
    }

    @Override
    public void logoutErrorResponse() {
        waitingBallPb.setVisibility(View.GONE);
        registerBt.setVisibility(View.VISIBLE);
        errorViewTv.setText(getString(R.string.no_internet));
    }

    @Override
    public void OnLogoutNoInternetConnection() {
        OnNoInternetConnection();
    }

}