package com.unimelb.comp30022.itproject;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class Team {
    private Integer teamId;
    private String teamName;
    private Long timeCreated;
    private Player creator;
    private Integer maxPlayers;
    private Boolean isActive;
    private URI teamImageUri;
    private ArrayList<Player> activePlayers;
    public  Team(Integer teamId, String teamName,Player creator){
        this.teamId = teamId;
        this.teamName = teamName;
        this.creator = creator;
        this.activePlayers = new ArrayList<Player>();
        this.timeCreated = System.currentTimeMillis();
    }
    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    //add players if space is available
    private boolean addPlayer(Player player){
        if(activePlayers != null && maxPlayers!=null){
            if(maxPlayers.compareTo(activePlayers.size())<0){
                this.activePlayers.add(player);
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
    private boolean removePlayer(Player player){
        if(activePlayers != null && activePlayers.size()>0 && activePlayers.contains(player)){
            activePlayers.remove(player);
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
