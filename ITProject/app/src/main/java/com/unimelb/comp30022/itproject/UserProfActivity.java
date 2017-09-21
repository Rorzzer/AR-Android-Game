package com.unimelb.comp30022.itproject;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class UserProfActivity extends AppCompatActivity
        implements View.OnClickListener{

    private final String TAG = UserProfActivity.class.getName();
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String uID;

    private EditText etUsername;
    private EditText etFirstname;
    private EditText etLastname;
    private EditText etEmail;

    private FirebaseUser fUser;

    private User userInfo;

    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DataSnapshot ds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_prof);
        findViewById(R.id.btnEnter).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get a reference to the database
        // Write a message to the database
        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        uID = fUser.getUid();

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        //etEmail = (EditText)findViewById(R.id.etEmail);
        etFirstname = (EditText)findViewById(R.id.etFirst);
        etLastname = (EditText)findViewById(R.id.etLast);
        etUsername = (EditText)findViewById(R.id.etUsername);
        etEmail = (EditText)findViewById(R.id.etEmail);


        //myRef.setValue("Hello, World!");


        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //private DataSnapshot dataSnapshot;
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                userInfo = dataSnapshot.child("users").child(uID).getValue(User.class);
                //ds.child("users").child(uID).getValue(User.class).getUsername()
                //ds = dataSnapshot;
                Log.d(TAG, "Value is: " + uID);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //userInfo = ds.child("users").child(uID).getValue(User.class);
        //dataSnapshot.get

        //etEmail.setText("rorororo");
        //etLastname.setText(userInfo.getLastName());
    }

    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnter:

                email = etEmail.getText().toString();
                firstName = etFirstname.getText().toString();
                lastName = etLastname.getText().toString();
                username = etUsername.getText().toString();

                writeNewUser(uID, username, firstName, lastName, email);

                break;
            case R.id.btnCancel:

                etUsername.setText("test");
                Log.d(TAG, "btnCancel");
                break;
        }
    }

    private boolean validateUsername(String username){
        return username.length() > 4;
    }

    private boolean validateName(String name){
        return name.length() > 3;
    }

    private boolean validateEmail(String email){
        return email.contains("@student.unimelb.edu.au");
    }

    // Add new user method
    private void writeNewUser(String userId, String etUsername, String etFirstname, String etLastname, String etEmail) {

        String username = "default";
        String firstName = "default";
        String lastName = "default";
        String email = "default@student.unimelb.edu.au";

        //userInfo = ds.child("users").child(uID).getValue(User.class);

        if (validateUsername(etUsername)){
            username = etUsername;
            } else if (userInfo.getUsername() != null) {
            // access database and place the previous value for field in variable
            username = userInfo.getUsername();
        }

        if (validateName(etFirstname)){
            firstName = etFirstname;
            } else if (userInfo.getFirstname() != null) {
            // access database and place the previous value for field in variable
            firstName = userInfo.getFirstname();
        }

        if (validateName(etLastname)){
            lastName = etLastname;
            } else if (userInfo.getLastname() != null) {
            // access database and place the previous value for field in variable
            lastName = userInfo.getLastname();
        }

        if (validateEmail(etEmail)){
            email = etEmail;
        } else if (userInfo.getEmail() != null) {
            // access database and place the previous value for field in variable
            email = userInfo.getEmail();
        }

        User user = new User(username, firstName, lastName, email);

        mDatabase.child(userId).setValue(user);
    }


}