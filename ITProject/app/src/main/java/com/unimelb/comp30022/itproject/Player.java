package com.unimelb.comp30022.itproject;


/**
 * Created by Kiptenai on 20/09/2017.
 * used to contain player identification information and variables as
 * part of the current game state
 */

import java.util.ArrayList;

public class Player  {
    private String displayName;
    private LatLng absLocation;
    private CoordinateLocation coordinateLocation;
    private Boolean isLoggedOn;
    private Long lastLoggedOn;
    private String imageUri;
    private Integer score;
    private String assignedTeamName;
    private String teamId;
    private Long lastPing;
    private Integer skillLevel;
    private Boolean isActive;
    private Boolean isCapturing;
    private Boolean hasBeenCaptured;
    private String capturedBy;
    private ArrayList<String> capturedList;
    private ArrayList<LatLng> path;
    private ArrayList<CoordinateLocation> relativePath;
    public Player( String displayName){
        this.displayName = displayName;
        this.isLoggedOn = false;
        this.score = 0;
        this.skillLevel = 0;
        this.isActive = false;
        this.isCapturing = false;
        capturedList = new ArrayList<String>();
        path = new ArrayList<LatLng>();
        relativePath = new ArrayList<CoordinateLocation>();
    }
    public Player() {
        this.isLoggedOn = false;
        this.score = 0;
        this.skillLevel = 0;
        this.isActive = false;
        this.isCapturing = false;
        capturedList = new ArrayList<String>();
        path = new ArrayList<LatLng>();
        relativePath = new ArrayList<CoordinateLocation>();
    }
    //mutator & acessor methods

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

    public CoordinateLocation getCoordinateLocation() {
        return coordinateLocation;
    }

    public void setCoordinateLocation(CoordinateLocation coordinateLocation) {
        this.coordinateLocation = coordinateLocation;
    }
    public Boolean getLoggedOn() {
        return isLoggedOn;
    }

    public void setLoggedOn(boolean loggedOn) {
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
        return imageUri != null;
    }
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getAssignedTeamName() {
        return assignedTeamName;
    }

    public void setAssignedTeamName(String assignedTeamName) {
        this.assignedTeamName = assignedTeamName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
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

    public boolean getActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Boolean getCapturing() {
        return isCapturing;
    }

    public void setCapturing(boolean capturing) {
        isCapturing = capturing;
    }

    public Boolean getHasBeenCaptured() {
        return hasBeenCaptured;
    }

    public void setHasBeenCaptured(boolean hasBeenCaptured) {
        this.hasBeenCaptured = hasBeenCaptured;
    }

    public ArrayList<String> getPlayerCapturedList() {
        return capturedList;
    }

    public ArrayList<LatLng> getPath() {
        return path;
    }

    public void setPath(ArrayList<LatLng> path) {
        this.path = path;
    }

    public ArrayList<CoordinateLocation> getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(ArrayList<CoordinateLocation> relativePath) {
        this.relativePath = relativePath;
    }

    public String getCapturedBy() {
        return capturedBy;
    }

    public void setCapturedBy(String capturedBy) {
        this.capturedBy = capturedBy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;

        Player player = (Player) o;

        return displayName.equals(player.getDisplayName());

    }

    @Override
    public int hashCode() {
        return 0;
    }



}
