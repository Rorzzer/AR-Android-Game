package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 21/09/2017.
 */

import java.util.Random;

/**
 *class for creating testing data for use in manual testcases
 * */
public class DataGenerator {
    private static int MAX_TEAM_CAP = 20;
    private static int MAX_RADIUS = 20;
    private static int MAX_TEAMS= 2;
    private static double MAX_LAT = 89.0;
    private static double MIN_LAT = -89.0;
    private static double MAX_LONG = 179.0;
    private static double MIN_LONG = -179.0;
    Random random;
    public DataGenerator(){
       this.random = new Random();
    }
    public GameSession generateRandomGameSession(LatLng latLng,int maxTeamMembers){
        GameSession gameSession = new GameSession();
        Player creator = generateRandomPlayer(latLng);
        gameSession.setCreator(creator.getDisplayName());
        gameSession.setMaxPlayers(maxTeamMembers * GameSession.MAX_TEAMS_2);
        gameSession.setLocation(latLng);
        gameSession.setSessionId(new Integer(random.nextInt()).toString());
        gameSession.setStartTime(random.nextLong());
        gameSession.setDuration(random.nextLong());
        gameSession.setEndTime(gameSession.getStartTime()+gameSession.getDuration());
        gameSession.setGameRadius(random.nextInt());
        for (int i = 0; i < GameSession.MAX_TEAMS_2; i++) {
            gameSession.addTeam(this.generateRandomTeam(latLng, maxTeamMembers, true));
        }

        return gameSession;

    }

    public GameSession generateRandomGameSession(LatLng latLng){
        return generateRandomGameSession(latLng,MAX_TEAM_CAP);
    }

    public Team generateRandomTeam(LatLng latLng, int maxMembers, boolean isCapturing){
        Team team = new Team(new Integer(random.nextInt()).toString(), "team" + random.nextInt(), true, generateRandomPlayer(latLng).getDisplayName());
        team.setMaxPlayers(maxMembers);
        team.setTeamImageUri("www.team"+random.nextInt()+".com");
        for (int i = 0; i < maxMembers; i++) {
            team.addPlayer(generateRandomPlayer(latLng));
        }
        team.setActive(random.nextBoolean());
        return team;
    }
    public Player generateRandomPlayer(LatLng latLng){
        Player player = new Player("player"+random.nextInt());
        player.setActive(random.nextBoolean());
        player.setLastLoggedOn(random.nextLong());
        LatLng nLatLng = new LatLng(latLng.getLatitude() + ((int) random.nextDouble()) % MAX_RADIUS,
                latLng.getLongitude() + ((int) random.nextDouble()) % MAX_RADIUS, 20);
        player.setAbsLocation(nLatLng);
        player.getPath().add(nLatLng);
        player.getCapturedList().add("tom");
        return player;
    }
    public LatLng generateRandomLocation(){
        LatLng latLng = new LatLng(MIN_LAT + (MAX_LAT - MIN_LAT) * random.nextDouble(),
                MIN_LONG + (MAX_LONG - MIN_LONG) * random.nextDouble(), 10);
        return latLng;

    }
}
