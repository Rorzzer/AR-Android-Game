package com.unimelb.comp30022.itproject;


import com.google.android.gms.maps.model.LatLng;

import java.net.URI;

/**
 * Created by Kiptenai on 20/09/2017.
 */
/**
 * Class to mediate player interactions and data
 * */
public class Player  {
    private Integer  playerId;
    private String displayName;
    private LatLng absLocation;
    private RelLocation relLocation;
    private Boolean isLoggedOn;
    private Long lastLoggedOn;
    private String imageUri;
    private Integer score;
    private String teamName;
    private Integer teamId;
    private Long lastPing;
    private Integer skillLevel;
    private Boolean isActive;
    //mutator & acessor methods
    public Player(Integer playerId , String displayName){
        this.playerId = playerId;
        this.displayName = displayName;
    }

    //mutators and getters to determine player information
    public Boolean isLoggedOn(){
        if(isLoggedOn!= null){
            return isLoggedOn;
        }
        return false;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LatLng getAbsLocation() {
        return absLocation;
    }

    public void setAbsLocation(LatLng absLocation) {
        this.absLocation = absLocation;
    }

    public RelLocation getRelLocation() {
        return relLocation;
    }

    public void setRelLocation(RelLocation relLocation) {
        this.relLocation = relLocation;
    }

    public Boolean getLoggedOn() {
        return isLoggedOn;
    }

    public void setLoggedOn(Boolean loggedOn) {
        isLoggedOn = loggedOn;
    }

    public Long getLastLoggedOn() {
        return lastLoggedOn;
    }

    public void setLastLoggedOn(Long lastLoggedOn) {
        this.lastLoggedOn = lastLoggedOn;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
    public Boolean hasImageUri(){
        if(imageUri != null){
            return true;
        }
        return false;
    }
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Long getLastPing() {
        return lastPing;
    }

    public void setLastPing(Long lastPing) {
        this.lastPing = lastPing;
    }

    public Integer getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;

        Player player = (Player) o;

        return getPlayerId().equals(player.getPlayerId());

    }

    @Override
    public int hashCode() {
        return 0;
    }

}
