package com.mhafizhasan.eventbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.cm_login_layout) LinearLayout loginLayout;
    @Bind(R.id.cm_register_layout) LinearLayout registerLayout;
    @Bind(R.id.cm_login_username) EditText usernameView;
    @Bind(R.id.cm_login_password) EditText passwordView;
    @Bind(R.id.cm_username_layout) TextInputLayout usernameLayout;
    @Bind(R.id.cm_password_layout) TextInputLayout passwordLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup layout and bind views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
        String username = getInput(usernameView, usernameLayout);
        String password = getInput(passwordView, passwordLayout);

        if(username == null || password == null)
            return;         // form incomplete

        // TODO: authenticate user and go to content page
        Intent intent = new Intent(this, ContentActivity.class);
        startActivity(intent);
        finish();

    }

}
