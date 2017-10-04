package com.unimelb.comp30022.itproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SessionInformationActivity extends AppCompatActivity
                                        implements View.OnClickListener {
    private static String  LOG_TAG = SessionInformationActivity.class.getName();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot snapshot;
    private FirebaseUser fbuser ;

    private String gameSessionId;
    private GameSession publicGameSession;
    private GameSession myGameSession;
    private User currentUserInfo;
    private String userId;
    private int spacesAvailable = 0;
    private int sumActivePlayers = 0;
    private boolean isValidGame = false;
    private ArrayList listViewContentList = new ArrayList();
    ArrayList<Player> playerArrayList;
    ArrayList<Team> teamArrayList;
    private ArrayAdapter adapter;

    private TextView tvSessionName;
    private TextView tvCreator;
    private TextView tvLocation;
    private TextView tvAddress;
    private ListView lvLoggedInMembers;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_information);
        Context context = getApplicationContext();

        tvSessionName = (TextView)findViewById(R.id.tvNameContent);
        tvCreator = (TextView)findViewById(R.id.tvCreatorContent);
        tvLocation = (TextView)findViewById(R.id.tvLocationContent);
        tvAddress = (TextView)findViewById(R.id.tvAddressContent);
        lvLoggedInMembers = (ListView)findViewById(R.id.lvPlayerListView);

        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbReference = firebaseDatabase.getReference("users");
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();
        findViewById(R.id.btnEditActiveLobby).setOnClickListener(this);
        findViewById(R.id.btnJoinLobby).setOnClickListener(this);

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
            case R.id.btnJoinLobby:
                getServerGameSessionObj(gameSessionId);
                Toast.makeText(this,currentUserInfo.getEmail() + "pressing Join Button : sessionid "+ gameSessionId,Toast.LENGTH_LONG).show();
                if(currentUserInfo != null){
                    if(spacesAvailable > 0 ){
                        //create Player and join
                    }
                }
                else{
                    Log.d(LOG_TAG, "User not authenticated");
                }
                break;
            case R.id.btnEditActiveLobby:
                if(currentUserInfo != null){
                    Intent sessionInformation = new Intent(SessionInformationActivity.this,CreateLobbyActivity.class);
                    sessionInformation.putExtra("gameSessionId",gameSessionId);
                    startActivity(sessionInformation);
                }
                else{
                    Log.d(LOG_TAG, "User not authenticated");
                }
                break;
        }
    }
    /***
     * method that gets the game details for use in joining teams and launching gamesession
     */
    public void fetchRecentLobbyInformation(){
        //fetch data about the game session using the information from the intent
        if(publicGameSession != null){
            sumActivePlayers = 0;
            for(int i = 0; i< publicGameSession.MAX_TEAMS_2; i++){
                teamArrayList = publicGameSession.getTeamArrayList();
                if(teamArrayList.get(i).getPlayerArrayList() != null) {
                    sumActivePlayers += teamArrayList.get(i).getPlayerArrayList().size();
                }
            }
            //exist games
            if(teamArrayList.get(0).getPlayerArrayList() == null ||
                    teamArrayList.get(1).getPlayerArrayList() == null){
                isValidGame = false;
                return;
            }
            else if(teamArrayList.get(0).getPlayerArrayList().size() == 0 ||
                    teamArrayList.get(1).getPlayerArrayList().size() == 0){
                isValidGame = false;
                return;
            }
            else{
                isValidGame = true;

            }
            spacesAvailable = publicGameSession.getMaxPlayers()- sumActivePlayers;
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
        if(!isValidGame){
            Toast.makeText(SessionInformationActivity.this,"Too few Members to Start Game",Toast.LENGTH_LONG).show();
        }
        else{
            //identifier that the game is starting
            publicGameSession.setGameStarted(true);
            updateServerGameSession(publicGameSession);
            Toast.makeText(getApplicationContext(), "Launching AR Camera" , Toast.LENGTH_SHORT).show();
            /***
             publicGameSession.updateRelativeLocations(origin);
             Gson gson = new Gson();
             Type gameSessionType = new TypeToken<GameSession>(){}.getType();
             String gameSessionString = gson.toJson(publicGameSession,gameSessionType);
             ServiceTools serviceTools = new ServiceTools();
             serviceTools.createNewService( LoginActivity.this, AndroidToUnitySender.class,
             "GameSession",gameSessionString );
             serviceTools.isServiceRunning(LoginActivity.this,AndroidToUnitySender.class);
             Log.d(LoginActivity.class.getName(), gameSessionString);
             Intent i = new Intent(LoginActivity.this, UnityPlayerActivity.class);
             startActivity(i);
             ***/
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
                    fetchRecentLobbyInformation();
                    loadDataToForm();
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
        /**
        gameSessionIdQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

         */
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    //update values
                    Log.d(LOG_TAG," Successfully Updated Game Session");
                }
                else{
                    //create new value
                    gameSession.setSessionId(gameSessionId);
                    Log.d(LOG_TAG," Successfully Updated Game Session");
                }
                gameSessionDbReference.child(gameSessionId).setValue(gameSession);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG,"Game Session - Update Error");
            }
        });
    }
    public void loadDataToForm(){
        tvSessionName.setText(publicGameSession.getSessionName());
        tvCreator.setText(publicGameSession.getCreator().getDisplayName());
        tvLocation.setText(publicGameSession.getLocation().toString());
        tvAddress.setText("Generated from location");

        for(int i=0;i< publicGameSession.MAX_TEAMS_2;i++){
            playerArrayList = publicGameSession.getTeamArrayList().get(i).getPlayerArrayList();
            if(playerArrayList != null){
                for(int j= 0;j < playerArrayList.size();j++){
                    listViewContentList.add(playerArrayList.get(j).getDisplayName());
                }
            }
        }
        if(listViewContentList.size() == 0){
            listViewContentList.add("No current Players");
        }
        adapter = new ArrayAdapter(SessionInformationActivity.this,android.R.layout.simple_list_item_1, listViewContentList);
        lvLoggedInMembers.setAdapter(adapter);


    }

}
