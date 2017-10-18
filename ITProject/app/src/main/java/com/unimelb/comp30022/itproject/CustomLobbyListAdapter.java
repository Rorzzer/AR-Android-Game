package com.unimelb.comp30022.itproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Kiptenai on 17/10/2017.
 */

public class CustomLobbyListAdapter extends ArrayAdapter {
    //to reference the Activity
    private final Activity context;
    //lobby images
    private final Integer[] imageIDarray;
    //lobby names
    private final String[] nameArray;
    //Lobby creator
    private final String[] creatorArray;

    public CustomLobbyListAdapter(Activity context, Integer[] imageIDArray, String[] nameArray, String[] creatorArray) {

        super(context, R.layout.lobbies_listview_row, nameArray);
        this.context = context;
        this.imageIDarray = imageIDArray;
        this.nameArray = nameArray;
        this.creatorArray = creatorArray;

    }

    public View getView(int pos, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View rowView = layoutInflater.inflate(R.layout.lobbies_listview_row, null, true);
        TextView lobbyNameText = rowView.findViewById(R.id.tvLobbyName);
        TextView lobbyCreatorText = rowView.findViewById(R.id.tvLobbyCreator);
        ImageView lobbyImage = rowView.findViewById(R.id.ivLvLobbyImage);
        //find and assign relevant image, lobby name and creator
        lobbyNameText.setText(nameArray[pos]);
        lobbyCreatorText.setText(creatorArray[pos]);
        lobbyImage.setImageResource(imageIDarray[pos]);
        return rowView;
    }

}
