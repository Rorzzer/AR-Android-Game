package com.unimelb.comp30022.itproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by RoryPowell.
 * This class manages signing in and out of firebase and account creation
 * it also handles validation of details
 */

public class SignInActivity extends AppCompatActivity
    implements View.OnClickListener{

    private final String TAG = SignInActivity.class.getName();
    private final String EMPTY = "Empty";
    private final String SIGN_OUT = "Signed Out";


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    private FirebaseUser user = null;

    private EditText etPass;
    private EditText etEmail;
    private Button btnSignIn;
    private Button btnSignOut;
    private Button btnCreateAcc;

    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Context context = getApplicationContext();

        // Set up click handlers and view item references
          btnSignIn = (Button)findViewById(R.id.btnSignInReg);
          btnSignOut = (Button)findViewById(R.id.btnSignOut);
          btnCreateAcc =  (Button)findViewById(R.id.btnCreate);
        findViewById(R.id.btnCreate).setOnClickListener(this);
        findViewById(R.id.btnSignInReg).setOnClickListener(this);
        findViewById(R.id.btnSignOut).setOnClickListener(this);
        btnSignOut.setVisibility(View.GONE);
        btnSignIn.setVisibility(View.GONE);
        btnCreateAcc.setVisibility(View.GONE);

        etEmail = (EditText) findViewById(R.id.etEmailAddr);
        etPass = (EditText) findViewById(R.id.etPassword);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("users");


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
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
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Button click logic
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignInReg:
                Log.d(TAG, "Sign in button pressed");
                signUserIn();
                break;

            case R.id.btnCreate:
                Log.d(TAG, "Create button pressed");
                createUserAccount();
                break;

            case R.id.btnSignOut:
                Log.d(TAG, "Sign Out button pressed");

                signUserOut();

                Toast.makeText(SignInActivity.this,R.string.user_successfuly_signed_out,Toast.LENGTH_SHORT).show();

                //return to the main screen after signing out
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);

                break;
        }
    }

    /**
     * check all text entries are valid
     */
    private boolean checkFormFields() {
        String email, password;

        email = etEmail.getText().toString();
        password = etPass.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            Log.d(TAG, "Email is empty");

            return false;
        }
        if (password.isEmpty()){
            Log.d(TAG, "Password is empty");

            etPass.setError("Password Required");
            return false;
        }
        return true;
    }

    /**
     * Update the text on screen with the error
     */
    private void updateStatus() {
        TextView tvStat = findViewById(R.id.tvSignInStatus);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            tvStat.setText("Signed in: " + user.getEmail());
            showSignedInInterface();
        }
        else {
            showSignedOutInterface();
            tvStat.setText(SIGN_OUT);
        }
    }

    /**
     * Update the text on screen with the error message passed in
     */
    private void updateStatus(String stat) {
        TextView tvStat = findViewById(R.id.tvSignInStatus);
        tvStat.setText(stat);
    }

    /**
     * Sign the user into the firebase auth
     */
    private void signUserIn() {
        if (!checkFormFields()){
            Log.d(TAG, "Form Fields false");
            return;
        }

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new
                        OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult>
                                                   task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SignInActivity.this, R.string.user_successfuly_signed_in,
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);

                        }
                        else {
                            Toast.makeText(SignInActivity.this, R.string.user_unsuccessfuly_signed_in,
                                    Toast.LENGTH_SHORT).show();
                        }
                        updateStatus();
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException){
                    updateStatus("Invalid Password.");
                    Log.d(TAG, "Invalid password");

                } else if (e instanceof FirebaseAuthInvalidUserException){
                    updateStatus("No account with this email.");
                    Log.d(TAG, "No account with this email");

                } else {
                    updateStatus(e.getLocalizedMessage());
                }
            }
        });

    }

    /**
     * Sign the user out of firebase
     */
    private void signUserOut() {
        mAuth.signOut();
        updateStatus();
    }

    /**
     * Create a user in firebase
     */
    private void createUserAccount() {
        if (!checkFormFields()) {
            return;
        }
        //get the email and password strings
        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // validate the email address
        if (isEmailValid(email)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new
                            OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult>
                                                               task) {
                                    if (task.isSuccessful()){

                                        //write blank user data placeholder in the database
                                        User newUser = new User(EMPTY, EMPTY, EMPTY, user.getEmail());
                                        mDatabase.child(user.getUid()).setValue(newUser);

                                        Toast.makeText(SignInActivity.this, R.string.user_successfully_created,
                                                Toast.LENGTH_SHORT).show();
                                        updateStatus("User created, now login then create a user profile.");
                                    }else{
                                        Toast.makeText(SignInActivity.this, R.string.account_creation_failed,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof FirebaseAuthUserCollisionException){
                                updateStatus("Email already in use");
                            } else {
                                updateStatus(e.getLocalizedMessage());
                            }
                        }
                    });
        } else {
            updateStatus("Must be a valid University of Melbourne email address");
            Toast.makeText(SignInActivity.this, R.string.account_creation_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if the user has a university of melbourne email address and is a valid email
     */
    private boolean isEmailValid(String email) {
        return (email.contains("@") && email.endsWith("unimelb.edu.au"));
    }
    private void showSignedInInterface(){
        btnSignOut.setVisibility(View.VISIBLE);
        btnSignIn.setVisibility(View.GONE);
        btnCreateAcc.setVisibility(View.GONE);
    }
    private void showSignedOutInterface(){
        btnSignOut.setVisibility(View.GONE);
        btnSignIn.setVisibility(View.VISIBLE);
        btnCreateAcc.setVisibility(View.VISIBLE);
    }

}
