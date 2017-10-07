package com.unimelb.comp30022.itproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener {

    /**
     * Standard Activity lifecycle methods
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // Set up click handlers and view item references
        findViewById(R.id.btnSignInReg).setOnClickListener(this);
        findViewById(R.id.btnUser).setOnClickListener(this);
        findViewById(R.id.btnMap).setOnClickListener(this);
        findViewById(R.id.btnCreateOrUpdateLobby).setOnClickListener(this);
        findViewById(R.id.btnFindLobby).setOnClickListener(this);
    }

    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        // TODO: add the AuthListener
        // mAuth.addAuthStateListener(mAuthListener);

    }
    /**
     * When the Activity resumes, the application should check authentication
     *
     */
    @Override
    public void onResume(){
        super.onResume();
        Button btnCreateLobby = findViewById(R.id.btnCreateOrUpdateLobby);
        Button btnFindLobby = findViewById(R.id.btnFindLobby);
        if (FirebaseAuth.getInstance().getCurrentUser()!= null){
            btnCreateLobby.setVisibility(View.VISIBLE);
            btnFindLobby.setVisibility(View.VISIBLE);

        }else{
            btnCreateLobby.setVisibility(View.GONE);
            btnFindLobby.setVisibility(View.GONE);
        }

    }
    @Override
    public void onStop() {
        super.onStop();
        /*
        // TODO: Remove the AuthListener
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
        */
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignInReg:
                Intent signIn = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(signIn);
                break;

            case R.id.btnUser:
                if (FirebaseAuth.getInstance().getCurrentUser()!= null){
                    Intent userProf = new Intent(getApplicationContext(), UserProfActivity.class);
                    startActivity(userProf);
                }else{
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
            case R.id.btnMap:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent MapsAct = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(MapsAct);
                } else {
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
            case R.id.btnCreateOrUpdateLobby:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent createLobby = new Intent(getApplicationContext(), CreateLobbyActivity.class);
                    startActivity(createLobby);
                } else {
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
            case R.id.btnFindLobby:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent findLobby = new Intent(getApplicationContext(), FindLobbyActivity.class);
                    startActivity(findLobby);
                } else {
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
        }
    }

    private void updateStatus(String stat) {
        TextView tvStat = findViewById(R.id.tvStatus);
        tvStat.setText(stat);
    }

}

