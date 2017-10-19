package com.unimelb.comp30022.itproject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kiptenai on 20/09/2017.
 */

public class GameSession {

    public static final Integer MAX_TEAMS_2 = 2;
    public static final Integer TEAM_CAPTURING = 0;
    public static final Integer TEAM_ESCAPING = 1;
    public static final int HARD_CAPTURE_DISTANCE = 5;
    public static final int EASY_CAPTURE_DISTANCE = 10;
    public static final int NUMBER_OF_FOOTSTEPS = 15;
    private static final double EARTH_RADIUS_M = 6372797.560;
    private static final double RAD_TO_DEGREE = 0.017453292519943295769236907684886;
    private String sessionId;
    private String startTimeString;
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
    private boolean easyMode;
    private ArrayList<Team> teamArrayList ;
    public GameSession()
    {
        this.timeSessionCreated = System.currentTimeMillis();
        this.teamArrayList = new ArrayList<Team>();
        this.gameStarted = false;
        this.gameCompleted = false;
        this.isPublicAccess = true;
    }

    public static ArrayList<Player> determineCapturedPlayersBetweenSessions(GameSession mySession, GameSession publicSession) {
        ArrayList<Player> newlyCapturedPlayers = new ArrayList<>();
        for (Player localPlayerCopy : mySession.allPlayerArrayLists()) {
            if (publicSession.getPlayerDetails(localPlayerCopy.getDisplayName()).getCapturing() != localPlayerCopy.getCapturing()) {
                newlyCapturedPlayers.add(localPlayerCopy);
            }
        }
        return newlyCapturedPlayers;
    }

    public static boolean containsPlayer(GameSession gameSession, Player player) {
        return gameSession.fetchCapturingTeam().containsPlayer(player) ||
                gameSession.fetchEscapingTeam().containsPlayer(player);
    }

