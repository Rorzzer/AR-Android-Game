package com.unimelb.comp30022.itproject;

import java.util.ArrayList;


/**
 * Created by Kiptenai on 20/09/2017.
 * Class to host all game session informatoin including active teams, players and locations
 * has methods to compare gamesessions, update, remove and fetch members of the session and
 * generate relative location information regarding players given their latitudes and longitudes
 *
 */

public class GameSession {

    public static final Integer MAX_TEAMS_2 = 2;
    public static final Integer TEAM_CAPTURING = 0;
    public static final Integer TEAM_ESCAPING = 1;
    public static final int HARD_CAPTURE_DISTANCE = 5;
    public static final int EASY_CAPTURE_DISTANCE = 10;
    public static final int NUMBER_OF_FOOTSTEPS = 15;
    private static final double EARTH_RADIUS_M = 6372797.560;
    private static final double DEGREE_TO_RAD = 0.017453292519943295769236907684886;
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
    /***
     *
     *
     * Basic mutator and accessor methods
     *
     * */
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

    /**
     * Determines number of individuals in the game
     * @return number of current players
     * */
    public int currentPlayerCount() {
        return this.fetchCapturingTeam().getNumPlayers() + this.fetchEscapingTeam().getNumPlayers();
    }
    /**
    * Determines difference between current players and maximum players
    * @return number of available spaces
    * */
    public int availableSpaces() {
        return this.maxPlayers - this.currentPlayerCount();
    }

    public Team fetchEscapingTeam() {
        return this.teamArrayList.get(GameSession.TEAM_ESCAPING);
    }

    public Team fetchCapturingTeam() {
        return this.teamArrayList.get(GameSession.TEAM_CAPTURING);
    }

    /**
     * compares two gamesessoins and returns the newly captured members that were captured between
     * server updates
     * @param  local the version of the game that is stored locally
     * @param  server version of the game stored on the server
     * @return an arraylist of players that were not yet indicated as captured locally, but were
     * captured between server updates
     * */
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

