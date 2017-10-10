package com.unimelb.comp30022.itproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

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

import java.util.ArrayList;
import java.util.List;

public class FindLobbyActivity extends AppCompatActivity {
    private static int ZERO = 0;
    private static String TAG = FindLobbyActivity.class.getName();
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot snapshot;
    private FirebaseUser fbuser ;

    private boolean isGameListEmpty = false;
    private User currentUserInfo;
    private String userId;
    private String gameSessionId;
    private List<GameSession> gameSessionList = new ArrayList();
    private String[] gameArray;
    private ArrayList<String> availableGames = new ArrayList();
    private ArrayAdapter<String> adapter;
    private ListView activeGamesList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_lobby);
        activeGamesList = findViewById(R.id.lvAvailableLobbies);
        progressBar = findViewById(R.id.loadingProgressBar);

        Context context = getApplicationContext();
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();
        adapter = new ArrayAdapter<String>(FindLobbyActivity.this, android.R.layout.simple_list_item_1, availableGames);
        activeGamesList.setAdapter(adapter);
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
        Log.d(TAG, "Launching android to unity service");
        showProgressStarted();
        getActiveGameSessions();
        activeGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (gameSessionList.size() > ZERO) {
                    Intent viewGameSession = new Intent(FindLobbyActivity.this, SessionInformationActivity.class);
                    viewGameSession.putExtra(KEY_GAMESESSIONID_DATA, gameSessionList.get(i).getSessionId());
                    startActivity(viewGameSession);
                }
            }
        });

    }
    /***
     * Searches sever for available lobbies that meet criteria
     */
    public void filterGameList(){

    }

    private void getActiveGameSessions() {
        Log.d(TAG, "Running to getActive Games list");
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("gameStarted").equalTo(false);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > ZERO) {
                    Log.d(TAG, "Datasnapshot is greater than zero");
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        GameSession gameSession = snap.getValue(GameSession.class);
                        gameSessionList.add(gameSession);
                        availableGames.add(gameSession.getSessionName());
                        Log.d(TAG, "Adding to gamesesion list" + gameSession.getSessionName());
                    }
                    adapter.notifyDataSetChanged();
                    for (String s : availableGames) {
                        Log.d(TAG, "availableGames has:" + s);
                    }

                } else {
                    Log.d(TAG, "DataSnapshot is zero");
                    isGameListEmpty = true;
                    availableGames.add("No Games Found");
                    adapter.notifyDataSetChanged();

                }
                progressCompleted();
                updateSubsequentActiveGameSessions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Game Session - Read Error" + databaseError.getMessage());
            }
        });
    }
    private void updateSubsequentActiveGameSessions() {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("gameStarted").equalTo(false);
        gameSessionIdQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GameSession gameSession = dataSnapshot.getValue(GameSession.class);
                if (isGameListEmpty) {
                    availableGames.clear();
                }
                if (!gameSessionList.contains(gameSession)) {
                    gameSessionList.add(gameSession);
                    availableGames.add(gameSession.getSessionName());
                    adapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GameSession gameSession = dataSnapshot.getValue(GameSession.class);
                if (gameSessionList.contains(gameSession)) {
                    gameSessionList.remove(gameSession);
                    availableGames.remove(gameSession.getSessionName());
                }
                if (gameSessionList.size() == ZERO) {
                    availableGames.add("No Games Found");
                    isGameListEmpty = true;
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void showProgressStarted() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void progressCompleted() {
        progressBar.setVisibility(View.GONE);
    }

}
