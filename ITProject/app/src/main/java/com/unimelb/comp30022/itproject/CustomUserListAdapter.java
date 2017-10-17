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

public class CustomUserListAdapter extends ArrayAdapter {
    //to reference the Activity
    private final Activity context;
    //user images
    private final Integer[] imageIDarray;
    //user names
    private final String[] nameArray;
    //user Email
    private final String[] emailArray;

    public CustomUserListAdapter(Activity context, Integer[] imageIDArray, String[] nameArray, String[] emailArray) {

        super(context, R.layout.user_listview_row, nameArray);
        this.context = context;
        this.imageIDarray = imageIDArray;
        this.nameArray = nameArray;
        this.emailArray = emailArray;

    }

    public View getView(int pos, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View rowView = layoutInflater.inflate(R.layout.user_listview_row, null, true);
        TextView userNameText = rowView.findViewById(R.id.tvLvUsername);
        TextView userEmailText = rowView.findViewById(R.id.tvLvEmail);
        ImageView userImage = rowView.findViewById(R.id.ivLvUserImage);
        //find and assign relevant image, name and email information
        userNameText.setText(nameArray[pos]);
        userEmailText.setText(emailArray[pos]);
        userImage.setImageResource(imageIDarray[pos]);
        return rowView;
    }
}
