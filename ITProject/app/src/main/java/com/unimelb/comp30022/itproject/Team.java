package com.unimelb.comp30022.itproject;

import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 * Holds and manages team and Player data for members in the current game
 */

public class Team {
    private String teamId;
    private String teamName;
    private Long timeTeamCreated;
    private String creator;
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
        this.numPlayers = 0;
        this.isCapturing = isCapturing;
        playerArrayList = new ArrayList<Player>();
    }

    public Team(String teamId) {
        this.timeTeamCreated = System.currentTimeMillis();
        this.teamId = teamId;
        this.numPlayers = 0;
        playerArrayList = new ArrayList<Player>();
    }
    public Team (){
        this.numPlayers = 0;
        this.timeTeamCreated = System.currentTimeMillis();
        playerArrayList = new ArrayList<Player>();

    }
    //basic mutator and acessor methods
    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
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

    public int getTeamSize() {
        return this.playerArrayList.size();
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(Integer numPlayers) {
        this.numPlayers = numPlayers;
    }

    /**
     * add players if space is available
     * @param player the individual to be added to the team
     * @return whether the addition was successful or not
     */

    public boolean addPlayer(Player player){
        if (this.playerArrayList.contains(player)) {
            return false;
        }
        this.playerArrayList.add(player);
        this.numPlayers = new Integer(this.playerArrayList.size());
        return true;
    }
    /***
     * removes an individual from the team
     * @param player individual to be removed
     * @return whether the removal was successful or not
     * */

    public boolean removePlayer(Player player){
        if (this.playerArrayList.size() > 0 && this.playerArrayList.contains(player)) {
            this.playerArrayList.remove(player);
            this.numPlayers = new Integer(this.playerArrayList.size());
            return true;
        }
        return false;
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
