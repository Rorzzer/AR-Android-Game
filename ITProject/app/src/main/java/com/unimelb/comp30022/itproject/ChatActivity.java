package com.unimelb.comp30022.itproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

import static java.lang.String.valueOf;

public class ChatActivity extends AppCompatActivity
        implements View.OnClickListener{


    private final String TAG = UserProfActivity.class.getName();
    private final String DUMMYGAMEID = "123456";

    private User userInfo = null;

    private FirebaseUser fUser;

    private String uID;
    private String username;
    private String chat;
    private String message;

    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etUsername;
    private EditText etChat;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.btnSend).setOnClickListener(this);

        etUsername = (EditText)findViewById(R.id.etUsername);
        etChat = (EditText)findViewById(R.id.etChat);
        etMessage = (EditText)findViewById(R.id.etMessage);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        updateFirebaseUser(mAuth);

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // update the fUser Firebase auth
                updateFirebaseUser(firebaseAuth);
            }
        };

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //private DataSnapshot dataSnapshot;
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    Log.d(TAG, "Datasnapshot exists");

                    if (fUser != null) {
                        userInfo = dataSnapshot.child("users").child(uID).getValue(User.class);
                        username = userInfo.getUsername();
                        etUsername.setText(username);
                        //mDatabase.child("chat").child("GameID").setValue(DUMMYGAMEID);
                        //mDatabase.child("chat").child(DUMMYGAMEID).child("Username").setValue(username);
                    } else {
                        Log.d(TAG, "fUser is null");
                        updateFirebaseUser(mAuth);
                    }

                    Log.d(TAG, "Value is: " + uID);



                    //chat = chat + "/n" + dataSnapshot.child("chat").child(DUMMYGAMEID).child(username).getValue().toString();

                    updateChat();


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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:


                message = etMessage.getText().toString();

                int DUMMYMESSAGEID = (int)(Math.random()*50000);

                String messageID = valueOf(DUMMYMESSAGEID);

                Chat chat = new Chat(username, DUMMYGAMEID, messageID, message);

                mDatabase.child("chat").child(DUMMYGAMEID).child(username).child(messageID).setValue(chat);

                break;
        }
    }


    private void updateChat(){


        etChat.setText(chat);

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


}
