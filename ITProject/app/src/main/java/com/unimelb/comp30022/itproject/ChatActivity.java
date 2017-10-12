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
    private String chat;
    private String message;

    private DatabaseReference mDatabase;
    private DatabaseReference chatRef;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etUsername;
    private EditText etChat;
    private EditText etMessage;

    private ArrayList<String> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.btnSend).setOnClickListener(this);

        etUsername = (EditText)findViewById(R.id.etUsername);
        etChat = (EditText)findViewById(R.id.etChat);
        etMessage = (EditText)findViewById(R.id.etMessage);

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
                            String message = chatClass.getMessage();
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
                        etUsername.setText(username);

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

                int DUMMYMESSAGEID = (int)(Math.random()*50000);

                String messageID = valueOf(DUMMYMESSAGEID);

                Chat chat = new Chat(username, DUMMYGAMEID, messageID, message);



                chatRef.child(chatRef.push().getKey()).setValue(chat);

                break;
        }
    }


    private void updateChat(ArrayList<String> messages){


//        ArrayList<Long> chatMessages = new ArrayList<>();

//        //iterate through each user, ignoring their UID
//        for (Map.Entry<String, Object> entry : messages.entrySet()){
//
//            //Get chat map
//            Map singleChat = (Map) entry.getValue();
//            //Get message field and append to list
//            chatMessages.add((Long) singleChat.get("message"));
//        }

        StringBuilder builder = new StringBuilder();
        for (String message : messages) {
            builder.append(message + "\n");
        }

        etChat.setText(builder.toString());

        //etChat.setText(messages.toString());
        //= mDatabase.child("chat").child(DUMMYGAMEID).child(username).

        //etChat.setText(chat);

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
