package com.unimelb.comp30022.itproject;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Kiptenai on 4/10/2017.
 */

public class SessionTimePicker extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(),this,hour,minute, DateFormat.is24HourFormat(getActivity()));
    }
    public void onTimeSet(TimePicker view, int hour, int minute){
        //configure view
        TextView tvSelectedStartTime = getActivity().findViewById(R.id.tvSelectedStartTime);
        tvSelectedStartTime.setText(String.valueOf(hour) + " : " + String.valueOf(minute));
    }
}
