package com.unimelb.comp30022.itproject;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class CreateLobbyActivity extends AppCompatActivity
                implements View.OnClickListener{
    private static String LOG_TAG = CreateLobbyActivity.class.getName();
    private static int MILLISECONDS_IN_MINUTE = 60000;
    private static double MAX_GAME_DURATION_MINS = 60;
    private static int MAX_TEAM_SIZE = 30;
    private static int MIN_GAME_DURATION = 5;
    private static int MAX_GAME_RADIUS = 500;
    //Firebase members
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot snapshot;
    private FirebaseUser fbuser ;

    private User currentUserInfo;
    private String userId;
    private String userName;
    private String description;
    private LatLng location;
    private String Address;
    private Boolean isPublic;
    private String sessionImageUri;
    private Long startTime;
    private Long endTime;
    private int durationInMinutes;
    private Long durationInMillis;
    private Integer gameRadius;
    private Integer maxTeamSize;
    private String sessionName;
    private String gameSessionId;
    private GameSession gameSession;
    private int maxSeekBar;
    private int minSeekBar;
    private double seekBarUnit;

    private EditText etSessionName;
    private EditText etImageUri;
    private EditText etStartTime;
    private EditText etDescription;
    private SeekBar durationSeekBar;
    private SeekBar maxTeamSizeSeekbar;
    private TextView tvDurationMinutes;
    private TextView tvMaxTeamSize;
    private RadioButton radioButtonPublicAccess;
    private ListView  listView;
    private ArrayList list = new ArrayList();
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lobby);
        Context context = getApplicationContext();
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbReference = firebaseDatabase.getReference("users");
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();

        etSessionName = (EditText)findViewById(R.id.etLobbyName);
        etImageUri = (EditText)findViewById(R.id.etImageUri);
        etStartTime = (EditText)findViewById(R.id.etStartTime);
        etDescription = (EditText)findViewById(R.id.etDescription);
        listView = (ListView)findViewById(R.id.lvPlayerListView);
        tvDurationMinutes = (TextView)findViewById(R.id.tvViewDurationContent);
        tvMaxTeamSize = (TextView)findViewById(R.id.tvViewMaxTeamSizeContent);
        findViewById(R.id.btnCreateLobby).setOnClickListener(this);
        findViewById(R.id.btnDeleteLobby).setOnClickListener(this);
        durationSeekBar = (SeekBar)findViewById(R.id.durationSlider);
        maxTeamSizeSeekbar = (SeekBar)findViewById(R.id.teamSizeSlider);
        radioButtonPublicAccess = (RadioButton)findViewById(R.id.btnPublic);

        radioButtonPublicAccess.setChecked(true);
        maxSeekBar = durationSeekBar.getMax();
        durationSeekBar.setProgress(maxSeekBar/2);

        //Seek bar listeners
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                seekBarUnit = MAX_GAME_DURATION_MINS/maxSeekBar;
                durationInMinutes = (int)seekBarUnit*progressValue;
                durationInMillis = new Long((int)(durationInMinutes * MILLISECONDS_IN_MINUTE));
                tvDurationMinutes.setText(new Integer(durationInMinutes).toString());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        maxSeekBar = maxTeamSizeSeekbar.getMax();
        maxTeamSizeSeekbar.setProgress(maxSeekBar/2);
        maxTeamSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                seekBarUnit = MAX_TEAM_SIZE/maxSeekBar;
                maxTeamSize = (int)seekBarUnit*progressValue;
                tvMaxTeamSize.setText(new Integer(maxTeamSize).toString());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        etStartTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if(isFocused){
                    DialogFragment dialogFragment = new SessionTimePicker();
                    dialogFragment.show(getFragmentManager(),"TimePicker");
                }
            }
        });

        //fetch game SessionID if already created
        String activityImports = getIntent().getStringExtra("gameSessionId");
        if(getIntent().getStringExtra("gameSessionId")!=null){
            //opening from another activity
            gameSessionId = activityImports;
        }
        /**
         * get AuthListener to fetch logged in user
         * */
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
                    Log.d(LOG_TAG, currentUserInfo.getEmail());
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

        if(getAddressList() == null){
            list.add("No Contacts");
        }
        else{
            for(String address : getAddressList()){
                list.add(address);
            }
        }

        adapter = new ArrayAdapter(CreateLobbyActivity.this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreateLobby:
                if(currentUserInfo != null){
                    createNewLobby(currentUserInfo);
                    Intent sessionInformation = new Intent(CreateLobbyActivity.this,SessionInformationActivity.class);
                    sessionInformation.putExtra("gameSessionId",gameSessionId);
                    startActivity(sessionInformation);
                }
                else{
                   Log.d(LOG_TAG, "User not authenticated");
                }
                break;
            case R.id.btnDeleteLobby:
                if(currentUserInfo != null){

                    deleteLobby(gameSessionId);
                }
                else{
                    Log.d(LOG_TAG, "User not authenticated");
                }
                break;
        }
    }
    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.btnPublic:
                if (checked)
                    // set public accessibility
                    isPublic = true;
                    Log.d(LOG_TAG,"Public Selected");
                    break;
            case R.id.btnPrivate:
                if (checked)
                    // set private accessibility
                    isPublic = false;
                Log.d(LOG_TAG,"Private Selected");
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
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
                displaySuccessfulUpdate();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG,"Game Session - Update Error");
            }
        });
    }
    /**
     * Delete GameSession object if it has been created on the server
     * */
    private void deleteServerGameSessionObj(final GameSession gameSession){
        if(gameSession== null || gameSession.getSessionId() == null){
            Log.d(LOG_TAG," Game session not instantiated");
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
                    Log.d(LOG_TAG," Successfully Deleted Game Session");
                }
                else{
                    //Value does not exist on server
                    Log.d(LOG_TAG," Game Session does not exist");
                }
                displaySuccessfulUpdate();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG,"Game session - Deletion Error");
            }
        });
    }

    /**
     * Fetch game sesion object if it already exists on the server
     * */
    private GameSession getSeverGameSessionObj(final String gameSessionId){
        GameSession fetchedGameSession = null ;
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    //assign fetched value
                    gameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
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
        return gameSession;
    }
    /**
     * launches a new lobby for other individuals to view andjoin
     * */
    private boolean createNewLobby(User userId){
        //create new lobby on server with creator on team 1
        loadFormData();
        gameSession = createMyNewGameSession(currentUserInfo);
        //notify individuals on invite list
        if(getAddressList() != null){
            inviteSelectedMembers(getAddressList());
        }
        //launch gamesession on server
        Gson gson = new Gson();
        Type gameSessionType = new TypeToken<GameSession>(){}.getType();
        //Log.d(LOG_TAG, gson.toJson(gameSession,gameSessionType));
        updateServerGameSession(gameSession);
        return false;
    }
    /**
     * allows a user to delete the current information from the device or server is the lobby
     * was launched
     * */
    private boolean deleteLobby(String gameSessionId){
        //if lobby is exists, delete server reference
        deleteServerGameSessionObj(gameSession);
        //if lobby is inactive, quit activity
        return false;
    }
    /**
     * updates data on the server if the lobby creator has altered the EditText fields
     * */
    private boolean updateLobbyInformation(){
        //if lobby exists update the lobby that matches current lobby id
        updateServerGameSession(gameSession);
        return false;
    }
    /***
     *
     * updates the listview containing the members to invite
     */
    private boolean updateLobbyInvites(){
        //add invite to listview
        return false;
    }

    public void LoadDataToForm(String gameSessionId){
        gameSession = getSeverGameSessionObj(gameSessionId);

    }

    /***
     * updates information from entered fields
     */
    public void loadFormData(){
        sessionName = etSessionName.getText().toString();
        Address = "getAddressFromLocation(Location)";
        //startTime = Long.parseLong(etStartTime.getText().toString());
        //endTime = Long.parseLong(etStartTime.getText().toString()) + durationInMillis ;
        gameRadius= new Integer(MAX_GAME_RADIUS);
        maxTeamSize = new Integer(10);
        description = etDescription.getText().toString();
    }
    /*
    * allows for faster invites of players for private sessions
    *
    */
    private ArrayList<String> getAddressList(){
        return null;
    }
    /*
    * notifies selected  memebers on the address list
    * */
    private boolean inviteSelectedMembers(ArrayList<String> addressList){
        return false;
    }
    /**
     *manipulates layout elements to provide user with feedback about successful informaiton update
     */
    private void displaySuccessfulUpdate(){
        Toast.makeText(this,"Successful update",Toast.LENGTH_LONG).show();
    }

    private GameSession createMyNewGameSession(User user){
        gameSession = new GameSession();
        gameSessionId = gameSessionDbReference.push().getKey();
        Player creator = new Player(user.getEmail());
        gameSession.setCreator(creator);
        gameSession.setMaxPlayers(maxTeamSize*gameSession.getMaxTeams());
        gameSession.add2Teams(gameSession.getSessionId(),creator);
        for(Team team : gameSession.getTeamArrayList()){
            team.setMaxPlayers(maxTeamSize);
        }
        gameSession.getTeamArrayList().get(0).addPlayer(creator);
        //update with current location, user, details
        gameSession.setLocation(new DataGenerator().generateRandomLocation());
        gameSession.setStartTime(new Long(2334324));
        //-->fetch
        gameSession.setDuration(new Long(2334324));
        gameSession.setEndTime(gameSession.getStartTime()+gameSession.getDuration());
        gameSession.setGameRadius(gameRadius);
        return gameSession;
    }

}
