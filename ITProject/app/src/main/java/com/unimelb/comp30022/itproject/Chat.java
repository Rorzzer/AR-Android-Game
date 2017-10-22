package com.unimelb.comp30022.itproject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by RoryPowell on 5/10/17.
 * Class to be stored in the database as a chat object.
 * It holds information of each chat message sent.
 */

public class Chat {

    private String username;
    private String message;
    private String dateTime;
    private Date date;
    private long time;
    private String team;
    private boolean teamOnly;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Chat(String userName, String message, String team, boolean teamOnly) {

        // each value is a line stored in the database, they are assigned by the constructor
        this.username = userName;
        this.message = message;
        this.date = new Date();
        this.time = date.getTime();
        this.team = team;
        this.teamOnly = teamOnly;

        //the class saves the date, the value is not used but can be viewed in the database
        // if needed
        Date dNow = new Date( );
        SimpleDateFormat ft =
                new SimpleDateFormat ("hh:mm:ss a zzz E dd.MM..yyyy");

        this.dateTime = ft.format(dNow);

    }

    // getters and setters

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public boolean isTeamOnly() {
        return teamOnly;
    }
}
