package com.unimelb.comp30022.itproject;


/**
 * Created by Kiptenai on 20/09/2017.
 */
/**
 * Class to mediate player interactions and data
 * */
public class Player  {
    private String displayName;
    private LatLng absLocation;
    private CoordinateLocation coordinateLocation;
    private Boolean isLoggedOn;
    private Long lastLoggedOn;
    private String imageUri;
    private Integer score;
    private String teamName;
    private Integer teamId;
    private Long lastPing;
    private Integer skillLevel;
    private Boolean isActive;
    private Boolean isCapturing;
    //mutator & acessor methods
    public Player( String displayName){
        this.displayName = displayName;
    }

    public Player(){}
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

    public Boolean getCapturing() {
        return isCapturing;
    }

    public void setCapturing(Boolean capturing) {
        isCapturing = capturing;
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
