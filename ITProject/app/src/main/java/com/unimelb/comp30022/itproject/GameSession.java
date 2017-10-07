package com.unimelb.comp30022.itproject;

import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class GameSession {

    public static final Integer MAX_TEAMS_2 = 2;
    public static final Integer TEAM_CAPTURING = 0;
    public static final Integer TEAM_ESCAPING = 1;
    private static final double EARTH_RADIUS_M = 6372797.560;
    private static final double RAD_IN_DEGREE = 0.017453292519943295769236907684886;
    private String sessionId;
    private Long startTime;
    private Long endTime;
    private Integer maxPlayers;
    private Long duration;
    private Boolean gameStarted;
    private Boolean gameCompleted;
    private Boolean isPublicAccess;
    private LatLng location;
    private Integer gameRadius;
    private Long timeSessionCreated;
    private Player creator;
    private String sessionName;
    private String description;
    private String sessionImageUri;
    private ArrayList<Team> teamArrayList ;
    public GameSession()
    {
        this.timeSessionCreated = System.currentTimeMillis();
        this.teamArrayList = new ArrayList<Team>();
    }

    public static double distanceBetweenPoints(LatLng origin, LatLng dest) {
        //havesine formula from http://www.movable-type.co.uk/scripts/latlong.html
        ///calculate distance
        double dLat = (dest.latitude - origin.latitude) * RAD_IN_DEGREE;
        double dLong = (dest.longitude - origin.longitude) * RAD_IN_DEGREE;
        double latL = Math.sin(dLat * 0.5) * Math.sin(dLat * 0.5);
        double longL = Math.sin(dLong * 0.5) * Math.sin(dLong * 0.5);
        double tmp = Math.cos(dest.latitude * RAD_IN_DEGREE) *
                Math.cos(origin.latitude * RAD_IN_DEGREE);
        double dist = EARTH_RADIUS_M * 2.0 * Math.asin(Math.sqrt(latL + tmp * longL));
        return dist;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
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

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getTimeSessionCreated() {
        return timeSessionCreated;
    }

    public void setTimeSessionCreated(Long timeSessionCreated) {
        this.timeSessionCreated = timeSessionCreated;
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

    public Boolean getPublicAccess() {
        return isPublicAccess;
    }

    public void setPublicAccess(Boolean publicAccess) {
        isPublicAccess = publicAccess;
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

    public String getSessionImageUri() {
        return sessionImageUri;
    }

    public void setSessionImageUri(String sessionImageUri) {
        this.sessionImageUri = sessionImageUri;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }


    public ArrayList<Team> getTeamArrayList() {
        return teamArrayList;
    }

    public void setTeamArrayList(ArrayList<Team> teamArrayList) {
        this.teamArrayList = teamArrayList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameSession)) return false;

        GameSession that = (GameSession) o;

        return getSessionId().equals(that.getSessionId());

    }

    @Override
    public int hashCode() {
        return getSessionId().hashCode();
    }

    private boolean addTeam(Team team) {
        if (MAX_TEAMS_2 > teamArrayList.size()) {
            this.teamArrayList.add(team);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean removeTeam(Team team){
        if (teamArrayList != null && teamArrayList.size() > 0 && teamArrayList.contains(team)) {
            teamArrayList.remove(team);
            return true;
        }
        return false;
    }

    public void add2Teams(String gameSessionId, Player player){
        for(int i = 0;i<MAX_TEAMS_2;i++){
            //create 2 opposign teams
            this.addTeam(new Team(gameSessionId + "team_" + String.valueOf(i),
                    "team_" + new Integer(i).toString(), new Boolean(i == TEAM_CAPTURING), player));
        }
    }
    public void addPlayerToCapturingTeam(Player player) {
        this.teamArrayList.get(TEAM_CAPTURING).addPlayer(player);
    }

    public void addPlayerToEscapingTeam(Player player) {
        this.teamArrayList.get(TEAM_ESCAPING).addPlayer(player);
    }

    public void removePlayerFromCapturingTeam(Player player) {
        this.teamArrayList.get(TEAM_CAPTURING).removePlayer(player);
    }

    public void removePlayerFromEscapingTeam(Player player) {
        this.teamArrayList.get(TEAM_ESCAPING).removePlayer(player);
    }

    public void updateRelativeLocations(LatLng originPlayerLocation){
        //assuming the player_location is the origin
        for(Team team : teamArrayList){
            for(Player player: team.getPlayerArrayList()) {
                //generate relative coordinate from origin
                player.setCoordinateLocation(convertToCartesian(originPlayerLocation, player.getAbsLocation()));
            }
        }
    }

    public void clearRelativeLocations(){
        for(Team team : teamArrayList){
            for(Player player: team.getPlayerArrayList()){
                player.setCoordinateLocation(null);
            }
        }
    }

    //converts LatLng to x,y,z positions for use in generation of relative positions and vectors
    public CoordinateLocation convertToCartesian(LatLng origin, LatLng dest){
        double dist = distanceBetweenPoints(origin, dest);
        double xComponent = dest.latitude-origin.latitude;
        double zComponent = dest.longitude-origin.longitude;
        double magnitude = Math.sqrt(xComponent*xComponent + zComponent*zComponent);
        CoordinateLocation relativeLoc = new CoordinateLocation(dist*(xComponent/magnitude),0.0,dist*(zComponent/magnitude));
        return relativeLoc;

    }


}
