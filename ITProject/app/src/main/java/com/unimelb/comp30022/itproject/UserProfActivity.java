package com.unimelb.comp30022.itproject;

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


public class UserProfActivity extends AppCompatActivity
        implements View.OnClickListener{

    private final String TAG = UserProfActivity.class.getName();
    private final String EMPTYFIRST = "Enter First name";
    private final String EMPTYLAST = "Enter Last name";
    private final String EMPTYUSERNAME = "Enter Username";
    private final String EMPTYEMAIL = "Enter Email";
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

        //etEmail = (EditText)findViewById(R.id.etEmail);
        etFirstname = (EditText)findViewById(R.id.etFirst);
        etLastname = (EditText)findViewById(R.id.etLast);
        etUsername = (EditText)findViewById(R.id.etUsername);
        etEmail = (EditText)findViewById(R.id.etEmail);

        //get a reference to the database
        // Write a message to the database
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

    private void updateFirebaseUser(FirebaseAuth firebaseAuth) {
        fUser = firebaseAuth.getCurrentUser();
        if (fUser != null) {
            uID = fUser.getUid();
            Log.d(TAG, "Signed in: " + fUser.getUid());
        }
        else{
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnter:

                email = etEmail.getText().toString();
                firstName = etFirstname.getText().toString();
                lastName = etLastname.getText().toString();
                username = etUsername.getText().toString();

                if (validateEmail(email) && validateName(firstName) && validateName(lastName)){
                    writeNewUser(uID, username, firstName, lastName, email);
                }
                break;

            case R.id.btnCancel:
                Log.d(TAG, "btnCancel");
                break;
        }
    }

    private boolean validateUsername(String username){
        // if the username is more than 4 letters it is considered valid
        if (username.length() > 3) {
            return true;
        } else {
            Toast.makeText(UserProfActivity.this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show();
            etUsername.setText(userInfo.getUsername());
            return false;
        }
    }

    private boolean validateName(String name){

        if (name.length() > 2) {
            return true;
        } else {
            Toast.makeText(UserProfActivity.this, "Names must be at least 3 characters long", Toast.LENGTH_SHORT).show();
            etFirstname.setText(userInfo.getFirstname());
            etLastname.setText(userInfo.getLastname());
            return false;
        }
    }

    private boolean validateEmail(String email){

        if (email.contains("@") && email.endsWith("unimelb.edu.au")) {
            return true;
        } else {
            Toast.makeText(UserProfActivity.this, "Must be a Unimelb Email", Toast.LENGTH_SHORT).show();
            etEmail.setText(userInfo.getEmail());
            return false;
        }
    }

    // Add new user method
    private void writeNewUser(String userId, String etUsername, String etFirstname, String etLastname, String etEmail) {

        final String username = etUsername;
        final String firstName = etFirstname;
        final String lastName = etLastname;
        final String email = etEmail;

        Query usernameQuery = mDatabase.orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0 && !username.equals(userInfo.getUsername())){
                    Toast.makeText(UserProfActivity.this, "Username already in use.", Toast.LENGTH_LONG).show();
                    loadUserData();
                } else if (validateUsername(username)){
                    // commit the information to the database
                    User user = new User(username, firstName, lastName, email);
                    mDatabase.child(uID).setValue(user);
                    Toast.makeText(UserProfActivity.this, "Details updated", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void loadUserData(){

        //Fill the edit texts with the users information.
        etUsername.setText(userInfo.getUsername());
        etFirstname.setText(userInfo.getFirstname());
        etLastname.setText(userInfo.getLastname());
        etEmail.setText(userInfo.getEmail());


    }

}
