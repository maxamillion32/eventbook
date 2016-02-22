package com.mhafizhasan.eventbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mhafizhasan.eventbook.net.GaeRequest;
import com.mhafizhasan.eventbook.net.GaeServer;
import com.mhafizhasan.eventbook.net.model.TokenModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import java.io.IOException;
import java.util.logging.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final CallChannel channel = new CallChannel();

    @Bind(R.id.cm_login_layout) LinearLayout loginLayout;
    @Bind(R.id.cm_register_layout) LinearLayout registerLayout;
    @Bind(R.id.cm_login_username) EditText usernameView;
    @Bind(R.id.cm_login_password) EditText passwordView;
    @Bind(R.id.cm_username_layout) TextInputLayout usernameLayout;
    @Bind(R.id.cm_password_layout) TextInputLayout passwordLayout;
    @Bind(R.id.cm_form_layout) LinearLayout formLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup layout and bind views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        channel.open(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        channel.close(this);
    }

    @OnClick(R.id.cm_show_login_link)
    void onClickShowLogin() {
        registerLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.cm_show_register_link)
    void onClickShowRegister() {
        loginLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.VISIBLE);
    }

    String getInput(EditText textView, TextInputLayout layout) {
        String input = textView.getEditableText().toString();
        if(input.isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError("Required");            // Error message to display underneath
            return null;

        } else {
            layout.setErrorEnabled(false);
            return  input;
        }
    }


    @OnClick(R.id.cm_register_button)
    void onClickRegister() {

    }

    @OnClick(R.id.cm_login_button)
    void OnClickLogin() {
        final String username = getInput(usernameView, usernameLayout);
        final String password = getInput(passwordView, passwordLayout);

        if(username == null || password == null)
            return;         // form incomplete

        // TODO: authenticate user and go to content page
        new GaeRequest<TokenModel>(channel, loginLayout) {

            @Override
            protected Call<TokenModel> getCall() {
                return GaeServer.api.getTokenUsingPassword(
                        "password",
                        username,
                        password,
                        GaeServer.CLIENT_ID,
                        GaeServer.CLIENT_SECRET,
                        GaeServer.DEFAULT_SCOPE
                );
            }

            @Override
            protected void onSucces(TokenModel response) {
                Toast.makeText(MainActivity.this, "Logged in: " + response.access_token, Toast.LENGTH_LONG).show();
                // TODO: Go to next activity
                Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            protected void onError(int httpCode, String httpMessage, String errorType, String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                // Show back the form
                formLayout.setVisibility(View.VISIBLE);
                // Show invalid
                usernameLayout.setError("Invalid Username");
                usernameLayout.setErrorEnabled(true);
                passwordLayout.setError("Invalid Password");
                passwordLayout.setErrorEnabled(true);
            }
        }.send();

        // Hide the form
        formLayout.setVisibility(View.GONE);




    }

}
