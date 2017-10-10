package com.unimelb.comp30022.itproject;


import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Kiptenai on 20/09/2017.
 */

public class GameSession {

    public static final Integer MAX_TEAMS_2 = 2;
    public static final Integer TEAM_CAPTURING = 0;
    public static final Integer TEAM_ESCAPING = 1;
    private static final double EARTH_RADIUS_M = 6372797.560;
    private static final double RAD_TO_DEGREE = 0.017453292519943295769236907684886;
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
    private String creator;
    private String sessionName;
    private String description;
    private String sessionImageUri;
    private Float bearing;
    private ArrayList<Team> teamArrayList ;
    public GameSession()
    {
        this.timeSessionCreated = System.currentTimeMillis();
        this.teamArrayList = new ArrayList<Team>();
        this.gameStarted = false;
        this.gameCompleted = false;
        this.isPublicAccess = true;
    }

    public static boolean containsPlayer(GameSession gameSession, Player player) {
        for (int i = 0; i < MAX_TEAMS_2; i++) {
            if (gameSession.getTeamArrayList().get(i).getPlayerArrayList().contains(player)) {
                return true;
            }
        }
        return false;
    }

    public static double distanceBetweenPoints(LatLng origin, LatLng dest) {
        //havesine formula from http://www.movable-type.co.uk/scripts/latlong.html
        ///calculate distance
        double dLat = (dest.getLatitude() - origin.getLatitude()) * RAD_TO_DEGREE;
        double dLong = (dest.getLongitude() - origin.getLongitude()) * RAD_TO_DEGREE;
        double latL = Math.sin(dLat * 0.5) * Math.sin(dLat * 0.5);
        double longL = Math.sin(dLong * 0.5) * Math.sin(dLong * 0.5);
        double tmp = Math.cos(dest.getLatitude() * RAD_TO_DEGREE) *
                Math.cos(origin.getLatitude() * RAD_TO_DEGREE);
        double dist = EARTH_RADIUS_M * 2.0 * Math.asin(Math.sqrt(latL + tmp * longL));
        return dist;
    }

    //converts LatLng to x,y,z positions for use in generation of relative positions and vectors
    //origin describes the point of reference, dest is the point who's relative position we want
    public static CoordinateLocation convertToCartesian(LatLng origin, LatLng dest) {
        CoordinateLocation relativeLoc;
        if (origin == null || dest == null) {
            return null;
        }
        double dist = distanceBetweenPoints(origin, dest);
        double xComponent = dest.getLatitude() - origin.getLatitude();
        double zComponent = dest.getLongitude() - origin.getLongitude();
        double magnitude = Math.sqrt(xComponent * xComponent + zComponent * zComponent);

        if (magnitude == 0.0) {
            relativeLoc = new CoordinateLocation(0, 0.0, 0.0);

        } else {
            relativeLoc = new CoordinateLocation(dist * (xComponent / magnitude), 0.0, dist * (zComponent / magnitude));

        }
        return relativeLoc;

    }

    public static double distanceBetweenTwoPlayers(Player player1, Player player2) {
        double dist = distanceBetweenPoints(player1.getAbsLocation(), player2.getAbsLocation());
        return dist;
    }

    public static boolean canCapturePlayer(Player chaser, Player unknown) {
        //valid capturing individual
        if (chaser.getLoggedOn() && chaser.getActive() && chaser.getCapturing()) {
            if (unknown.getLoggedOn() && unknown.getActive() && !chaser.getCapturing()) {
                return true;
            }
        }
        return false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
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

    public Float getBearing() {
        return bearing;
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }

    public ArrayList<Team> getTeamArrayList() {
        return teamArrayList;
    }

    public void setTeamArrayList(ArrayList<Team> teamArrayList) {
        this.teamArrayList = teamArrayList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) return true;
        if (!(o instanceof GameSession)) return false;

        GameSession that = (GameSession) o;

        return getSessionId().equals(that.getSessionId());

    }

    @Override
    public int hashCode() {
        return getSessionId().hashCode();
    }

    public boolean addTeam(Team team) {
        if (MAX_TEAMS_2 > teamArrayList.size()) {
            this.teamArrayList.add(team);
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Player> getPlayersWithinDistance(Player player, Double distance) {
        ArrayList<Player> closePlayers = new ArrayList<Player>();
        for (Team team : this.getTeamArrayList()) {
            for (Player p : team.getPlayerArrayList()) {
                double dist = distanceBetweenTwoPlayers(player, p);
                if (dist < distance) {
                    closePlayers.add(p);
                }
            }
        }
        return closePlayers;
    }

    public void capturePlayer(Player capturing, Player captured) {
        capturing.getCapturedList().add(captured.getDisplayName());
        captured.setCapturedBy(capturing.getDisplayName());
        captured.setCapturing(true);
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
                    "team_" + new Integer(i).toString(), new Boolean(i == TEAM_CAPTURING), player.getDisplayName()));
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
                int pathSize = player.getPath().size();
                if (pathSize > 0) {
                    player.getRelativePath().clear();
                    for (int i = 0; i < pathSize; i++) {
                        player.getRelativePath().add(convertToCartesian(player.getAbsLocation(), player.getPath().get(i)));
                    }
                }
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

    public void updatePlayerLocation(Player player, LatLng latLng) {
        if (player == null || latLng == null) {
            return;
        }
        for (int i = 0; i < MAX_TEAMS_2; i++) {
            int pos;
            Team team = teamArrayList.get(i);
            if (team.containsPlayer(player)) {
                pos = team.getPlayerArrayList().indexOf(player);
                Log.d("updatePlayerLocation", "updating player" + team.getPlayerArrayList().get(pos).getDisplayName());
                team.getPlayerArrayList().get(pos).setAbsLocation(latLng);
            }
        }
    }

    public void updatePaths(int maxSteps) {
        for (Team team : this.teamArrayList) {
            for (Player player : team.getPlayerArrayList()) {
                ArrayList<LatLng> path = player.getPath();
                if (path.size() >= maxSteps) {
                    //clear single element and update latest
                    path.remove(0);
                    path.add(player.getAbsLocation());
                } else {
                    path.add(player.getAbsLocation());
                }
            }
        }

    }

    public ArrayList<Player> getAllPlayerInformation() {
        //concatenate player arraylists
        ArrayList<Player> allTeams = new ArrayList<Player>();
        ArrayList<Player> team1 = this.getTeamArrayList().get(0).getPlayerArrayList();
        ArrayList<Player> team2 = this.getTeamArrayList().get(1).getPlayerArrayList();
        allTeams.addAll(team1);
        allTeams.addAll(team2);
        return allTeams;
    }

    public Player getPlayerDetails(String displayname) {
        for (Player player : this.getAllPlayerInformation()) {
            if (player.equals(new Player(displayname))) {
                return player;
            }

        }
        return null;

    }




}
