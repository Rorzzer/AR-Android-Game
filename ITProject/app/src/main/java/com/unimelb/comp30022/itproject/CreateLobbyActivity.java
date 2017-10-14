package com.unimelb.comp30022.itproject;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private static String TAG = CreateLobbyActivity.class.getName();
    private static int MILLISECONDS_IN_MINUTE = 60000;
    private static double MAX_GAME_DURATION_MINS = 60;
    private static int MAX_TEAM_SIZE = 30;
    private static int MIN_GAME_DURATION = 5;
    private static int MAX_GAME_RADIUS = 500;
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    //Firebase members
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot snapshot;
    private FirebaseUser fbuser ;

    private User currentUserInfo;
    private String userId = null;
    private String userName;
    private String description;
    private LatLng location;
    private String Address;
    private Boolean isPublic;
    private String sessionImage;
    private Long startTime;
    private Long endTime;
    private int durationInMinutes;
    private Long durationInMillis;
    private Integer gameRadius;
    private Integer maxTeamSize;
    private String sessionName;
    private boolean gameStarted;
    private String gameSessionId = null;
    private GameSession gameSession;
    private int teamSeekBarMaxValue;
    private int durationSeekBarMaxValue;
    private int minSeekBar;
    private double seekBarUnit;
    private boolean inEditMode;

    private EditText etSessionName;
    private TextView tvSelectedStartTime;
    private EditText etDescription;
    private SeekBar durationSeekBar;
    private SeekBar maxTeamSizeSeekbar;
    private TextView tvDurationMinutes;
    private TextView tvMaxTeamSize;
    private RadioButton radioButtonPublicAccess;
    private RadioButton radiobuttonPrivateAccess;
    private Button btnCreateOrEdit;
    private Button btnDeleteOrCancel;
    private ListView  listView;
    private ArrayList list = new ArrayList();
    private ArrayAdapter adapter;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lobby);
        Context context = getApplicationContext();

        etSessionName = (EditText) findViewById(R.id.etLobbyName);
        tvSelectedStartTime = (TextView) findViewById(R.id.tvSelectedStartTime);
        etDescription = (EditText) findViewById(R.id.etDescription);
        listView = (ListView) findViewById(R.id.lvPlayerListView);
        tvDurationMinutes = (TextView) findViewById(R.id.tvSelectedDuration);
        tvMaxTeamSize = (TextView) findViewById(R.id.tvSelectedMaxSize);
        btnCreateOrEdit = (Button)findViewById(R.id.btnCreateOrUpdateLobby);
        btnDeleteOrCancel = (Button)findViewById(R.id.btnDeleteOrCancelLobby);
        btnCreateOrEdit.setOnClickListener(this);
        btnDeleteOrCancel.setOnClickListener(this);
        findViewById(R.id.btnSelectStartTime).setOnClickListener(this);
        durationSeekBar = (SeekBar) findViewById(R.id.durationSlider);
        maxTeamSizeSeekbar = (SeekBar) findViewById(R.id.teamSizeSlider);
        radioButtonPublicAccess = (RadioButton) findViewById(R.id.btnPublic);
        radiobuttonPrivateAccess = (RadioButton) findViewById(R.id.btnPrivate);
        durationSeekBarMaxValue = durationSeekBar.getMax();
        teamSeekBarMaxValue = maxTeamSizeSeekbar.getMax();

        //determine whether the user is editing or creating a new activity
        //fetch game SessionID if already created
        String activityImports = getIntent().getStringExtra("gameSessionId");

        if (activityImports != null) {
            inEditMode = true;
            //opening from another activity
            gameSessionId = activityImports;

        } else {
            inEditMode = false;
        }

        //view controls to match the mode(edit / create)
        if (!inEditMode) {
            //setDefault values
            radioButtonPublicAccess.setChecked(true);
            isPublic = true;
            durationSeekBar.setProgress(durationSeekBarMaxValue / 2);
            seekBarUnit = (int) MAX_GAME_DURATION_MINS / durationSeekBarMaxValue;
            tvDurationMinutes.setText(String.valueOf(seekBarUnit * durationSeekBarMaxValue / 2));
            durationInMillis = new Long((int) (seekBarUnit * durationSeekBarMaxValue / 2 * MILLISECONDS_IN_MINUTE));

            maxTeamSizeSeekbar.setProgress(teamSeekBarMaxValue / 2);
            seekBarUnit = MAX_TEAM_SIZE / teamSeekBarMaxValue;
            tvMaxTeamSize.setText(String.valueOf(seekBarUnit * teamSeekBarMaxValue / 2));
            maxTeamSize = (int) seekBarUnit * teamSeekBarMaxValue / 2;
            tvSelectedStartTime.setText("");
            btnCreateOrEdit.setText("Create");
            btnDeleteOrCancel.setText("Cancel");

        } else {
            btnCreateOrEdit.setText("Update");
            btnDeleteOrCancel.setText("Delete");
        }
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbReference = firebaseDatabase.getReference("users");
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();

        //slider bar listeners
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                seekBarUnit = MAX_GAME_DURATION_MINS / durationSeekBarMaxValue;
                durationInMinutes = (int)seekBarUnit*progressValue;
                durationInMillis = new Long(durationInMinutes * MILLISECONDS_IN_MINUTE);
                tvDurationMinutes.setText(String.valueOf(durationInMinutes));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        maxTeamSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                seekBarUnit = MAX_TEAM_SIZE / teamSeekBarMaxValue;
                maxTeamSize = (int)seekBarUnit*progressValue;
                tvMaxTeamSize.setText(String.valueOf(maxTeamSize));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        /**
         * get AuthListener to fetch logged in user
         * */
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
                    Log.d(TAG, currentUserInfo.getEmail());
                    if (inEditMode) {
                        //populate the form
                        getServerGameSessionObj(gameSessionId);
                    }
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
            case R.id.btnCreateOrUpdateLobby:
                if(currentUserInfo != null){
                    if (!gameStarted) {
                        createNewLobby(currentUserInfo);
                    } else {
                        Toast.makeText(CreateLobbyActivity.this, "Can't Edit running Game", Toast.LENGTH_SHORT);
                    }

                }
                else{
                    Log.d(TAG, "User not authenticated");
                }
                break;
            case R.id.btnDeleteOrCancelLobby:
                if(currentUserInfo != null){
                    if (inEditMode) {
                        if (!gameStarted) {
                            deleteServerGameSessionObj(gameSession);
                        } else {
                            Toast.makeText(CreateLobbyActivity.this, "Can't delete running Game", Toast.LENGTH_SHORT);
                        }

                    } else {
                        finish();
                    }

                }
                else{
                    Log.d(TAG, "User not authenticated");
                }
                break;
            case R.id.btnSelectStartTime:
                DialogFragment dialogFragment = new SessionTimePicker();
                dialogFragment.show(getFragmentManager(), "TimePicker");
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
                Log.d(TAG, "Public Selected");
                    break;
            case R.id.btnPrivate:
                if (checked)
                    // set private accessibility
                    isPublic = false;
                Log.d(TAG, "Private Selected");
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
                    //update values game already exists
                    Log.d(TAG, " Successfully Updated Game Session");
                }
                else{
                    //create new value game does not exist
                    gameSessionId = gameSessionDbReference.push().getKey();
                    gameSession.setSessionId(gameSessionId);
                    Log.d(TAG, " Successfully Updated Game Session");
                    gameSessionDbReference.child(gameSessionId).setValue(gameSession);
                }
                displaySuccessfulUpdate();
                launchSessionInformationActivity();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game Session - Update Error");
            }
        });
    }
    /**
     * Delete GameSession object if it has been created on the server
     * */
    private void deleteServerGameSessionObj(final GameSession gameSession){
        if(gameSession== null || gameSession.getSessionId() == null){
            Log.d(TAG, " Game session not instantiated unable to delete");
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
                    Log.d(TAG, " Successfully Deleted Game Session");
                }
                else{
                    //Value does not exist on server
                    Log.d(TAG, " Game Session does not exist");
                }
                displaySuccessfulUpdate();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game session - Deletion Error");
            }
        });
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
                    gameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
                    loadDataToForm(gameSession);
                    if (gameSession.getGameStarted()) {
                        gameStarted = true;
                    }
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
        return gameSession;
    }
    /**
     * launches a new lobby for other individuals to view andjoin
     * */
    private boolean createNewLobby(User userId){
        //create new lobby on server with creator on team 1
        //gameSession = new DataGenerator().generateRandomGameSession(new LatLng(-35.325,144.34234));
        gameSession = createMyNewGameSession(currentUserInfo);
        loadDataFromForm();
        //notify individuals on invite list
        if(getAddressList() != null){
            inviteSelectedMembers(getAddressList());
        }
        //launch gamesession on server
        updateServerGameSession(gameSession);
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

    /**
     * Updates the form if the user is editing their created sesssion in edit mode
     */
    public void loadDataToForm(GameSession gameSession){
        etSessionName.setText(gameSession.getSessionName());
        //durationSeekBar.setProgress(gameSession.getDuration().intValue()/durationSeekBar.getMax());
        //maxTeamSizeSeekbar.setProgress();
        //tvSelectedStartTime.setText();
        etDescription.setText(gameSession.getDescription());

        if(gameSession.getPublicAccess() == true){
            radioButtonPublicAccess.setChecked(true);
        }
        else{
            radiobuttonPrivateAccess.setChecked(true);
        }
    }

    /***
     * updates information from entered fields
     */
    public void loadDataFromForm(){
        gameSession.setSessionName(etSessionName.getText().toString());
        //gamesession.setSessionUri();
        //startTime = Long.parseLong(tvSelectedStartTime.getText().toString());
        //gameSession.get = Long.parseLong(etStartTime.getText().toString()) + durationInMillis.longValue();
        //gameSession.setGameRadius(new Integer(MAX_GAME_RADIUS));
        gameSession.setMaxPlayers(new Integer(maxTeamSize * 2));
        gameSession.setPublicAccess(isPublic);
        gameSession.setDescription(etDescription.getText().toString());
        gameSession.setStartTime(new Long(2334324));
        for (Team team : gameSession.getTeamArrayList()) {
            team.setMaxPlayers(maxTeamSize);
        }
        gameSession.setDuration(new Long(2334324));
        gameSession.setEndTime(new Long(gameSession.getStartTime().longValue() + gameSession.getDuration().longValue()));
        gameSession.setGameRadius(gameRadius);
        gameSession.setLocation(new DataGenerator().generateRandomLocation());
    }

    /**
     * launches the Session information Activity
     * */
    public void launchSessionInformationActivity() {
        Intent sessionInformation = new Intent(CreateLobbyActivity.this, SessionInformationActivity.class);
        sessionInformation.putExtra(KEY_GAMESESSIONID_DATA, gameSessionId);
        startActivity(sessionInformation);
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
    /***
     * Generates a new gamesession based on form information
     * */
    private GameSession createMyNewGameSession(User user){
        gameSession = new GameSession();
        gameSession.setGameStarted(false);
        gameSession.setGameCompleted(false);
        Player creator = new Player(user.getEmail());
        gameSession.setCreator(creator);
        gameSession.add2Teams(gameSession.getSessionId(),creator);
        loadDataFromForm();
        gameSession.addPlayerToCapturingTeam(creator);
        Gson gson = new Gson();
        DataGenerator dataGenerator = new DataGenerator();
        Type gameSessionType = new TypeToken<GameSession>() {
        }.getType();
        Log.d(TAG, gson.toJson(gameSession));
        return gameSession;
    }

}
