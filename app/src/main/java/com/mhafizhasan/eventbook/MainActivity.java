package com.mhafizhasan.eventbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mhafizhasan.eventbook.net.CreateGuestRequest;
import com.mhafizhasan.eventbook.net.TokenUsingPasswordRequest;
import com.mhafizhasan.eventbook.net.model.UserModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import java.math.BigInteger;
import java.security.SecureRandom;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final CallChannel channel = new CallChannel();

    @Bind(R.id.cm_login_layout) LinearLayout loginLayout;
    @Bind(R.id.cm_register_layout) LinearLayout registerLayout;
    @Bind(R.id.cm_login_username) EditText usernameView;
    @Bind(R.id.cm_login_password) EditText passwordView;
    @Bind(R.id.cm_firstname) EditText firstnameView;
    @Bind(R.id.cm_lastname) EditText lastnameView;
    @Bind(R.id.cm_firstname_layout) TextInputLayout firstnameLayout;
    @Bind(R.id.cm_lastname_layout) TextInputLayout lastnameLayout;
    @Bind(R.id.cm_username_layout) TextInputLayout usernameLayout;
    @Bind(R.id.cm_password_layout) TextInputLayout passwordLayout;
    @Bind(R.id.cm_form_layout) LinearLayout formLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup layout and bind views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // If login credentials found, auto login
        UserLoginDetails login = new UserLoginDetails().from(this);
        if(login.password != null) {
            formLayout.setVisibility(View.GONE);       // auto login, no need to show form
            new TokenUsingPasswordRequest(channel, loginLayout, login.me.email, login.password) {
                @Override
                protected void onGrantedAccessToken(String access_token) {
                    startContentActivity();
                }
            }.send();
        }
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
        final String firstname = getInput(firstnameView, firstnameLayout);
        final String lastname = getInput(lastnameView, lastnameLayout);

        if(firstname == null || lastname == null)
            return;         // form incomplete

        // Generate  random password, 128-bit UUID
        byte[] random = new byte[16];       // 16 x 8 = 128 bits
        SecureRandom randomizer = new SecureRandom();
        randomizer.nextBytes(random);
        final String password = new BigInteger(1, random).toString(16);
        // Send create guest request
        new CreateGuestRequest(channel, loginLayout, firstname, lastname, password) {
            @Override
            protected void onSuccess(UserModel response) {
                // Successfully created, user save to permanent storage
                UserLoginDetails login = UserLoginDetails.from(MainActivity.this);
                login.me = response;
                login.password = password;
                login.save(MainActivity.this);
                // Login using this credentials
                new TokenUsingPasswordRequest(channel, loginLayout, response.email, password) {
                    @Override
                    protected void onGrantedAccessToken(String access_token) {
                        startContentActivity();

                    }
                }.send();

            }
        }.send();

    }

    @OnClick(R.id.cm_login_button)
    void OnClickLogin() {
        final String username = getInput(usernameView, usernameLayout);
        final String password = getInput(passwordView, passwordLayout);

        if(username == null || password == null)
            return;         // form incomplete
        // Start login
        new TokenUsingPasswordRequest(channel, loginLayout, username, password) {
            @Override
            public void onGrantedAccessToken(String access_token) {
                startContentActivity();
            }

            @Override
            protected void onError(int httpCode, String httpMessage, String errorType, String errorMessage) {
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

    void startContentActivity() {
        // Logged in and saved token to storage and proceed to show content
        Intent i = new Intent(this, ContentActivity.class);
        startActivity(i);
        finish();
    }

}
