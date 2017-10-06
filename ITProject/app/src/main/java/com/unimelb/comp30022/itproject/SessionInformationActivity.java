package com.unimelb.comp30022.itproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SessionInformationActivity extends AppCompatActivity
                                        implements View.OnClickListener {
    private static String  LOG_TAG = SessionInformationActivity.class.getName();
    ArrayList<Player> playerArrayList;
    ArrayList<Team> teamArrayList;
    ArrayList<String> joinedPlayers = new ArrayList<String>();
    HashMap<String, String> serviceInputList = new HashMap<String, String>();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser fbuser ;
    private String gameSessionId;
    private GameSession publicGameSession;
    private GameSession myGameSession;
    private User currentUserInfo;
    private String userId;
    private int spacesAvailable = 0;
    private int sumActivePlayers = 0;
    private boolean isValidGame = false;
    private boolean isInitialising = true;
    private ArrayAdapter<String> adapter;
    private TextView tvSessionName;
    private TextView tvCreator;
    private TextView tvLocation;
    private TextView tvAddress;
    private Button btnJoinGame;
    private Button btnEditGame;
    private Button btnDeleteGame;
    private Button btnStartGame;
    private ListView lvLoggedInMembers;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_information);
        Context context = getApplicationContext();

        tvSessionName = findViewById(R.id.tvNameContent);
        tvCreator = findViewById(R.id.tvCreatorContent);
        tvLocation = findViewById(R.id.tvLocationContent);
        tvAddress = findViewById(R.id.tvAddressContent);
        lvLoggedInMembers = findViewById(R.id.lvPlayerListView);
        btnJoinGame = findViewById(R.id.btnJoinLeaveLobby);
        btnEditGame = findViewById(R.id.btnEditLobby);
        btnDeleteGame = findViewById(R.id.btnDeleteLobby);
        btnStartGame = findViewById(R.id.btnStartGame);

        btnJoinGame.setOnClickListener(this);
        btnDeleteGame.setOnClickListener(this);
        btnEditGame.setOnClickListener(this);
        btnStartGame.setOnClickListener(this);

        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbReference = firebaseDatabase.getReference("users");
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();

        btnDeleteGame.setVisibility(View.INVISIBLE);
        btnEditGame.setVisibility(View.INVISIBLE);
        btnStartGame.setVisibility(View.INVISIBLE);
        btnJoinGame.setVisibility(View.INVISIBLE);

        adapter = new ArrayAdapter<String>(SessionInformationActivity.this, android.R.layout.simple_list_item_1, joinedPlayers);
        lvLoggedInMembers.setAdapter(adapter);

        //update data from calling activity
        gameSessionId = getIntent().getStringExtra("gameSessionId");
        Log.d(LOG_TAG,gameSessionId);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fbuser = firebaseAuth.getCurrentUser();
                if(fbuser != null){
                    Log.d(LOG_TAG, "Retrieved firebaseUser");
                }
                else{
                    Log.d(LOG_TAG, "failure to retrieve firebaseUser");
                }
            }
        };
        /**
         * Fetch current user details
         * */
        userDbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserInfo = dataSnapshot.child(userId).getValue(User.class);
                    //update gamesession details on the view on create activity
                    getServerGameSessionObj(gameSessionId);
                }
                else{
                    Log.d(LOG_TAG, "User does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Failed to read User info");
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnJoinLeaveLobby:
                Log.d(LOG_TAG, "Pressing Join/Leave Lobby");
                if(currentUserInfo != null){
                    fetchAvailableLobbyInformation();
                    Log.d(LOG_TAG, "Spaces avaialble " + spacesAvailable + ":" + "isvalid game :" + isValidGame);
                    addCurrentPlayerToGameSession();

                }
                else{
                    Log.d(LOG_TAG, "User not authenticated");
                }
                break;
            case R.id.btnEditLobby:
                Log.d(LOG_TAG, "Pressing Edit Lobby");
                if(currentUserInfo != null){
                    Intent sessionInformation = new Intent(SessionInformationActivity.this,CreateLobbyActivity.class);
                    sessionInformation.putExtra("gameSessionId",gameSessionId);
                    startActivity(sessionInformation);
                }
                else{
                    Log.d(LOG_TAG, "User not authenticated");
                }
                break;
            case R.id.btnDeleteLobby:
                Log.d(LOG_TAG, "Pressing Delete Lobby");
                if (currentUserInfo != null) {
                    deleteServerGameSessionObj(publicGameSession);
                } else {
                    Log.d(LOG_TAG, "User not authenticated");
                }
                break;
            case R.id.btnStartGame:
                Log.d(LOG_TAG, "Pressing StartGame ");
                if (currentUserInfo != null) {
                    startGameSession();
                }
                break;


        }
    }
    /***
     * method that gets the game details for use in joining teams and launching gamesession
     */
    public void fetchAvailableLobbyInformation() {
        //fetch data about the game session using the information from the intent
        if(publicGameSession != null){
            sumActivePlayers = 0;
            teamArrayList = publicGameSession.getTeamArrayList();
            for (int i = 0; i < GameSession.MAX_TEAMS_2; i++) {
                if(teamArrayList.get(i).getPlayerArrayList() != null) {
                    sumActivePlayers += teamArrayList.get(i).getPlayerArrayList().size();
                }
            }
            spacesAvailable = publicGameSession.getMaxPlayers() - sumActivePlayers;
            //exist games
            if (teamArrayList.get(GameSession.TEAM_CAPTURING).getPlayerArrayList() == null ||
                    teamArrayList.get(GameSession.TEAM_ESCAPING).getPlayerArrayList() == null) {
                isValidGame = false;
                return;
            } else if (teamArrayList.get(GameSession.TEAM_CAPTURING).getPlayerArrayList().size() == 0 ||
                    teamArrayList.get(GameSession.TEAM_ESCAPING).getPlayerArrayList().size() == 0) {
                isValidGame = false;
                return;
            }
            else{
                isValidGame = true;
            }
            return;
        }
        else{
            isValidGame = false;
            return;
        }
    }
    /**
     * launches publicGameSession and passes the information necessary to launch the AR activity to unity.
     * */
    public void startGameSession(){
        //ensure players have been added to the session on the server
        fetchAvailableLobbyInformation();
        if(!isValidGame){
            Toast.makeText(SessionInformationActivity.this,"Too few Members to Start Game",Toast.LENGTH_LONG).show();
        }
        else{
            //identifier that the game is starting
            publicGameSession.setGameStarted(true);
            updateServerGameSession(publicGameSession);
            Toast.makeText(getApplicationContext(), "Launching AR Camera" , Toast.LENGTH_SHORT).show();
             Gson gson = new Gson();
            DataGenerator dataGenerator = new DataGenerator();
             Type gameSessionType = new TypeToken<GameSession>(){}.getType();
            String gameSessionString =
                    gson.toJson(dataGenerator.generateRandomGameSession(publicGameSession.getLocation()), gameSessionType);
            serviceInputList.put("gameSession", gameSessionString);
             ServiceTools serviceTools = new ServiceTools();
            ServiceTools.startNewService(SessionInformationActivity.this, AndroidToUnitySender.class, serviceInputList);
            ServiceTools.isServiceRunning(SessionInformationActivity.this, AndroidToUnitySender.class);
            Log.d(SessionInformationActivity.class.getName(), gameSessionString);
            //Intent i = new Intent(SessionInformationActivity.this, UnityPlayerActivity.class);
            //startActivity(i);
        }
    }

    /**
     * Fetch game sesion object if it already exists on the server
     * */
    private GameSession getServerGameSessionObj(final String gameSessionId){
        GameSession fetchedGameSession = null ;
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    //assign fetched value
                    publicGameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
                    if (publicGameSession.getCreator().getDisplayName().equals(currentUserInfo.getEmail())) {
                        btnDeleteGame.setVisibility(View.VISIBLE);
                        btnEditGame.setVisibility(View.VISIBLE);
                        btnStartGame.setVisibility(View.VISIBLE);
                    } else {
                        btnJoinGame.setVisibility(View.VISIBLE);
                    }
                    fetchAvailableLobbyInformation();
                    loadDataToForm();
                    //listenToServerForChanges();
                    Log.d(LOG_TAG," Successfully Fetched Game Session");
                }
                else{
                    //return null value
                    Log.d(LOG_TAG,"Game Session does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG,"Game Session - Read Error");
            }
        });
        return publicGameSession;
    }
    /***
     * updates a game session information for a specific value from local device to server
     * */
    private void updateServerGameSession(final GameSession gameSession){
        if(gameSession == null){
            return;
        }
        final String key = gameSession.getSessionId();
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(key);

        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //update values
                    Log.d(LOG_TAG, " Successfully Updated Game Session");
                } else {
                    //create new value
                    gameSession.setSessionId(gameSessionId);
                    Log.d(LOG_TAG, " Successfully Updated Game Session");
                }
                gameSessionDbReference.child(gameSessionId).setValue(gameSession);
                Gson gson = new Gson();
                Type gameSessionType = new TypeToken<GameSession>() {
                }.getType();
                String gameSessionString =
                        gson.toJson(publicGameSession, gameSessionType);
                Log.d(LOG_TAG, gameSessionString);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game Session - Update Error");
            }
        });
    }

    public void listenToServerForChanges() {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                publicGameSession = dataSnapshot.getValue(GameSession.class);
                fetchAvailableLobbyInformation();
                loadDataToForm();
                Toast.makeText(SessionInformationActivity.this, "Game details have Changed", Toast.LENGTH_LONG).show();

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(SessionInformationActivity.this, "The Session Was Deleted, Joining is not allowed", Toast.LENGTH_LONG).show();
                btnJoinGame.setVisibility(View.GONE);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /**
     * Delete GameSession object if it has been created on the server
     */
    private void deleteServerGameSessionObj(final GameSession gameSession) {
        if (gameSession == null || gameSession.getSessionId() == null) {
            Log.d(LOG_TAG, " Game session not instantiated");
            return;
        }
        final String key = gameSession.getSessionId();
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(key);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    //value exists on server
                    gameSessionId = gameSession.getSessionId();
                    gameSessionDbReference.child(key).removeValue();
                    Log.d(LOG_TAG, " Successfully Deleted Game Session");
                }
                else{
                    //Value does not exist on server
                    Log.d(LOG_TAG, " Game Session does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game session - Deletion Error");
            }
        });
    }
    public void loadDataToForm(){
        tvSessionName.setText(publicGameSession.getSessionName());
        tvCreator.setText(publicGameSession.getCreator().getDisplayName());
        tvLocation.setText(publicGameSession.getLocation().toString());
        tvAddress.setText("Generated from location");

        for (int i = 0; i < GameSession.MAX_TEAMS_2; i++) {
            playerArrayList = publicGameSession.getTeamArrayList().get(i).getPlayerArrayList();
            if(playerArrayList != null){
                for(int j= 0;j < playerArrayList.size();j++){
                    String displayName = playerArrayList.get(j).getDisplayName();
                    if (!joinedPlayers.contains(displayName)) {
                        joinedPlayers.add(displayName);
                    }
                }
            }
        }

    }

    public String[] convertListToArray(List<String> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public boolean addCurrentPlayerToGameSession() {
        if (spacesAvailable > 0) {
            boolean success = false;
            //create Player and join
            int capturingPlayers = publicGameSession.getTeamArrayList().get(GameSession.TEAM_CAPTURING).getPlayerArrayList().size();
            int escapingPlayers = publicGameSession.getTeamArrayList().get(GameSession.TEAM_ESCAPING).getPlayerArrayList().size();
            Log.d(LOG_TAG, "capturing players:" + capturingPlayers + " Escaping players:" + escapingPlayers);
            if (capturingPlayers
                    < publicGameSession.getMaxPlayers() / 2) {
                success = publicGameSession.getTeamArrayList().get(GameSession.TEAM_CAPTURING).addPlayer(getCurrentPlayer());
            } else if (escapingPlayers < publicGameSession.getMaxPlayers() / 2) {
                success = publicGameSession.getTeamArrayList().get(GameSession.TEAM_ESCAPING).addPlayer(getCurrentPlayer());
            }
            Log.d(LOG_TAG, "updating the player list with " + currentUserInfo.getEmail());
            if (success) {
                if (!joinedPlayers.contains(currentUserInfo.getEmail())) {
                    joinedPlayers.add(currentUserInfo.getEmail());
                } else {
                    Toast.makeText(SessionInformationActivity.this, "Already Joined Session", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
                updateServerGameSession(publicGameSession);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /*
    * Acquire the player's key information for use in the game session
    * **/
    public Player getCurrentPlayer() {
        Player player = new Player(currentUserInfo.getEmail());
        return player;
    }


}
