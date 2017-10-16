package com.unimelb.comp30022.itproject;

import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class Team {
    private String teamId;
    private String teamName;
    private Long timeTeamCreated;
    private String creator;
    private Integer maxPlayers;
    private Integer numPlayers;
    private Boolean isActive;
    private Boolean isCapturing;
    private String teamImageUri;
    private ArrayList<Player> playerArrayList;

    public Team(String teamId, String teamName, Boolean isCapturing, String creator) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.creator = creator;
        this.timeTeamCreated = System.currentTimeMillis();
        this.isActive = true;
        this.isCapturing = isCapturing;
        playerArrayList = new ArrayList<Player>();
    }

    public Team(String teamId) {
        this.teamId = teamId;
        playerArrayList = new ArrayList<Player>();
    }
    public Team (){
        this.timeTeamCreated = System.currentTimeMillis();
        playerArrayList = new ArrayList<Player>();

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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Boolean getCapturing() {
        return isCapturing;
    }

    public void setCapturing(Boolean capturing) {
        isCapturing = capturing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;

        Team team = (Team) o;

        return getTeamId().equals(team.getTeamId());

    }

    @Override
    public int hashCode() {
        return getTeamId().hashCode();
    }
}
