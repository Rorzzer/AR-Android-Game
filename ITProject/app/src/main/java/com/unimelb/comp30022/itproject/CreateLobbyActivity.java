package com.unimelb.comp30022.itproject;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CreateLobbyActivity extends AppCompatActivity
                implements View.OnClickListener{
    private static final int CAMERA_REQUEST_CODE = 1;
    private static String TAG = CreateLobbyActivity.class.getName();
    private static int SECONDS_IN_MINUTE = 60;
    private static int SECONDS_IN_HOUR = 3600;
    private static double MAX_GAME_DURATION_MINS = 60;
    private static int MAX_TEAM_SIZE = 30;
    private static int MIN_GAME_DURATION = 5;
    private static int MAX_GAME_RADIUS = 500;
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    Uri uri;
    StorageReference imagePath;
    //Firebase members
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot snapshot;
    private FirebaseUser fbuser ;
    private StorageReference storageReference;
    private User currentUserInfo;
    private String userId = null;
    private String userName;
    private String description;
    private LatLng location;
    private String Address;
    private Boolean isPublic;
    private Uri sessionImage;
    private Long startTime;
    private long durationInMinutes;
    private long durationInSeconds;
    private Integer gameRadius;
    private Integer maxTeamSize;
    private String sessionName;
    private boolean gameStarted;
    private String gameSessionId = null;
    private GameSession gameSession;
    private int teamSeekBarMaxValue;
    private int durationSeekBarMaxValue;
    private int minSeekBar;
    private double durationSeekBarUnit;
    private double teamSeekBarUnit;
    private boolean inEditMode;
    private boolean timeSelected = false;

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
    private Button btnSelectStartTime;
    private ImageButton addImage;
    private ImageView lobbyImage;
    private ListView  listView;
    private ArrayList list = new ArrayList();
    private ArrayAdapter adapter;
    private Handler handler;
    private ProgressBar uploadProgressBar;

    public static long getTomorrowMidnightInEpochTime(long timestamp) {
        Calendar givenDate = Calendar.getInstance();
        givenDate.setTimeInMillis(timestamp);
        givenDate.set(Calendar.DAY_OF_YEAR, givenDate.get(Calendar.DAY_OF_YEAR) + 1);
        givenDate.set(Calendar.HOUR_OF_DAY, 0);
        givenDate.set(Calendar.MINUTE, 0);
        givenDate.set(Calendar.SECOND, 0);
        givenDate.set(Calendar.MILLISECOND, 0);
        return givenDate.getTimeInMillis();
    }

    public static long getTodayMidnightInEpochTime(long timestamp) {
        Calendar givenDate = Calendar.getInstance();
        givenDate.setTimeInMillis(timestamp);
        givenDate.set(Calendar.HOUR_OF_DAY, 0);
        givenDate.set(Calendar.MINUTE, 0);
        givenDate.set(Calendar.SECOND, 0);
        givenDate.set(Calendar.MILLISECOND, 0);
        return givenDate.getTimeInMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lobby);
        Context context = getApplicationContext();

        etSessionName = findViewById(R.id.etLobbyName);
        tvSelectedStartTime = findViewById(R.id.tvSelectedStartTime);
        etDescription = findViewById(R.id.etDescription);
        listView = findViewById(R.id.lvPlayerListView);
        lobbyImage = findViewById(R.id.ivUploadImagePreview);
        tvDurationMinutes = findViewById(R.id.tvSelectedDuration);
        tvMaxTeamSize = findViewById(R.id.tvSelectedMaxSize);
        btnCreateOrEdit = findViewById(R.id.btnCreateOrUpdateLobby);
        btnDeleteOrCancel = findViewById(R.id.btnDeleteOrCancelLobby);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        addImage = findViewById(R.id.addImageButton);
        btnCreateOrEdit.setOnClickListener(this);
        btnDeleteOrCancel.setOnClickListener(this);
        addImage.setOnClickListener(this);
        btnSelectStartTime.setOnClickListener(this);
        durationSeekBar = findViewById(R.id.durationSlider);
        maxTeamSizeSeekbar = findViewById(R.id.teamSizeSlider);
        radioButtonPublicAccess = findViewById(R.id.btnPublic);
        radiobuttonPrivateAccess = findViewById(R.id.btnPrivate);
        uploadProgressBar = findViewById(R.id.pbUploadProgress);
        durationSeekBarMaxValue = durationSeekBar.getMax();
        teamSeekBarMaxValue = maxTeamSizeSeekbar.getMax();

        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbReference = firebaseDatabase.getReference("users");
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        storageReference = FirebaseStorage.getInstance().getReference();
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();

        //determine whether the user is editing or creating a new activity
        //fetch game SessionID if already created
        String activityImports = getIntent().getStringExtra("gameSessionId");

        if (activityImports != null) {
            inEditMode = true;
            //opening from another activity
            gameSessionId = activityImports;

        } else {
            inEditMode = false;
            gameSessionId = gameSessionDbReference.push().getKey();
        }
        //view controls to match the mode(edit / create)
        teamSeekBarUnit = MAX_TEAM_SIZE / teamSeekBarMaxValue;
        durationSeekBarUnit = (int) MAX_GAME_DURATION_MINS / durationSeekBarMaxValue;
        if (!inEditMode) {
            //setDefault values
            radioButtonPublicAccess.setChecked(true);
            isPublic = true;
            durationSeekBar.setProgress(durationSeekBarMaxValue / 2);
            tvDurationMinutes.setText(String.valueOf(durationSeekBarUnit * durationSeekBarMaxValue / 2));
            durationInSeconds = (int) (durationSeekBarUnit * durationSeekBarMaxValue / 2 * SECONDS_IN_MINUTE);
            durationInMinutes = durationInSeconds / SECONDS_IN_MINUTE;
            maxTeamSizeSeekbar.setProgress(teamSeekBarMaxValue / 2);
            tvMaxTeamSize.setText(String.valueOf(teamSeekBarUnit * teamSeekBarMaxValue / 2));
            maxTeamSize = (int) teamSeekBarUnit * teamSeekBarMaxValue / 2;
            btnCreateOrEdit.setText("Create");
            btnDeleteOrCancel.setText("Cancel");
        } else {
            btnCreateOrEdit.setText("Update");
            btnDeleteOrCancel.setText("Delete");
        }
        uploadProgressBar.setVisibility(View.GONE);
        //slider bar listeners
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                durationSeekBarUnit = MAX_GAME_DURATION_MINS / durationSeekBarMaxValue;
                durationInMinutes = (long) (durationSeekBarUnit * progressValue);
                durationInSeconds = durationInMinutes * SECONDS_IN_MINUTE;
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
                teamSeekBarUnit = MAX_TEAM_SIZE / teamSeekBarMaxValue;
                maxTeamSize = (int) teamSeekBarUnit * progressValue;
                tvMaxTeamSize.setText(String.valueOf(maxTeamSize));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        tvSelectedStartTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                timeSelected = true;
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
                        if (checkFormFields()) {
                            createNewLobby();
                        }
                    } else {
                        Toast.makeText(CreateLobbyActivity.this, R.string.lobby_unable_to_edit_started, Toast.LENGTH_SHORT);
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
                            deleteSessionImage();
                        } else {
                            Toast.makeText(CreateLobbyActivity.this, R.string.lobby_unable_to_delete_started, Toast.LENGTH_SHORT);
                        }
                    } else {
                        deleteSessionImage();
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
            case R.id.addImageButton:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            //valid image capture
            uri = data.getData();
            uploadImage(uri);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        uploadProgressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

    private void deleteSessionImage() {
        if (uri != null) {
            imagePath = storageReference.child("gameSessionPhotos").child(gameSessionId);
            imagePath.delete();
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
    }

    private void uploadImage(Uri uri) {
        if (uri != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Uploading Image")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            uploadProgressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            imagePath = storageReference.child("gameSessionPhotos").child(gameSessionId);
            imagePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sessionImage = taskSnapshot.getDownloadUrl();
                    Picasso.with(CreateLobbyActivity.this).load(sessionImage).resize(60, 60).centerCrop().into(lobbyImage);
                    uploadProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(CreateLobbyActivity.this, R.string.success_adding_image, Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    uploadProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(CreateLobbyActivity.this, R.string.error_adding_image, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * launches a new lobby for other individuals to view andjoin
     * */
    private boolean createNewLobby() {
        //create new lobby on server with creator on team 1
        //gameSession = new DataGenerator().generateRandomGameSession(new LatLng(-35.325,144.34234));
        gameSession = createNewGameSession(currentUserInfo);
        //notify individuals on invite list
        if(getAddressList() != null){
            inviteSelectedMembers(getAddressList());
        }
        Log.d(TAG, "creating new Lobby");
        //launch gamesession on server
        loadDataFromForm();
        updateServerGameSession(gameSession);
        return false;
    }

    private boolean checkFormFields() {
        String name, description, startTime;
        name = etSessionName.getText().toString();
        description = etDescription.getText().toString();

        if (name.isEmpty()) {
            etSessionName.setError("Name required");
            return false;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description required");
            return false;
        }
        if (timeSelected == false) {
            btnSelectStartTime.setError("Start time required");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Start time is required")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        if (durationInMinutes == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Duration cannot be set to zero minutes")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        if (maxTeamSize == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Team size cannot be set to zero members")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }

        Log.d(TAG, "all conditions in form met");
        return true;
    }

    /**
     * Updates the form if the user is editing their created sesssion in edit mode
     */
    public void loadDataToForm(GameSession gameSession){
        etSessionName.setText(gameSession.getSessionName());
        durationSeekBar.setProgress((int) (gameSession.getDuration().intValue() / (durationSeekBarUnit * SECONDS_IN_MINUTE)));
        maxTeamSizeSeekbar.setProgress((int) (gameSession.getMaxPlayers() / (2 * teamSeekBarUnit)));
        tvSelectedStartTime.setText(gameSession.getStartTimeString());
        if (gameSession.getSessionImageUri() != null) {
            Uri uri = Uri.parse(gameSession.getSessionImageUri());
            Picasso.with(CreateLobbyActivity.this).load(uri).resize(60, 60).centerCrop().into(lobbyImage);
        }
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
        //startTime = Long.parseLong(tvSelectedStartTime.getText().toString());
        gameSession.setDuration(new Long(durationInSeconds));
        gameSession.setGameRadius(new Integer(MAX_GAME_RADIUS));
        gameSession.setMaxPlayers(new Integer(maxTeamSize * 2));
        gameSession.setPublicAccess(isPublic);
        gameSession.setDescription(etDescription.getText().toString());
        gameSession.setStartTimeString(tvSelectedStartTime.getText().toString());
        String[] time = tvSelectedStartTime.getText().toString().replaceAll(" ", "").split(":");
        int selectedHour = Integer.valueOf(time[0]);
        int selectedMinute = Integer.valueOf(time[1]);
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentHour > selectedHour) {
            //assume selected time on next day get tomorrow epoch time
            startTime = getTomorrowMidnightInEpochTime(System.currentTimeMillis())
                    + selectedHour * SECONDS_IN_HOUR + selectedMinute + SECONDS_IN_MINUTE;
            gameSession.setStartTime(startTime);
        } else {
            //time is today
            startTime = getTodayMidnightInEpochTime(System.currentTimeMillis())
                    + selectedHour * SECONDS_IN_HOUR + selectedMinute + SECONDS_IN_MINUTE;
            gameSession.setStartTime(startTime);
        }
        if (sessionImage != null) {
            gameSession.setSessionImageUri(sessionImage.toString());
        }
        gameSession.setEndTime(new Long(gameSession.getStartTime().longValue() + gameSession.getDuration().longValue()));
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

    /**
     *manipulates layout elements to provide user with feedback about successful informaiton update
     */
    private void displaySuccessfulUpdate(){
        Toast.makeText(this, R.string.successful_game_created, Toast.LENGTH_SHORT).show();
    }

    /***
     * Generates a new gamesession based on form information
     * */
    private GameSession createNewGameSession(User user) {
        gameSession = new GameSession();
        gameSession.setSessionId(gameSessionId);
        gameSession.setGameStarted(false);
        gameSession.setGameCompleted(false);
        Player creator = new Player(user.getEmail());
        gameSession.setCreator(creator.getDisplayName());
        gameSession.add2Teams(gameSession.getSessionId(),creator);
        return gameSession;
    }

    /*
* allows for faster invites of players for private sessions
*
*/
    private ArrayList<String> getAddressList() {
        return null;
    }

    /*
    * notifies selected  memebers on the address list
    * */
    private boolean inviteSelectedMembers(ArrayList<String> addressList) {
        return false;
    }

    /***
     *
     * updates the listview containing the members to invite
     */
    private boolean updateLobbyInvites() {
        //add invite to listview
        return false;
    }

}