    /***
     * Determines if the gamesession has the indicated player, based on a comparison of the player's
     * name
     * @param gameSession game to be queried
     * @param  player player object whose name is to bequeried
     * @return  whether the player exists or not in the given gamesession
     * */
    public static boolean containsPlayer(GameSession gameSession, Player player) {
        return gameSession.fetchCapturingTeam().containsPlayer(player) ||
                gameSession.fetchEscapingTeam().containsPlayer(player);
    }
    /**
     * calculates distance in meters between two locations on the globe using the great-cirle
     * havesine formula
     * @param  origin a corrdinate point on the map
     * @param  dest a coordinate point on the map
     * @return  distance in meters between the two points
     * */
    public static double distanceBetweenPoints(LatLng origin, LatLng dest) {
        //havesine formula from http://www.movable-type.co.uk/scripts/latlong.html
        ///calculate distance
        if (origin == null || dest == null) {
            return Double.MAX_VALUE;
        }
        double dLat = (dest.getLatitude() - origin.getLatitude()) * DEGREE_TO_RAD;
        double dLong = (dest.getLongitude() - origin.getLongitude()) * DEGREE_TO_RAD;
        double latL = Math.sin(dLat * 0.5) * Math.sin(dLat * 0.5);
        double longL = Math.sin(dLong * 0.5) * Math.sin(dLong * 0.5);
        double tmp = Math.cos(dest.getLatitude() * DEGREE_TO_RAD) *
                Math.cos(origin.getLatitude() * DEGREE_TO_RAD);
        double dist = EARTH_RADIUS_M * 2.0 * Math.asin(Math.sqrt(latL + tmp * longL));
        return dist;
    }
    /***
     * converts LatLng to x,y,z positions for use in generation of relative positions and vectors
     * origin describes the point of reference, dest is the point who's relative position we want
     * @param origin point of reference
     * @param  dest point whose coorinate we want to know with the origin as a point of reference
     * */
    //
    //
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
    /**
     * Determines distance between two players based on their coordinates
     * @param player1 first player
     * @param  player2 second player
     * @return  distance in meters between both players
     * */
    public static double distanceBetweenTwoPlayers(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            return Double.MAX_VALUE;
        }
        double dist = distanceBetweenPoints(player1.getAbsLocation(), player2.getAbsLocation());
        return dist;
    }
    /**
     * Comparison method using gamesessionIDs
     * @return true or false based on comparison outcome
     * */
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

    /**
     * adds a player to the capturing team
     * @param player individual to be added to current game
     * @return whether or not the player was successfully added
     * */
    public boolean addPlayerToCapturingTeam(Player player) {
        if (this.availableSpaces() > 0) {
            this.teamArrayList.get(TEAM_CAPTURING).addPlayer(player);
            return true;
        }
        return false;
    }
    /**
     * adds a player to the escaping team
     * @param player individual to be added to current game
     * @return whether or not the player was successfully added
     * */
    public boolean addPlayerToEscapingTeam(Player player) {
        if (this.availableSpaces() > 0) {
            this.teamArrayList.get(TEAM_ESCAPING).addPlayer(player);
            return true;
        }
        return false;
    }
    /**
     * removes a player to the capturing team
     * @param player individual to be added to current game
     * @return whether or not the player was successfully removed
     * */
    public boolean removePlayerFromCapturingTeam(Player player) {
        if (this.fetchCapturingTeam().containsPlayer(player)) {
            this.fetchCapturingTeam().removePlayer(player);
            return true;
        }
        return false;
    }
    /**
     * removes a player to the escaping team
     * @param player individual to be added to current game
     * @return whether or not the player was successfully removed
     * */
    public boolean removePlayerFromEscapingTeam(Player player) {
        if (this.fetchEscapingTeam().containsPlayer(player)) {
            this.fetchEscapingTeam().removePlayer(player);
            return true;
        }
        return false;
    }
    /***
     * Determines the player's team index in the TeamArray of the game
     * @param  player individual to be indexed
     * @return whether the team exists or -1 if the player does not exist in the team
     * */
    public int getTeamIndex(Player player) {
        if (this.fetchCapturingTeam().containsPlayer(player)) {
            return GameSession.TEAM_CAPTURING;
        } else if (this.fetchEscapingTeam().containsPlayer(player)) {
            return GameSession.TEAM_ESCAPING;
        }
        return -1;
    }
    /***
     * Determines the player's position in the playerArray of the team
     * @param  player individual to be indexed
     * @return whether the player exists or -1 if the player does not exist in the team
     * */
    public int getPlayerIndexInTeam(Player player) {
        int teamId = getTeamIndex(player);
        return this.getTeamArrayList().get(teamId).getPlayerArrayList().indexOf(player);
    }
    /**
     * Adds a team to the game if space exists
     * @param  team team to be added
     * @return whether or not the team was successfully added
     * */
    public boolean addTeam(Team team) {
        if (MAX_TEAMS_2 > teamArrayList.size()) {
            this.teamArrayList.add(team);
            return true;
        } else {
            return false;
        }
    }
    /***
     * returns the player instances within a specified distance
     * @param  player individual whose vicinity is being queried
     * @param distance radious around player to be queried
     * @return arraylist containing plaeyrs within distance of the given player
     * */
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

    /***
     * returns the number of players that are currenly capturing
     * @return number of capturing individuals
     * */
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
    /**
     * returns the numbers of players that have been captrued
     *@return number of captured players
     * **/
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
    /***
     * sets the active status of players that have not updated their status beyond the inactive
     * time to false
     * @param currentTime current epoch time
     * @param  maxAllowedInactivetime the longest allowed time of inactivity before being labelled
     *                                inactive
     * */
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
    /****
     * determines the remaining time to game end
     * @param currentTime current time in epoch time format
     * */
    public long getRemainingTime(long currentTime) {
        return this.getEndTime() - currentTime;
    }

    /***
     * determines whether the game is over based on the time or the capture state of the game
     * @param currentTime current epoch time
     *@return  whether the game should be declared as over
     * */
    public boolean isGameOver(long currentTime) {
        //no time left
        if (this.capturingCount() == this.getMaxPlayers() || this.getRemainingTime(currentTime) <= 0) {
            this.setGameCompleted(true);
            return true;
        }
        this.setGameCompleted(false);
        return false;
    }
    /****
     * returns an arraylist of captured players
     * @return an arraylist of players that are both capturing and have been captured
     * */
    public ArrayList<Player> getCapturedList() {
        ArrayList<Player> capturedList = new ArrayList<>();
        for (Player player : this.allPlayerArrayLists()) {
            if (player.getCapturing() == true && player.getHasBeenCaptured() == true) {
                capturedList.add(player);
            }
        }
        return capturedList;
    }
    /***
     * returns a list of all the players that are playing the capturing role
     * @return arraylist of members whose 'capturing' is set to true
     * */
    public ArrayList<Player> fetchCapturingList() {
        ArrayList<Player> capturedList = new ArrayList<>();
        for (Player player : this.allPlayerArrayLists()) {
            if (player.getCapturing() == true) {
                capturedList.add(player);
            }
        }
        return capturedList;
    }
    /**
     * captures one player and sets the other as captured
     * @param captured the individual whose status is set as captured
     * @param capturing the individual who is capturing and updating his list of captures
     * @return whehter or not hte capture has been successful
     * */
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
    /***
     * removes a team from the gamesession
     * @param team the team to be removed
     * @return whether or not the team was successfull removed
     * */
    public boolean removeTeam(Team team) {
        if (teamArrayList != null && teamArrayList.size() > 0 && teamArrayList.contains(team)) {
            teamArrayList.remove(team);
            return true;
        }
        return false;
    }
    /****
     *
     * Adds a capturing team and an escaping team, setting the first to capturing
     * @param gameSessionId the id of the session to be added
     * @param player the player that created the two teams or that is creating the game
     * */
    public void add2Teams(String gameSessionId, Player player) {
        for (int i = 0; i < MAX_TEAMS_2; i++) {
            //create 2 opposign teams
            this.addTeam(new Team("team_" + String.valueOf(i + 1),
                    "team_" + new Integer(i + 1).toString(), new Boolean(i == TEAM_CAPTURING),
                    player.getDisplayName()));
        }
    }
    /****
     *
     * Updates all player location relative to the origin
     * @param originPlayerLocation the center around which all other relative positions are to be determined
     *
     * */
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
    /****
     * remvoves all the relative posistions that have been added to the players
     * */
    public void clearRelativeLocations(){
        for(Team team : teamArrayList){
            for(Player player: team.getPlayerArrayList()){
                player.setCoordinateLocation(null);
                player.getRelativePath().clear();
            }
        }

    }
    /****
     *
     * updates the player's location, identifying the player by displayname
     * @param player the individiual whose location is to be updated
     * @param latLng the new location of the individual
     * @return whether the update was succesful or not
     * */
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
    /****
     *
     * updates the path of all the players based on their previous locations
     * @param maxSteps the maximum number of steps that an individual leaves behind them
     * */
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
    /**
     * fetches the playerArraylists of all individuals on both teams
     * @return an arraylist of players from all teams
     * **/
    public ArrayList<Player> allPlayerArrayLists() {
        //concatenate player arraylists
        ArrayList<Player> allTeams = new ArrayList<Player>();
        ArrayList<Player> team1 = this.getTeamArrayList().get(0).getPlayerArrayList();
        ArrayList<Player> team2 = this.getTeamArrayList().get(1).getPlayerArrayList();
        allTeams.addAll(team1);
        allTeams.addAll(team2);
        return allTeams;
    }
    /***
     * gets the detils of the player whose name is provided
     * @param displayname the unique identifier of the player
     * @return null object if the fetch was unsuccessful and player object if it was successful
     * */
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