    public static double distanceBetweenPoints(LatLng origin, LatLng dest) {
        //havesine formula from http://www.movable-type.co.uk/scripts/latlong.html
        ///calculate distance
        if (origin == null || dest == null) {
            return Double.MAX_VALUE;
        }
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
            relativeLoc = new CoordinateLocation(0, 0.0, 0.0, 0);

        } else {
            relativeLoc = new CoordinateLocation(dist * (xComponent / magnitude), 0.0, dist * (zComponent / magnitude), dest.getAccuracy());
        }
        return relativeLoc;

    }

    public static double distanceBetweenTwoPlayers(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            return Double.MAX_VALUE;
        }
        double dist = distanceBetweenPoints(player1.getAbsLocation(), player2.getAbsLocation());
        return dist;
    }

    public static ArrayList<Player>
    determineIndividualsCapturedFromUpdate(GameSession local, GameSession server) {
        //no difference
        if (local.getCapturedList().size() == server.getCapturedList().size()) {
            return null;
        }
        ArrayList<Player> newCaptures = new ArrayList<>();
        ArrayList<Player> mySession = local.allPlayerArrayLists();
        ArrayList<Player> publicSession = server.allPlayerArrayLists();
        for (Player player : mySession) {
            if (publicSession.get(publicSession.indexOf(player)).getCapturing() != mySession.get(mySession.indexOf(player)).getCapturing()) {
                newCaptures.add(player);
            }
        }
        return newCaptures;
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

    public String getStartTimeString() {
        return startTimeString;
    }

    public void setStartTimeString(String startTimeString) {
        this.startTimeString = startTimeString;
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

    public boolean isEasyMode() {
        return easyMode;
    }

    public void setEasyMode(boolean easyMode) {
        this.easyMode = easyMode;
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

    public int currentPlayerCount() {
        return this.fetchCapturingTeam().getNumPlayers() + this.fetchEscapingTeam().getNumPlayers();
    }

    public int availableSpaces() {
        return this.maxPlayers - this.currentPlayerCount();
    }

    public Team fetchEscapingTeam() {
        return this.teamArrayList.get(GameSession.TEAM_ESCAPING);
    }

    public Team fetchCapturingTeam() {
        return this.teamArrayList.get(GameSession.TEAM_CAPTURING);
    }

    public boolean addPlayerToCapturingTeam(Player player) {
        if (this.availableSpaces() > 0) {
            this.teamArrayList.get(TEAM_CAPTURING).addPlayer(player);
            return true;
        }
        return false;
    }

    public boolean addPlayerToEscapingTeam(Player player) {
        if (this.availableSpaces() > 0) {
            this.teamArrayList.get(TEAM_ESCAPING).addPlayer(player);
            return true;
        }
        return false;
    }

    public boolean removePlayerFromCapturingTeam(Player player) {
        if (this.fetchCapturingTeam().containsPlayer(player)) {
            this.fetchCapturingTeam().removePlayer(player);
            return true;
        }
        return false;
    }

    public boolean removePlayerFromEscapingTeam(Player player) {
        if (this.fetchEscapingTeam().containsPlayer(player)) {
            this.fetchEscapingTeam().removePlayer(player);
            return true;
        }
        return false;
    }

    public int getTeamIndex(Player player) {
        if (this.fetchCapturingTeam().containsPlayer(player)) {
            return GameSession.TEAM_CAPTURING;
        } else if (this.fetchEscapingTeam().containsPlayer(player)) {
            return GameSession.TEAM_ESCAPING;
        }
        return -1;
    }

    public int getPlayerIndexInTeam(Player player) {
        int teamId = getTeamIndex(player);
        return this.getTeamArrayList().get(teamId).getPlayerArrayList().indexOf(player);
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
                if (p.getAbsLocation() != null) {
                    double dist = distanceBetweenTwoPlayers(player, p);
                    if (dist < distance) {
                        closePlayers.add(p);
                    }
                }

            }
        }
        return closePlayers;
    }

    public int capturingCount() {
        int count = 0;
        for (Team team : this.getTeamArrayList()) {
            for (Player p : team.getPlayerArrayList()) {
                if (p.getCapturing() == true) {
                    count++;
                }
            }
        }
        return count;
    }

    public int capturedCount() {
        int count = 0;
        for (Team team : this.getTeamArrayList()) {
            for (Player p : team.getPlayerArrayList()) {
                if (p.getHasBeenCaptured() == true) {
                    count++;
                }
            }
        }
        return count;
    }

    public void refreshActivePlayers(long currentTime, long maxAllowedInactivetime) {
        for (Team team : this.getTeamArrayList()) {
            for (Player p : team.getPlayerArrayList()) {
                if ((currentTime - maxAllowedInactivetime) > p.getLastPing()) {
                    p.setActive(false);
                } else {
                    p.setActive(true);
                }
            }
        }
    }

    public long getRemainingTime(long currentTime) {
        return this.getEndTime() - currentTime;
    }

    public boolean isGameOver(long currentTime) {
        //no time left
        if (this.capturingCount() == this.getMaxPlayers() || this.getRemainingTime(currentTime) <= 0) {
            this.setGameCompleted(true);
            return true;
        }
        this.setGameCompleted(false);
        return false;
    }

    public ArrayList<Player> getCapturedList() {
        ArrayList<Player> capturedList = new ArrayList<>();
        for (Player player : this.allPlayerArrayLists()) {
            if (player.getCapturing() == true && player.getHasBeenCaptured() == true) {
                capturedList.add(player);
            }
        }
        return capturedList;
    }

    public ArrayList<Player> fetchCapturingList() {
        ArrayList<Player> capturedList = new ArrayList<>();
        for (Player player : this.allPlayerArrayLists()) {
            if (player.getCapturing() == true) {
                capturedList.add(player);
            }
        }
        return capturedList;
    }

    public boolean capturePlayer(Player capturing, Player captured) {
        captured = this.getPlayerDetails(captured.getDisplayName());
        capturing = this.getPlayerDetails(capturing.getDisplayName());
        if (captured.getCapturing() == true) {
            return false;
        }
        if (!capturing.getPlayerCapturedList().contains(captured.getDisplayName())) {
            capturing.getPlayerCapturedList().add(captured.getDisplayName());
            captured.setCapturedBy(capturing.getDisplayName());
            captured.setCapturing(true);
            captured.setHasBeenCaptured(true);

            return true;
        }
        return false;
    }

    public boolean removeTeam(Team team) {
        if (teamArrayList != null && teamArrayList.size() > 0 && teamArrayList.contains(team)) {
            teamArrayList.remove(team);
            return true;
        }
        return false;
    }

    public void add2Teams(String gameSessionId, Player player) {
        for (int i = 0; i < MAX_TEAMS_2; i++) {
            //create 2 opposign teams
            this.addTeam(new Team("team_" + String.valueOf(i + 1),
                    "team_" + new Integer(i + 1).toString(), new Boolean(i == TEAM_CAPTURING), player.getDisplayName()));
        }
    }

    public void updateRelativeLocations(LatLng originPlayerLocation){
        //assuming the player_location is the origin
        for(Team team : teamArrayList){
            for(Player player: team.getPlayerArrayList()) {
                //generate relative coordinate from origin
                if (player.getAbsLocation() != null) {
                    player.setCoordinateLocation(convertToCartesian(originPlayerLocation, player.getAbsLocation()));
                    //update relative coordinates of the footsteps
                    int pathSize = player.getPath().size();
                    if (pathSize > 0) {
                        player.getRelativePath().clear();
                        for (int i = 0; i < pathSize; i++) {
                            player.getRelativePath().add(convertToCartesian(originPlayerLocation, player.getPath().get(i)));
                        }
                    }
                }

            }
        }
    }

    public void clearRelativeLocations(){
        for(Team team : teamArrayList){
            for(Player player: team.getPlayerArrayList()){
                player.setCoordinateLocation(null);
                player.getRelativePath().clear();
            }
        }

    }

    public boolean updatePlayerLocation(Player player, LatLng latLng) {
        if (player == null || latLng == null) {
            return false;
        }
        int pos;
        ArrayList<Player> capturing = this.fetchCapturingTeam().getPlayerArrayList();
        ArrayList<Player> escaping = this.fetchEscapingTeam().getPlayerArrayList();
        if (capturing.contains(player)) {
            pos = capturing.indexOf(player);
            capturing.get(pos).setAbsLocation(latLng);
            return true;
        } else if (escaping.contains(player)) {
            pos = escaping.indexOf(player);
            escaping.get(pos).setAbsLocation(latLng);
            return true;
        }

        return false;
    }

    public void updatePaths(int maxSteps) {
        for (Team team : this.teamArrayList) {
            for (Player player : team.getPlayerArrayList()) {
                ArrayList<LatLng> path = player.getPath();
                if (path.size() >= maxSteps) {
                    //clear single element and update latest
                    path.remove(0);
                }
                path.add(player.getAbsLocation());
            }
        }

    }

    public ArrayList<Player> allPlayerArrayLists() {
        //concatenate player arraylists
        ArrayList<Player> allTeams = new ArrayList<Player>();
        ArrayList<Player> team1 = this.getTeamArrayList().get(0).getPlayerArrayList();
        ArrayList<Player> team2 = this.getTeamArrayList().get(1).getPlayerArrayList();
        allTeams.addAll(team1);
        allTeams.addAll(team2);
        return allTeams;
    }

    public Player getPlayerDetails(String displayname) {
        ArrayList<Player> players = this.allPlayerArrayLists();
        Player newPlayer = new Player(displayname);
        if (players.contains(newPlayer)) {
            return players.get(players.indexOf(newPlayer));
        } else {
            return null;
        }
    }


}
