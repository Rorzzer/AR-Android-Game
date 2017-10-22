package com.unimelb.comp30022.itproject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by RoryPowell.
 * This class contains all the logic for the chat fragment,
 * it read and writes to the database
 */

public class ChatFragment extends Fragment implements View.OnClickListener {

    public static final String PUBLIC_CHAT = "PUBLIC_CHAT";
    private final String TAG = ChatFragment.class.getName();

    private User userInfo = null;
    private GameSession gameSession = null;
    private Player player = null;
    private FirebaseUser fUser;

    private String uID;
    private String username;
    private String team;
    private String gameSessionId;
    private Boolean validUser = false;

    private DatabaseReference mDatabase;
    private DatabaseReference chatRef;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etMessage;
    private TextView tvUsername;
    private Switch swtChat;
    private Button btnSend;

    private Chat chatClass;

    private ArrayList<String> teamMessageList;
    private ArrayList<String> publicMessageList;
    private ArrayAdapter<String> publicChatAdapter;
    private ArrayAdapter<String> teamChatAdapter;

    private ListView lvChat;

    private StringBuilder teamChatBuilder;
    private StringBuilder publicChatBuilder;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String gameSessionId) {

        Bundle bundle = new Bundle();
        bundle.putString("gameSessionId", gameSessionId);

        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Read the bundle passed from the activity
     */
    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            gameSessionId = bundle.getString("gameSessionId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        //get a reference to the enclosing activity
        final Activity activity = getActivity();

        // read the arguments passed to the fragment on creation
        readBundle(getArguments());

        Log.d(TAG, "GameSessionId: " + gameSessionId);

        btnSend = (Button) v.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        lvChat = (ListView) v.findViewById(R.id.lvChat);

        etMessage = (EditText) v.findViewById(R.id.etMessage);
        etMessage.setText("");

        swtChat = (Switch) v.findViewById(R.id.swtChat);

        publicChatBuilder = new StringBuilder();
        teamChatBuilder = new StringBuilder();

        tvUsername = (TextView) v.findViewById(R.id.tvUsername);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        chatRef = database.getReference("chat").child(gameSessionId);
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

                        Log.d(TAG, "User ID is: " + uID);

                        userInfo = dataSnapshot.child("users").child(uID).getValue(User.class);

                        if (!gameSessionId.equals(PUBLIC_CHAT)) {

                            gameSession = dataSnapshot.child("gameSessions").child(gameSessionId).getValue(GameSession.class);

                            // get the game session details and player information
                            Log.d(TAG, "creator: " + gameSession.getCreator());
                            player = gameSession.getPlayerDetails(userInfo.getEmail());
                            team = player.getTeamId();

                            Log.d(TAG, "team: " + team);
                        }

                        // do not allow users that have not created a username to use the chat feature
                        if (userInfo.getUsername().equals("Empty")) {
                            validUser = false;
                        } else {
                            validUser = true;
                            username = userInfo.getUsername();
                            tvUsername.setText(username);
                        }
                    } else {

                        Log.d(TAG, "fUser is null");
                        validUser = false;
                        updateFirebaseUser(mAuth);
                    }

                } else {
                    Log.d(TAG, "The datasnapshot does not exist");
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

                        // Use arrays and array adapters to display the chat messages
                        teamMessageList = new ArrayList<String>();
                        publicMessageList = new ArrayList<String>();

                        publicChatAdapter = new ArrayAdapter<String>(activity.getApplicationContext(), android.R.layout.simple_list_item_1, publicMessageList);
                        teamChatAdapter = new ArrayAdapter<String>(activity.getApplicationContext(), android.R.layout.simple_list_item_1, teamMessageList);

                        // public chat is the default chat
                        lvChat.setAdapter(publicChatAdapter);

                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                            chatClass = dsp.getValue(Chat.class);

                            // build a string per line of chat and append the username to it
                            StringBuilder chatLineBuilder = new StringBuilder();
                            chatLineBuilder.append(chatClass.getUsername() + ": " + chatClass.getMessage() + "\n");
                            String chatLine = chatLineBuilder.toString();

                            // sort the chats into the correct list for team or public chat
                            if (chatClass.isTeamOnly() && chatClass.getTeam().equals(team)) {

                                //debug
                                Log.d(TAG, "Add " + chatLine + " to team message list");

                                teamMessageList.add(String.valueOf(chatLine));
                                lvChat.setAdapter(teamChatAdapter);
                                teamChatAdapter.notifyDataSetChanged();


                            }else if (!chatClass.isTeamOnly()){

                                //debug
                                Log.d(TAG, "Add " + chatLine + " to public message list");

                                publicMessageList.add(String.valueOf(chatLine));
                                lvChat.setAdapter(publicChatAdapter);
                                publicChatAdapter.notifyDataSetChanged();

                            }
                        }

                        updateTeamChat(teamMessageList);
                        updatePublicChat(publicMessageList);
                    }
                } else {
                    Log.d(TAG, "The datasnapshot does not exist");
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
                    Log.d(TAG, "Chat switch onCheckedChanged");

                    //change the listview display when the switch is flicked
                    if (isChecked) {
                        lvChat.setAdapter(teamChatAdapter);
                        teamChatAdapter.notifyDataSetChanged();

                    }else{
                        lvChat.setAdapter(publicChatAdapter);
                        publicChatAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        return v;
    }

    /**
     * Write the chat object to the database when Send is clicked
     */
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnSend:
                if (validUser) {
                    String message;
                    message = etMessage.getText().toString();
                    etMessage.setText("");

                    Chat chat = new Chat(username, message, team, swtChat.isChecked());

                    //write the class to the database
                    chatRef.child(chatRef.push().getKey()).setValue(chat);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Please create a user profile.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Update the team chat builder with the chat lines from the arraylist
     */
    private void updateTeamChat(ArrayList<String> list){
        Log.d(TAG, "updateTeamChat: ");

        teamChatBuilder = new StringBuilder();

        for (String line : list) {
            Log.d(TAG, "team chat append line: " + line);
            teamChatBuilder.append(" (" + team + "): " + line);
        }
    }

    /**
     * Update the public chat builder with the chat lines from the arraylist
     */
    private void updatePublicChat(ArrayList<String> list){
        Log.d(TAG, "updatePublicChat: ");

        publicChatBuilder = new StringBuilder();

        for (String line : list) {
            Log.d(TAG, "public chat append line: " + line);
            publicChatBuilder.append(line);
        }
    }

    /**
     * Update the firebase user
     */
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
