package com.unimelb.comp30022.itproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener {

    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Set up click handlers and view item references
        findViewById(R.id.btnSignInReg).setOnClickListener(this);
        findViewById(R.id.btnUser).setOnClickListener(this);
    }

    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        // TODO: add the AuthListener
        // mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        /*
        // TODO: Remove the AuthListener
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
        */
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignInReg:
                Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(i);
                break;

            case R.id.btnUser:

                break;

        }
    }
}

