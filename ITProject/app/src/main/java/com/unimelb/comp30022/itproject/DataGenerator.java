package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 21/09/2017.
 */

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 *class for creating testing data for use in manual testcases
 * */
public class DataGenerator {
    private static int MAX_TEAM_CAP = 20;
    private static int MAX_TEAMS= 2;

    Random random;
    public DataGenerator(){
       this.random = new Random();
    }
    public GameSession generateRandomGameSession(LatLng latLng){
        GameSession gameSession = new GameSession();
        gameSession.setCreator(generateRandomPlayer(latLng));
        gameSession.setMaxPlayers(MAX_TEAM_CAP *MAX_TEAMS);
        gameSession.setMaxTeams(MAX_TEAMS);
        gameSession.setLocation(latLng);
        gameSession.setSessionId(random.nextInt());
        gameSession.setStartTime(random.nextLong());
        gameSession.setDuration(random.nextLong());
        gameSession.setEndtime(gameSession.getStartTime()+gameSession.getDuration());
        gameSession.setGameRadius(random.nextInt());
        for(int i = 0;i<MAX_TEAMS;i++){
           gameSession.addTeam(generateRandomTeam(latLng));
        }
        return gameSession;

    }

    public Team generateRandomTeam(LatLng latLng){
        Team team = new Team(random.nextInt(),"team"+random.nextInt(), generateRandomPlayer(latLng));
        team.setMaxPlayers(MAX_TEAM_CAP);
        for(int i = 0; i< MAX_TEAM_CAP; i++){
            team.addPlayer(generateRandomPlayer(latLng));
        }
        team.setActive(random.nextBoolean());
        return team;
    }
    public Player generateRandomPlayer(LatLng latLng){
        Player player = new Player(random.nextInt(),"player"+random.nextInt());
        player.setActive(random.nextBoolean());
        player.setLastLoggedOn(random.nextLong());
        LatLng nLatLng = new LatLng(latLng.latitude+random.nextDouble(),latLng.latitude+random.nextDouble());
        player.setLocation(nLatLng);
        return player;
    }

}
