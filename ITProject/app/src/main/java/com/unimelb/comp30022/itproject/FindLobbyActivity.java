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

    private static String  LOG_TAG = FindLobbyActivity.class.getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot snapshot;
    private FirebaseUser fbuser ;

    private User currentUserInfo;
    private String userId;
    private String gameSessionId;
    private List<GameSession> gameSessionList = new ArrayList();
    private String[] gameArray;
    private ArrayList<String> availableGames = new ArrayList();
    private ArrayAdapter<String> adapter;
    private ListView activeGamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_lobby);
        activeGamesList = findViewById(R.id.lvAvailableLobbies);

        Context context = getApplicationContext();
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        //gameSessionDbReference = firebaseDatabase.getReference().child("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();
        adapter = new ArrayAdapter<String>(FindLobbyActivity.this, android.R.layout.simple_list_item_1, availableGames);
        activeGamesList.setAdapter(adapter);


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
        gameSessionDbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "changed data added");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getActiveGameSessions();
        updateSubsequentActiveGameSessions();
        activeGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent viewGameSession = new Intent(FindLobbyActivity.this,SessionInformationActivity.class);
                viewGameSession.putExtra("gameSessionId",gameSessionList.get(i).getSessionId());
                startActivity(viewGameSession);
            }
        });
    }
    public String[] convertListToArray(List<String> list){
        String[] array = new String[list.size()];
        for(int i =0; i<list.size();i++){
            array[i] = list.get(i);
        }
        return  array;
    }
    /***
     * Searches sever for available lobbies that meet criteria
     */
    public void filterGameList(){

    }

    private void getActiveGameSessions() {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("gameStarted").equalTo(false);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        GameSession gameSession = snap.getValue(GameSession.class);
                        gameSessionList.add(gameSession);
                        availableGames.add(gameSession.getSessionName());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    //return null value
                    Log.d(LOG_TAG, "Game Session does not exist");
                }
                //updateSubsequentActiveGameSessions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game Session - Read Error");
            }
        });
    }

    private void updateSubsequentActiveGameSessions() {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("gameCompleted").equalTo(false);
        gameSessionIdQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GameSession gameSession = dataSnapshot.getValue(GameSession.class);
                if (!gameSessionList.contains(gameSession)) {
                    gameSessionList.add(gameSession);
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
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }



}
