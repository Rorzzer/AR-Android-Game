package com.unimelb.comp30022.itproject;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * unit testing framework for gamesession class
 * Created by Kiptenai on 2/10/2017.
 */
public class GameSessionTest {
    private DataGenerator dataGenerator = new DataGenerator();
    private LatLng latLng = null;
    private Player player = null;
    private GameSession gameSession = null;
    private Team team = null;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addTeamWithVaccancy() throws Exception{
        latLng = dataGenerator.generateRandomLocation();
        team = dataGenerator.generateRandomTeam(latLng,10,true);
        gameSession = dataGenerator.generateRandomGameSession(latLng);
        gameSession.removeTeam(gameSession.getTeamArrayList().get(0));
        assertEquals("Expected true",true,gameSession.addTeam(team));
    }

    @Test
    public void addTeamWithoutVaccancy() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        team = dataGenerator.generateRandomTeam(latLng,10,true);
        gameSession = dataGenerator.generateRandomGameSession(latLng);
        assertEquals("Expected false",false, gameSession.addTeam(team));
    }

    @Test
    public void removeTeamWithTeamsExist() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        team = dataGenerator.generateRandomTeam(latLng,10,true);
        gameSession = new GameSession();
        gameSession.addTeam(team);
        assertEquals("Expected false", false, gameSession.removeTeam(team));
    }

    @Test
    public void removeTeamWithTeamsDontExist() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        team = dataGenerator.generateRandomTeam(latLng,10,true);
        gameSession = new GameSession();
        assertEquals("Expected false", false, gameSession.removeTeam(team));
    }
    @Test
    public void addPlayerToTeamWithVaccancy() throws Exception{
        latLng = dataGenerator.generateRandomLocation();
        player = dataGenerator.generateRandomPlayer(latLng);
        team = dataGenerator.generateRandomTeam(latLng,10,true);
        team.getPlayerArrayList().remove(0);
        assertEquals("Expected True", true, team.addPlayer(dataGenerator.generateRandomPlayer(latLng)));
    }
    @Test
    public void addPlayerToTeamWithoutVaccancy() throws Exception{
        latLng = dataGenerator.generateRandomLocation();
        team = dataGenerator.generateRandomTeam(latLng,10,true);
        player = dataGenerator.generateRandomPlayer(latLng);
        assertEquals("Expected False", false,team.addPlayer(player));
    }
    @Test
    public void updateRelativeLocations() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        gameSession = dataGenerator.generateRandomGameSession(latLng);
        player = gameSession.getTeamArrayList().get(0).getPlayerArrayList().get(0);
        assertEquals("Expected Null", null, player.getRelLocation());
        gameSession.updateRelativeLocations(player.getAbsLocation());
        assertEquals("Expected True",true, (player.getRelLocation()!=null) );
    }

    @Test
    public void clearRelativeLocationslocations() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        gameSession = dataGenerator.generateRandomGameSession(latLng);
        player = gameSession.getTeamArrayList().get(0).getPlayerArrayList().get(0);
        gameSession.updateRelativeLocations(player.getAbsLocation());
        assertEquals("Expected True",true, (player.getRelLocation()!=null) );
        gameSession.clearRelativeLocations();
        assertEquals("Expected Null", null, player.getRelLocation());
    }

    @Test
    public void convertToCartesianSamePoint() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        gameSession = new GameSession();
        assertEquals("Expected true",true,gameSession.convertToCartesian(latLng).equals(gameSession.convertToCartesian(latLng)) );
    }
    @Test
    public void convertToCartesianDifferentPoints() throws Exception {
        gameSession = new GameSession();
        LatLng loc1 = new LatLng(13.3,34.2);
        LatLng loc2 = new LatLng(4.2,3.5);
        assertEquals("Expected False", false,
                gameSession.convertToCartesian(loc1)
                        .equals(gameSession.convertToCartesian(loc2)));

    }



}