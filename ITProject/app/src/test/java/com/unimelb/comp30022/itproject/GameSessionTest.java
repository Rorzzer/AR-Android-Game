package com.unimelb.comp30022.itproject;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void removeTeamWithTeamsExist() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        player = dataGenerator.generateRandomPlayer(latLng);
        gameSession = new GameSession();
        gameSession.add2Teams("Sessionid", player);
        assertEquals("Expected false", false, gameSession.removeTeam(team));
    }

    @Test
    public void removeTeamWithTeamsDontExist() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
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
        assertEquals("Expected Null", null, player.getCoordinateLocation());
        gameSession.updateRelativeLocations(player.getAbsLocation());
        assertEquals("Expected True",true, (player.getCoordinateLocation()!=null) );
    }

    @Test
    public void clearRelativeLocationslocations() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        gameSession = dataGenerator.generateRandomGameSession(latLng);
        player = gameSession.getTeamArrayList().get(0).getPlayerArrayList().get(0);
        gameSession.updateRelativeLocations(player.getAbsLocation());
        assertEquals("Expected True",true, (player.getCoordinateLocation()!=null) );
        gameSession.clearRelativeLocations();
        assertEquals("Expected Null", null, player.getCoordinateLocation());
    }

    @Test
    public void convertToCartesianSamePoint() throws Exception {
        latLng = dataGenerator.generateRandomLocation();
        gameSession = new GameSession();
        assertEquals("Expected true",true,gameSession.convertToCartesian(latLng,latLng).equals(gameSession.convertToCartesian(latLng,latLng)));
        assertEquals(gameSession.convertToCartesian(latLng,latLng).getX(),0.0,0.1);
        assertEquals(gameSession.convertToCartesian(latLng,latLng).getY(),0.0,0.1);
        assertEquals(gameSession.convertToCartesian(latLng,latLng).getZ(),0.0,0.1);
    }
    @Test
    public void convertToCartesianDifferentPoints() throws Exception {
        gameSession = new GameSession();
        LatLng loc1 = new LatLng(13.3,144.343);
        LatLng loc2 = new LatLng(14.2,90.34);
        assertEquals("Expected False", false,
                gameSession.convertToCartesian(loc1,loc2)
                        .equals(gameSession.convertToCartesian(loc2,loc2)));
        assertEquals(gameSession.convertToCartesian(latLng,latLng).getZ(),0.0,0.1);
        assertEquals(gameSession.convertToCartesian(latLng,latLng).getZ(),0.0,0.1);


    }



}