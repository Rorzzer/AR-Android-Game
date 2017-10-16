package com.unimelb.comp30022.itproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.Switch;
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


    private final String TAG = ChatActivity.class.getName();
    private final String DUMMYGAMEID = "123456";

    private User userInfo = null;

    private FirebaseUser fUser;

    private String uID;
    private String username;
    private String message;
    private String team;

    private DatabaseReference mDatabase;
    private DatabaseReference chatRef;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etMessage;

    private TextView tvUsername;
    private TextView tvChat;

    private Switch swtChat;

    private Chat chatClass;

    private ArrayList<String> teamMessageList;
    private ArrayList<String> publicMessageList;
    private ArrayList<String> chatList;

    private ArrayAdapter<String> publicChatAdapter;
    private ArrayAdapter<String> teamChatAdapter;

    private ListView lvChat;

    private StringBuilder teamChatBuilder;
    private StringBuilder publicChatBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        findViewById(R.id.btnSend).setOnClickListener(this);

        lvChat = (ListView) findViewById(R.id.lvChat);

//        publicMessageList = new ArrayList();
//        teamMessageList = new ArrayList();



        //lvChat.setAdapter(teamChatAdapter);
        //publicMessageList = new ArrayList<>();
        //lvChat.setAdapter(publicChatAdapter);


        etMessage = (EditText)findViewById(R.id.etMessage);
        etMessage.setText("");

        swtChat = (Switch)findViewById(R.id.swtChat);

        publicChatBuilder = new StringBuilder();
        teamChatBuilder = new StringBuilder();

        // this logic will be replaced by retreiving the team name from the database.
        team = "team_0";

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

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //private DataSnapshot dataSnapshot;
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    if (fUser != null) {

                        teamMessageList = new ArrayList<String>();
                        publicMessageList = new ArrayList<String>();

                        publicChatAdapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, publicMessageList);
                        teamChatAdapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, teamMessageList);

                        lvChat.setAdapter(publicChatAdapter);

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            chatClass = dsp.getValue(Chat.class);

                            // build a string per line of chat and append the username to it
                            StringBuilder chatLineBuilder = new StringBuilder();
                            chatLineBuilder.append(chatClass.getUsername() + ": " + chatClass.getMessage() + "\n");
                            String chatLine = chatLineBuilder.toString();

                            // sort the chats into the correct list
                            if (chatClass.isTeamOnly() && chatClass.getTeam().equals(team)) {
                                Log.d(TAG, "Add " + chatLine + " to team message list");
                                teamMessageList.add(String.valueOf(chatLine));
                                lvChat.setAdapter(teamChatAdapter);
                                teamChatAdapter.notifyDataSetChanged();


                            }else if (!chatClass.isTeamOnly()){
                                Log.d(TAG, "Add " + chatLine + " to pub message list");
                                publicMessageList.add(String.valueOf(chatLine));
                                lvChat.setAdapter(publicChatAdapter);
                                publicChatAdapter.notifyDataSetChanged();


                                // debug code, to print chat list
//                                for (String member : publicMessageList){
//                                    Log.i("List item: ", member);
//                                }
                            }
                        }
                        updateTeamChat(teamMessageList);
                        updatePublicChat(publicMessageList);
                        updateChat();
                    }
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

        // Update the chat display when switch is changed
        if (swtChat != null) {
            swtChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged");

                    if (isChecked) {
                        updateChatDisplay(teamChatBuilder);
//                        lvChat.setAdapter(teamChatAdapter);

                    }else{
                        updateChatDisplay(publicChatBuilder);
//                        lvChat.setAdapter(publicChatAdapter);


                    }
                }
            });
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:

                message = etMessage.getText().toString();
                etMessage.setText("");

                Chat chat = new Chat(username, message, "team_0", swtChat.isChecked());

                chatRef.child(chatRef.push().getKey()).setValue(chat);

                break;
        }
    }


    private void updateTeamChat(ArrayList<String> list){
        Log.d(TAG, "updateTeamChat: ");

        teamChatBuilder = new StringBuilder();

        for (String line : list) {
            Log.d(TAG, "team chat append line: " + line);
            teamChatBuilder.append(line);
        }
    }

    private void updatePublicChat(ArrayList<String> list){
        Log.d(TAG, "updatePublicChat: ");

        publicChatBuilder = new StringBuilder();

        for (String line : list) {
            Log.d(TAG, "pub chat append line: " + line);
            publicChatBuilder.append(line);
        }
    }

    private void updateChatDisplay(StringBuilder display){
        tvChat.setText(display);
    }

    private void updateChat(){

        if (swtChat.isChecked()){
            updateChatDisplay(teamChatBuilder);


        }else{
            updateChatDisplay(publicChatBuilder);

        }
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
        chatRef.setValue(null);

        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
