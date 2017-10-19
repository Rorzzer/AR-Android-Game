package com.unimelb.comp30022.itproject;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;

/**
 * Created by Kiptenai on 18/10/2017.
 * Extension on automatically generated stub from unity
 *to convert it into a fragment
 */

public class UnityPlayerFragment extends Fragment {

    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    //Declare a FrameLayout object
    FrameLayout frameLayout;
    public UnityPlayerFragment() {
        // Required public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUnityPlayer = new UnityPlayer(getActivity());
        View view = inflater.inflate(com.unimelb.comp30022.itproject.R.layout.fragment_unity, container, false);

        this.frameLayout = (FrameLayout) view.findViewById(com.unimelb.comp30022.itproject.R.id.unityFrameLayout);

        this.frameLayout.addView(mUnityPlayer.getView(),
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        mUnityPlayer.requestFocus();
        mUnityPlayer.windowFocusChanged(true);
        return view;
    }
    // Quit Unity
    @Override
    public void onDestroy() {
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override
    public void onPause() {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override
    public void onResume() {
        super.onResume();
        mUnityPlayer.resume();
    }
     @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            mUnityPlayer.configurationChanged(newConfig);
       }
}
