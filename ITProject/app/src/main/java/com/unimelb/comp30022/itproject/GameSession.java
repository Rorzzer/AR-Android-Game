package com.unimelb.comp30022.itproject;

import com.google.android.gms.maps.model.LatLng;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class GameSession {



    private Integer sessionId;
    private Long startTime;
    private Long endtime;
    private Integer maxTeams;
    private Integer maxPlayers;
    private Long duration;
    private Boolean gameStarted;
    private Boolean gameCompleted;
    private LatLng location;
    private Integer gameRadius;
    private Player creator;
    private String description;
    private URI sessionImageUri;
    private ArrayList<Team> teamArrayList ;
    public GameSession(){
        teamArrayList = new ArrayList<Team>();
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndtime() {
        return endtime;
    }

    public void setEndtime(Long endtime) {
        this.endtime = endtime;
    }

    public Integer getMaxTeams() {
        return maxTeams;
    }

    public void setMaxTeams(Integer maxTeams) {
        this.maxTeams = maxTeams;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Boolean getGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(Boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public Boolean getGameCompleted() {
        return gameCompleted;
    }

    public void setGameCompleted(Boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Integer getGameRadius() {
        return gameRadius;
    }

    public void setGameRadius(Integer gameRadius) {
        this.gameRadius = gameRadius;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URI getSessionImageUri() {
        return sessionImageUri;
    }

    public void setSessionImageUri(URI sessionImageUri) {
        this.sessionImageUri = sessionImageUri;
    }

    public boolean addTeam(Team team){
        if(teamArrayList != null && maxTeams != null  ){
            if(maxTeams>teamArrayList.size()){
                this.teamArrayList.add(team);
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
    public boolean removePlayer(Team team){
        if(teamArrayList != null && teamArrayList.size()>0 && teamArrayList.contains(team)){
            teamArrayList.remove(team);
            return true;
        }
        return false;
    }


}
