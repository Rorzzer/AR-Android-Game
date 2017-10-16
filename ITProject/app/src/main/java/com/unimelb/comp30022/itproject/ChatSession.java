package com.unimelb.comp30022.itproject;

/**
 * Created by RoryPowell on 3/10/17.
 */

public class ChatSession {

    private Integer sessionId;


    public ChatSession() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public ChatSession(Integer GameSessionID){
        sessionId = GameSessionID;

    }


    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }
}
