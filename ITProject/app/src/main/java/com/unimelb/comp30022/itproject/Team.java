package com.unimelb.comp30022.itproject;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class Team {
    private Integer teamId;
    private String teamName;
    private Long timeTeamCreated;
    private Player creator;
    private Integer maxPlayers;
    private Boolean isActive;
    private URI teamImageUri;
    private ArrayList<Player> playerArrayList;
    public  Team(Integer teamId, String teamName,Player creator){
        this.teamId = teamId;
        this.teamName = teamName;
        this.creator = creator;
        this.playerArrayList = new ArrayList<Player>();
        this.timeTeamCreated = System.currentTimeMillis();
    }
    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    //add players if space is available
    public boolean addPlayer(Player player){
        if(playerArrayList != null && maxPlayers != null  ){
            if(maxPlayers>playerArrayList.size()){
                this.playerArrayList.add(player);
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
    public boolean removePlayer(Player player){
        if(playerArrayList != null && playerArrayList.size()>0 && playerArrayList.contains(player)){
            playerArrayList.remove(player);
            return true;
        }
        return false;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
