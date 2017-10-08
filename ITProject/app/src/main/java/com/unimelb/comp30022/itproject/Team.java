package com.unimelb.comp30022.itproject;

import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class Team {
    private String teamId;
    private String teamName;
    private Long timeTeamCreated;
    private Player creator;
    private Integer maxPlayers;
    private Integer numPlayers;
    private Boolean isActive;
    private Boolean isCapturing;
    private String teamImageUri;
    private ArrayList<Player> playerArrayList = new ArrayList<Player>();
    public  Team(String teamId, String teamName,Boolean isCapturing ,Player creator){
        this.teamId = teamId;
        this.teamName = teamName;
        this.creator = creator;
        this.timeTeamCreated = System.currentTimeMillis();
        this.isActive = true;
        this.isCapturing = isCapturing;
    }
    public Team (){
        this.timeTeamCreated = System.currentTimeMillis();

    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    //add players if space is available
    public boolean addPlayer(Player player){
        if( maxPlayers != null && maxPlayers > playerArrayList.size() ){
            if (playerArrayList == null) {
                this.playerArrayList = new ArrayList<Player>();
            }
            this.playerArrayList.add(player);
            this.numPlayers = new Integer(this.playerArrayList.size());
            return true;
        }
        else{
            return false;
        }
    }

    public boolean removePlayer(Player player){
        if (this.playerArrayList != null && this.playerArrayList.size() > 0 && this.playerArrayList.contains(player)) {
            this.playerArrayList.remove(player);
            this.numPlayers = new Integer(this.playerArrayList.size());
            return true;
        }
        return false;
    }

    public boolean containsPlayer(Player player) {
        return playerArrayList.contains(player);
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTimeTeamCreated() {
        return timeTeamCreated;
    }

    public void setTimeTeamCreated(Long timeTeamCreated) {
        this.timeTeamCreated = timeTeamCreated;
    }

    public String getTeamImageUri() {
        return teamImageUri;
    }

    public void setTeamImageUri(String teamImageUri) {
        this.teamImageUri = teamImageUri;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public ArrayList<Player> getPlayerArrayList() {
        return playerArrayList;
    }

    public void setPlayerArrayList(ArrayList<Player> playerArrayList) {
        this.playerArrayList = playerArrayList;
    }

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public Boolean getCapturing() {
        return isCapturing;
    }

    public void setCapturing(Boolean capturing) {
        isCapturing = capturing;
    }

}
