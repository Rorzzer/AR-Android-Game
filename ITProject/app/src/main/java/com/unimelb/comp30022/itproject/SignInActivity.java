package com.unimelb.comp30022.itproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity
    implements View.OnClickListener{
    private final String TAG = "FB_SIGNIN";

    // TODO: Add Auth members
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private EditText etPass;
    private EditText etEmail;

    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Set up click handlers and view item references
        findViewById(R.id.btnCreate).setOnClickListener(this);
        findViewById(R.id.btnSignInReg).setOnClickListener(this);
        findViewById(R.id.btnSignOut).setOnClickListener(this);

        etEmail = (EditText)findViewById(R.id.etEmailAddr);
        etPass = (EditText)findViewById(R.id.etPassword);

        // TODO: Get a reference to the Firebase auth object
    mAuth = FirebaseAuth.getInstance();

        // TODO: Attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Signed in: " + user.getUid());
                }
                else{
                    Log.d(TAG, "Currently Signed Out");
                }
            }
        };

        updateStatus();
    }

    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        // TODO: add the AuthListener
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        // TODO: Remove the AuthListener
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignInReg:
                signUserIn();
                break;

            case R.id.btnCreate:
                createUserAccount();
                break;

            case R.id.btnSignOut:
                signUserOut();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                break;
        }
    }

    private boolean checkFormFields() {
        String email, password;

        email = etEmail.getText().toString();
        password = etPass.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()){
            etPass.setError("Password Required");
            return false;
        }

        return true;
    }

    private void updateStatus() {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        // TODO: get the current user
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            tvStat.setText("Signed in: " + user.getEmail());
        }
        else {
            tvStat.setText("Signed Out");
        }

    }

    private void updateStatus(String stat) {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        tvStat.setText(stat);
    }

    private void signUserIn() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // TODO: sign the user in with email and password credentials
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new
                        OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult>
                                                   task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SignInActivity.this, "Signed in",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(SignInActivity.this, "Sign in failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        updateStatus();
                    }
                });

    }

    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        updateStatus();
    }

    private void createUserAccount() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // TODO: Create the user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new
                        OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult>
                                                   task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SignInActivity.this, "User was created",
                                Toast.LENGTH_SHORT).show();
                        }
                        else{
                        Toast.makeText(SignInActivity.this, "Account creation failed",
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
