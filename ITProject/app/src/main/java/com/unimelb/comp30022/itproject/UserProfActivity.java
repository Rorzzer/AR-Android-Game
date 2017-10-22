package com.unimelb.comp30022.itproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by RoryPowell.
 * This class manages the users profile, users create a username
 * and fill their name and preferred email address in.
 * this information is placed in the database as a User.class object
 */

public class UserProfActivity extends AppCompatActivity
        implements View.OnClickListener{

    private final String TAG = UserProfActivity.class.getName();
    private final String EMPTYFIRST = "Enter First name";
    private final String EMPTYLAST = "Enter Last name";
    private final String EMPTYUSERNAME = "Enter Username";
    private final String EMPTYEMAIL = "Enter Email";
    private final String EMPTY = "";
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String uID;

    private EditText etUsername;
    private EditText etFirstname;
    private EditText etLastname;
    private EditText etEmail;

    private User userInfo = null;
    private FirebaseUser fUser;

    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_user_prof);
        findViewById(R.id.btnEnter).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        etFirstname = (EditText)findViewById(R.id.etFirst);
        etLastname = (EditText)findViewById(R.id.etLast);
        etUsername = (EditText)findViewById(R.id.etUsername);
        etEmail = (EditText)findViewById(R.id.etEmail);
        hideUserPage();

        //get a reference to the database
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("users");

        updateFirebaseUser(mAuth);

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // update the fUser Firebase auth
                updateFirebaseUser(firebaseAuth);
            }
        };

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //private DataSnapshot dataSnapshot;
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    Log.d(TAG, "Datasnapshot exists");

                    if (fUser != null) {
                        // load the userInfo if it exists
                        userInfo = dataSnapshot.child(uID).getValue(User.class);

                    } else {
                        Log.d(TAG, "fUser is null");
                        updateFirebaseUser(mAuth);
                    }


                    if(userInfo != null) {
                        loadUserData();

                    } else {
                        userInfo = new User(EMPTYUSERNAME, EMPTYFIRST, EMPTYLAST, EMPTYEMAIL);
                    }
                    showUserPage();
                    Log.d(TAG, "Value is: " + uID);
                } else {
                    Log.d(TAG, "datasnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Update the firebase user information
     */
    private void updateFirebaseUser(FirebaseAuth firebaseAuth) {
        fUser = firebaseAuth.getCurrentUser();
        if (fUser != null) {
            uID = fUser.getUid();
            Log.d(TAG, "Signed in: " + fUser.getUid());
        } else {
            Log.d(TAG, "Currently Signed Out");
        }
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
     * Button click methods
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnter:

                email = etEmail.getText().toString();
                firstName = etFirstname.getText().toString();
                lastName = etLastname.getText().toString();
                username = etUsername.getText().toString();

                if (validateEmail(email) && validateName(firstName) && validateName(lastName)){
                    // if the information is valid, write it to the database
                    writeNewUser(username, firstName, lastName, email);
                }
                break;

            case R.id.btnCancel:
                Log.d(TAG, "btnCancel");
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                break;
        }
    }

    /**
     *  Validation methods below, rules are defined per method
     */
    private boolean validateUsername(String username){
        // if the username is more than 4 letters it is considered valid
        if (username.length() > 3) {
            return true;
        } else {
            Toast.makeText(UserProfActivity.this, R.string.usernames_at_least_four, Toast.LENGTH_SHORT).show();
            etUsername.setText(userInfo.getUsername());
            return false;
        }
    }
    private boolean validateName(String name){
        if (name.length() > 2) {
            return true;
        } else {
            Toast.makeText(UserProfActivity.this, R.string.names_at_least_three, Toast.LENGTH_SHORT).show();
            etFirstname.setText(userInfo.getFirstname());
            etLastname.setText(userInfo.getLastname());
            return false;
        }
    }
    private boolean validateEmail(String email){
        // validate for a unimelb email address
        if (email.contains("@") && email.endsWith("unimelb.edu.au")) {
            return true;
        } else {
            Toast.makeText(UserProfActivity.this, R.string.requires_unimelb_email, Toast.LENGTH_SHORT).show();
            etEmail.setText(userInfo.getEmail());
            return false;
        }
    }

    /**
     * Add a new user to the database
     */
    private void writeNewUser(String etUsername, String etFirstname, String etLastname, String etEmail) {

        final String username = etUsername;
        final String firstName = etFirstname;
        final String lastName = etLastname;
        final String email = etEmail;

        // Check if the username entered is unique by querying the database
        Query usernameQuery = mDatabase.orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0 && !username.equals(userInfo.getUsername())){
                    Toast.makeText(UserProfActivity.this, R.string.username_already_used, Toast.LENGTH_SHORT).show();
                    loadUserData();

                } else if (validateUsername(username)) {
                    // commit the information to the database
                    User user = new User(username, firstName, lastName, email);
                    mDatabase.child(uID).setValue(user);
                    Toast.makeText(UserProfActivity.this, R.string.successfuly_updated_details, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Fill the edit texts with the users information.
     */
    private void loadUserData(){

        //Fill the edit texts with the users information.
        if(userInfo.getUsername().equals(EMPTY)){
            etUsername.setHint(EMPTYUSERNAME);
        }else{
            etUsername.setText(userInfo.getUsername());
        }
        if(userInfo.getFirstname().equals(EMPTY)){
            etFirstname.setHint(EMPTYFIRST);
        }else{
            etFirstname.setText(userInfo.getFirstname());
        }
        if(userInfo.getLastname().equals(EMPTY)){
            etLastname.setHint(EMPTYLAST);
        }else{
            etLastname.setText(userInfo.getLastname());
        }
        etEmail.setText(userInfo.getEmail());

    }
    private void hideUserPage() {
        findViewById(R.id.userProfProgressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.llUser).setVisibility(View.INVISIBLE);
        findViewById(R.id.llFirst).setVisibility(View.INVISIBLE);
        findViewById(R.id.llLast).setVisibility(View.INVISIBLE);
        findViewById(R.id.llEmail).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnEnter).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnCancel).setVisibility(View.INVISIBLE);
    }

    private void showUserPage() {
        findViewById(R.id.userProfProgressBar).setVisibility(View.GONE);
        findViewById(R.id.llUser).setVisibility(View.VISIBLE);
        findViewById(R.id.llFirst).setVisibility(View.VISIBLE);
        findViewById(R.id.llLast).setVisibility(View.VISIBLE);
        findViewById(R.id.llEmail).setVisibility(View.VISIBLE);
        findViewById(R.id.btnEnter).setVisibility(View.VISIBLE);
        findViewById(R.id.btnCancel).setVisibility(View.VISIBLE);
    }


}
