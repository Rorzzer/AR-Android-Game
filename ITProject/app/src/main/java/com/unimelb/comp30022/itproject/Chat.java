package com.unimelb.comp30022.itproject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by RoryPowell on 5/10/17.
 */

public class Chat {

    private String username;
    //private String gameID;
    //private String messageID;
    private String message;
    private String dateTime;
    private Date date;
    private long time;
    private String team;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Chat(String userName, String message, String Team) {
        this.username = userName;
        //this.gameID = gameID;
        //this.messageID = messageID;
        this.message = message;
        this.date = new Date();
        this.time = date.getTime();

        Date dNow = new Date( );
        SimpleDateFormat ft =
                new SimpleDateFormat ("hh:mm:ss a zzz E dd.MM..yyyy");

        this.dateTime = ft.format(dNow);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

//    public String getGameID() {
//        return gameID;
//    }

//    public void setGameID(String gameID) {
//        this.gameID = gameID;
//    }

//    public String getMessageID() {
//        return messageID;
//    }

//    public void setMessageID(String messageID) {
//        this.messageID = messageID;
//    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
