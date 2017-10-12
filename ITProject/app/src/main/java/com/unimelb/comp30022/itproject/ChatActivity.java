package com.unimelb.comp30022.itproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import static java.lang.String.valueOf;

public class ChatActivity extends AppCompatActivity
        implements View.OnClickListener{


    private final String TAG = UserProfActivity.class.getName();
    private final String DUMMYGAMEID = "123456";

    private User userInfo = null;

    private FirebaseUser fUser;

    private String uID;
    private String username;
    private String message;

    private DatabaseReference mDatabase;
    private DatabaseReference chatRef;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etMessage;

    private TextView tvUsername;
    private TextView tvChat;

    private ArrayList<String> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        findViewById(R.id.btnSend).setOnClickListener(this);

        etMessage = (EditText)findViewById(R.id.etMessage);

        tvUsername = (TextView)findViewById(R.id.tvUsername);
        tvChat = (TextView)findViewById(R.id.tvChat);
        tvChat.setMovementMethod(new ScrollingMovementMethod());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("users");
        chatRef = database.getReference("chat").child(DUMMYGAMEID);

        updateFirebaseUser(mAuth);

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // update the fUser Firebase auth
                updateFirebaseUser(firebaseAuth);
            }
        };

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //private DataSnapshot dataSnapshot;
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    if (fUser != null) {

                        //updateChat((Map<String,Object>) dataSnapshot.child(DUMMYGAMEID).getValue());

                        messageList = new ArrayList<String>();

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            Chat chatClass;
                            chatClass = dsp.getValue(Chat.class);
                            StringBuilder usernameAndMessage = new StringBuilder();
                            usernameAndMessage.append(chatClass.getUsername() + ": " + chatClass.getMessage());

                            String message = usernameAndMessage.toString();
                            messageList.add(String.valueOf(message)); //add result into array list
                        }

                        updateChat(messageList);

                    } else {

                    }

                    //updateChat();


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
                        username = userInfo.getUsername();
                        tvUsername.setText(username);

                        //mDatabase.child("chat").child("GameID").setValue(DUMMYGAMEID);
                        //mDatabase.child("chat").child(DUMMYGAMEID).child("Username").setValue(username);
                    } else {
                        Log.d(TAG, "fUser is null");
                        updateFirebaseUser(mAuth);
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:

                message = etMessage.getText().toString();

                Chat chat = new Chat(username, message, "team_1");

                chatRef.child(chatRef.push().getKey()).setValue(chat);

                break;
        }
    }


    private void updateChat(ArrayList<String> messages){

        StringBuilder builder = new StringBuilder();
        for (String message : messages) {
            builder.append(message + "\n");
        }

        tvChat.setText(builder.toString());

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


        // this logic will eventually be moved to the onStop of game session
        //chatRef.setValue(null);

        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }


    }


}
