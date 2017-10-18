package com.unimelb.comp30022.itproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.firebase.geofire.GeoFire;
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
    public static int PERMISSION_CODE = 99;
    private static String TAG = SessionInformationActivity.class.getName();
    private static String joinText = "Join";
    private static String leaveText = "Leave";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";

    private static final String GEO_FIRE_DB = "https://itproject-43222.firebaseio.com/";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/GeoFireData";

    ArrayList<Player> playerArrayList;
    ArrayList<Team> teamArrayList;
    ArrayList<String> joinedPlayers = new ArrayList<String>();
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
    private boolean hasFineLocationPermission = false;
    private Boolean canFetchLocations;

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

    private DatabaseReference GeoRef = FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF);
    private GeoFire geoFire = new GeoFire(GeoRef);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_information);
        Context context = getApplicationContext();

        tvSessionName = (TextView) findViewById(R.id.tvNameContent);
        tvCreator = (TextView)findViewById(R.id.tvCreatorContent);
        tvLocation = (TextView)findViewById(R.id.tvLocationContent);
        tvAddress = (TextView)findViewById(R.id.tvAddressContent);
        lvLoggedInMembers = (ListView) findViewById(R.id.lvPlayerListView);
        btnJoinGame = (Button) findViewById(R.id.btnJoinLeaveLobby);
        btnEditGame = (Button)findViewById(R.id.btnEditLobby);
        btnDeleteGame = (Button)findViewById(R.id.btnDeleteLobby);
        btnStartGame = (Button)findViewById(R.id.btnStartGame);
        findViewById(R.id.sessionContent).setVisibility(View.INVISIBLE);
        findViewById(R.id.loadingProgressLobby).setVisibility(View.VISIBLE);

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
        Log.d(TAG, gameSessionId);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fbuser = firebaseAuth.getCurrentUser();
                if(fbuser != null){
                    Log.d(TAG, "Retrieved firebaseUser");
                }
                else{
                    Log.d(TAG, "failure to retrieve firebaseUser");
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
                    Log.d(TAG, "User does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to read User info");
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnJoinLeaveLobby:
                Log.d(TAG, "Pressing Join/Leave Lobby");
                if(currentUserInfo != null){
                    generateAvailableLobbyInformation();
                    Log.d(TAG, "Spaces avaialble " + spacesAvailable + ":" + "isvalid game :" + isValidGame);
                    if (hasPlayerJoinedSession(getNewCurrentPlayer())) {
                        //remove player from session
                        removePlayerFromGameSession(getNewCurrentPlayer());
                        updateServerGameSession(publicGameSession);
                        loadDataToForm();
                        adapter.notifyDataSetChanged();
                        btnJoinGame.setText(joinText);
                    } else {
                        //add player to session
                        if (!hasGooglePlay()) {
                            Toast.makeText(SessionInformationActivity.this, R.string.google_play_unavailable, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            if (!getCurrentPermissions()) {
                                Log.d(TAG, "Doesn't have permissions requesting ");
                                //don't allow player to join the session until permissions are granted
                                shouldRequestPermissions();
                            } else {
                                //
                                Log.d(TAG, "hasPermissions is" + String.valueOf(hasFineLocationPermission));
                                addCurrentPlayerToGameSession();
                                Gson gson = new Gson();
                                Type gameSessionType = new TypeToken<GameSession>() {
                                }.getType();
                                Log.d(TAG, gson.toJson(publicGameSession, gameSessionType));
                                updateServerGameSession(publicGameSession);
                                loadDataToForm();
                                adapter.notifyDataSetChanged();
                                btnJoinGame.setText(leaveText);
                            }

                        }

                    }
                }
                else{
                    Log.d(TAG, "User not authenticated");
                }
                break;
            case R.id.btnEditLobby:
                Log.d(TAG, "Pressing Edit Lobby");
                if(currentUserInfo != null){
                    Intent sessionInformation = new Intent(SessionInformationActivity.this,CreateLobbyActivity.class);
                    sessionInformation.putExtra("gameSessionId",gameSessionId);
                    startActivity(sessionInformation);
                }
                else{
                    Log.d(TAG, "User not authenticated");
                }
                break;
            case R.id.btnDeleteLobby:
                Log.d(TAG, "Pressing Delete Lobby");
                if (currentUserInfo != null) {
                    deleteServerGameSessionObj(publicGameSession);
                    inactivatePage();
                    Toast.makeText(SessionInformationActivity.this, R.string.successful_game_deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "User not authenticated");
                }
                break;
            case R.id.btnStartGame:
                Log.d(TAG, "Pressing StartGame ");
                if (currentUserInfo != null) {
                    startGameSession();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(SessionInformationActivity.this, AndroidToUnitySenderService.class);
        stopService(intent);
    }

    /***
     * method that gets the game details for use in joining teams and launching gamesession
     */
    public void generateAvailableLobbyInformation() {
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
            Log.d(TAG, "Fetching available Lobby Info: public game session is null");
            isValidGame = false;
            return;
        }
    }

    private boolean getCurrentPermissions() {
        hasFineLocationPermission = (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        return hasFineLocationPermission;
    }

    private void locationRequestRationaleSnackbar(String mainText, String actionText, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                mainText,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionText, listener).show();
    }

    private boolean hasGooglePlay() {
        int availability = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (availability == ConnectionResult.SUCCESS) {
            return true;
        } else {
            GooglePlayServicesUtil.showErrorDialogFragment(availability, this, 0);
            return false;
        }

    }

    //whether the applications should request for permisssions
    public boolean shouldRequestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Post snackbar to explain permission request
            //request for permissions
            Log.d(TAG, "Explaining permissins request");

            canFetchLocations = false;
            locationRequestRationaleSnackbar(getResources().getString(R.string.permission_rationale),
                    getResources().getString(R.string.Ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(SessionInformationActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSION_CODE);
                        }
                    });
            return true;
        } else {
            //
            Log.d(TAG, "shouldn't show rationale just ask for permission");

            ActivityCompat.requestPermissions(SessionInformationActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_CODE);

        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length <= 0) {
                //failed permissions request
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                canFetchLocations = true;
            } else {
                //notify why permissions are being requested
                locationRequestRationaleSnackbar(getResources().getString(R.string.denied_permission_rationale),
                        getResources().getString(R.string.Ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Request permission
                                ActivityCompat.requestPermissions(SessionInformationActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_CODE);
                            }
                        });
            }

        }
    }
    /**
     * launches publicGameSession and passes the information necessary to launch the AR activity to unity.
     * */
    public void startGameSession(){
        //ensure players have been added to the session on the server
        generateAvailableLobbyInformation();
        if(!isValidGame){
            Toast.makeText(SessionInformationActivity.this, R.string.too_few_to_start, Toast.LENGTH_SHORT).show();
        }
        else{
            //identifier that the game is starting
            publicGameSession.setGameStarted(true);
            updateServerGameSession(publicGameSession);
            launchGameSession();
        }
    }
    /**
     * Fetch game sesion object if it already exists on the server
     * */
    private void getServerGameSessionObj(final String gameSessionId) {
        GameSession fetchedGameSession = null ;
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    //assign fetched value
                    publicGameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
                    findViewById(R.id.sessionContent).setVisibility(View.VISIBLE);
                    findViewById(R.id.loadingProgressLobby).setVisibility(View.GONE);
                    if (publicGameSession.getCreator().equals(currentUserInfo.getEmail())) {
                        showCreatorPage();
                    } else {
                        showPlayerPage();
                    }
                    if (hasPlayerJoinedSession(getNewCurrentPlayer())) {
                        btnJoinGame.setText(leaveText);
                    }
                    if (publicGameSession.getGameStarted() == true) {
                        inactivatePage();
                        Toast.makeText(SessionInformationActivity.this, R.string.game_unable_to_join_started_prompt, Toast.LENGTH_SHORT).show();
                    }
                    generateAvailableLobbyInformation();
                    loadDataToForm();
                    adapter.notifyDataSetChanged();
                    listenToServerForGameSessionChanges();
                    Log.d(TAG, " Successfully Fetched Game Session");
                }
                else{
                    //return null value
                    Log.d(TAG, "Game Session does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game Session - Read Error");
            }
        });
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
                    Log.d(TAG, " Successfully Updated Game Session");
                } else {
                    //create new value
                    gameSession.setSessionId(gameSessionId);
                    Log.d(TAG, " Successfully Updated Game Session");
                }
                gameSessionDbReference.child(gameSessionId).setValue(gameSession);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game Session - Update Error");
            }
        });
    }

    public void listenToServerForGameSessionChanges() {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                publicGameSession = dataSnapshot.getValue(GameSession.class);
                generateAvailableLobbyInformation();
                loadDataToForm();
                adapter.notifyDataSetChanged();
                if (publicGameSession.getGameStarted()) {
                    //game has started launch the game state if the player is one of the joined members
                    if (GameSession.containsPlayer(publicGameSession, getNewCurrentPlayer())) {
                        if (!currentUserInfo.getEmail().equals(publicGameSession.getCreator()) &&
                                isInitialising) {
                            launchGameSession();
                            Toast.makeText(SessionInformationActivity.this, R.string.game_started_prompt, Toast.LENGTH_SHORT).show();
                            isInitialising = false;
                            inactivatePage();
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(SessionInformationActivity.this, R.string.game_deleted_prompt, Toast.LENGTH_SHORT).show();
                inactivatePage();
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
            Log.d(TAG, " Game session not instantiated");
            return;
        }
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSession.getSessionId());
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    //value exists on server
                    gameSessionId = gameSession.getSessionId();
                    gameSessionDbReference.child(gameSession.getSessionId()).removeValue();
                    geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Log.d(TAG, " Successfully Deleted Game Session");
                }
                else{
                    //Value does not exist on server
                    Log.d(TAG, " Game Session does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game session - Deletion Error");
            }
        });
    }
    public void loadDataToForm(){
        tvSessionName.setText(publicGameSession.getSessionName());
        tvCreator.setText(publicGameSession.getCreator());
       // tvLocation.setText(publicGameSession.getLocation().toString());
        tvAddress.setText("Generated from location");
        refreshPlayerList(joinedPlayers);

    }

    public boolean addCurrentPlayerToGameSession() {
        if (spacesAvailable > 0) {
            boolean success = false;
            //create Player and join
            Team capturingTeam = publicGameSession.fetchCapturingTeam();
            Team escapingTeam = publicGameSession.fetchEscapingTeam();
            if (capturingTeam.getTeamSize() < publicGameSession.getMaxPlayers() / 2 &&
                    escapingTeam.getPlayerArrayList().size() >= capturingTeam.getTeamSize()) {
                Player player = getNewCurrentPlayer();
                setCurrentPlayerDetails(player, true);
                capturingTeam.addPlayer(player);
            }
            //add to escapping team if it has space or is smaller than the capturing team
            else if (escapingTeam.getTeamSize() < publicGameSession.getMaxPlayers() / 2 &&
                    capturingTeam.getTeamSize() >= escapingTeam.getTeamSize()) {
                Player player = getNewCurrentPlayer();
                setCurrentPlayerDetails(player, false);
                escapingTeam.addPlayer(player);
            }
            //update the player list
            loadDataToForm();
            adapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }

    }

    public void removePlayerFromGameSession(Player player) {
        if (hasPlayerJoinedSession(player)) {
            Team capturingTeam = publicGameSession.fetchCapturingTeam();
            Team escapingTeam = publicGameSession.fetchEscapingTeam();
            if (capturingTeam.containsPlayer(player)) {
                capturingTeam.removePlayer(player);
            } else if (escapingTeam.containsPlayer(player)) {
                escapingTeam.removePlayer(player);
            }
        }
    }

    public boolean hasPlayerJoinedSession(Player player) {
        Team capturingTeam = publicGameSession.getTeamArrayList().get(GameSession.TEAM_CAPTURING);
        Team escapingTeam = publicGameSession.getTeamArrayList().get(GameSession.TEAM_ESCAPING);
        return capturingTeam.getPlayerArrayList().contains(player) ||
                escapingTeam.getPlayerArrayList().contains(player);
    }

    public void setCurrentPlayerDetails(Player player, Boolean isCapturing) {
        if (isCapturing) {
            player.setCapturing(true);
            player.setAssignedTeamName(publicGameSession.getTeamArrayList().get(GameSession.TEAM_CAPTURING).getTeamName());
            player.setTeamId(publicGameSession.getTeamArrayList().get(GameSession.TEAM_CAPTURING).getTeamId());

        }
        if (!isCapturing) {
            player.setCapturing(false);
            player.setAssignedTeamName(publicGameSession.getTeamArrayList().get(GameSession.TEAM_ESCAPING).getTeamName());
            player.setTeamId(publicGameSession.getTeamArrayList().get(GameSession.TEAM_ESCAPING).getTeamId());

        }
        player.setHasBeenCaptured(false);
        player.setLastLoggedOn(System.currentTimeMillis());
        player.setLoggedOn(true);
        player.setLastPing(System.currentTimeMillis());
        player.setScore(0);
        player.setActive(true);
        player.setHasBeenCaptured(false);
    }

    /*
    * Acquire the player's key information for use in the game session
    * **/
    public Player getNewCurrentPlayer() {
        Player player = new Player(currentUserInfo.getEmail());
        return player;
    }

    //loads data into an arraylist for listview purposes, requries a Gamesession object declared in the scope
    public void refreshPlayerList(ArrayList<String> list) {
        list.clear();
        for (int i = 0; i < GameSession.MAX_TEAMS_2; i++) {
            playerArrayList = publicGameSession.getTeamArrayList().get(i).getPlayerArrayList();
            if (playerArrayList != null) {
                for (int j = 0; j < playerArrayList.size(); j++) {
                    String displayName = playerArrayList.get(j).getDisplayName();
                    if (!list.contains(displayName)) {
                        list.add(displayName);
                    }
                }
            }
        }
    }

    private void inactivatePage() {
        btnJoinGame.setClickable(false);
        btnStartGame.setClickable(false);
        btnDeleteGame.setClickable(false);
        btnEditGame.setClickable(false);
    }

    private void showCreatorPage() {
        btnDeleteGame.setVisibility(View.VISIBLE);
        btnEditGame.setVisibility(View.VISIBLE);
        btnStartGame.setVisibility(View.VISIBLE);
        btnJoinGame.setVisibility(View.VISIBLE);
    }

    private void showPlayerPage() {
        btnJoinGame.setVisibility(View.VISIBLE);
        btnEditGame.setVisibility(View.GONE);
        btnStartGame.setVisibility(View.GONE);
        btnDeleteGame.setVisibility(View.GONE);
    }

    private void launchGameSession() {
        Toast.makeText(getApplicationContext(), R.string.game_started_prompt, Toast.LENGTH_SHORT).show();
        Intent activeGame = new Intent(SessionInformationActivity.this, RunningGameActivity.class);
        geoFire.removeLocation(firebaseAuth.getCurrentUser().getUid());
        activeGame.putExtra(KEY_GAMESESSIONID_DATA, gameSessionId);
        startActivity(activeGame);
    }


}
