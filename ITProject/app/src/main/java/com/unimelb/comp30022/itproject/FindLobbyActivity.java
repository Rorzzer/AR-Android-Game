package com.unimelb.comp30022.itproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private List<String> gameNames = new ArrayList();
    private ArrayAdapter adapter;

    ListView listView;

    private ListView activeGamesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_lobby);
        activeGamesList = (ListView)findViewById(R.id.lvAvailableLobbies);
        Context context = getApplicationContext();
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
        fbuser = firebaseAuth.getCurrentUser();
        userId = fbuser.getUid();

        listView = (ListView)findViewById(R.id.lvAvailableLobbies);

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
                if(gameSessionList.size() > 0){
                    gameSessionList.clear();
                }
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    GameSession gameSession = snap.getValue(GameSession.class);
                    gameSessionList.add(gameSession);
                    gameNames.add(gameSession.getSessionName());
                    String [] gameArray = convertListToArray(gameNames);
                    adapter = new ArrayAdapter(FindLobbyActivity.this,android.R.layout.simple_list_item_1,gameArray);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

}
