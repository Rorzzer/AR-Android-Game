package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 21/09/2017.
 * useful for generating dummy data to test the creation of gamesessions
 * and the functionality of the team managment and player management systems
 *
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
    /**
     * Generates a random gamesession with two teams, randomly named members, based on a given
     * location
     * @param  latLng location of the initiated gamesession
     * */
    public GameSession generateRandomGameSession(LatLng latLng){
        return generateRandomGameSession(latLng,MAX_TEAM_CAP);
    }
    /**
     * Generates a random team with  randomly named members, based on a given
     * location
     * @param  latLng location of the initiated gamesession
     * @param  maxMembers number of members in the gamesession
     * @param isCapturing whether the generate team is an escaping or capturing team
     * */
    public Team generateRandomTeam(LatLng latLng, int maxMembers, boolean isCapturing){
        Team team = new Team(new Integer(random.nextInt()).toString(), "team" + random.nextInt(), true, generateRandomPlayer(latLng).getDisplayName());
        team.setTeamImageUri("www.team"+random.nextInt()+".com");
        for (int i = 0; i < maxMembers; i++) {
            team.addPlayer(generateRandomPlayer(latLng));
        }
        team.setActive(random.nextBoolean());
        return team;
    }
    /**
     * Generates a random player , based on a given
     * location
     * @param  latLng location of the initiated gamesession
     * */
    public Player generateRandomPlayer(LatLng latLng){
        Player player = new Player("player" + String.valueOf(random.nextInt() % 100000));
        player.setActive(random.nextBoolean());
        player.setLastLoggedOn(random.nextLong());
        LatLng nLatLng = new LatLng(latLng.getLatitude() + ((int) random.nextDouble()) % MAX_RADIUS,
                latLng.getLongitude() + ((int) random.nextDouble()) % MAX_RADIUS, 20);
        player.setAbsLocation(nLatLng);
        return player;
    }
    /**
     * Generates a random location on the planet
     * */
    public LatLng generateRandomLocation(){
        LatLng latLng = new LatLng(MIN_LAT + (MAX_LAT - MIN_LAT) * random.nextDouble(),
                MIN_LONG + (MAX_LONG - MIN_LONG) * random.nextDouble(), 10);
        return latLng;

    }
}
