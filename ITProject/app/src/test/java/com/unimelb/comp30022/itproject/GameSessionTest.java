package com.unimelb.comp30022.itproject;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    private ArrayList<Player> playerArrayList = new ArrayList<>();
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private Gson gson = new Gson();
    private Type gameSessionType = new TypeToken<GameSession>() {
    }.getType();

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
    public void fetchPlayer() throws Exception {
        GameSession session = new GameSession();
        session.setMaxPlayers(10);
        session.add2Teams("34342", new Player("creator"));
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        one.setDisplayName("one");
        one.setCapturing(true);
        one.setHasBeenCaptured(false);
        session.addPlayerToCapturingTeam(one);
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getDisplayName().equals(one.getDisplayName()), true);
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getCapturing().equals(one.getCapturing()), true);
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getHasBeenCaptured().equals(one.getHasBeenCaptured()), true);


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
        gameSession.setMaxPlayers(10);
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
        assertEquals("Expected true", true, GameSession.convertToCartesian(latLng, latLng).equals(GameSession.convertToCartesian(latLng, latLng)));
        assertEquals(GameSession.convertToCartesian(latLng, latLng).getX(), 0.0, 0.1);
        assertEquals(GameSession.convertToCartesian(latLng, latLng).getY(), 0.0, 0.1);
        assertEquals(GameSession.convertToCartesian(latLng, latLng).getZ(), 0.0, 0.1);
    }
    @Test
    public void convertToCartesianDifferentPoints() throws Exception {
        gameSession = new GameSession();
        LatLng loc1 = new LatLng(13.3, 144.343, 10);
        LatLng loc2 = new LatLng(14.2, 90.34, 10);
        assertEquals("Expected False", false,
                GameSession.convertToCartesian(loc1, loc2)
                        .equals(GameSession.convertToCartesian(loc2, loc2)));
        assertEquals(GameSession.convertToCartesian(loc1, loc1).getZ(), 0.0, 0.1);
        assertEquals(GameSession.convertToCartesian(loc2, loc2).getZ(), 0.0, 0.1);


    }

    @Test
    public void getPlayersWithinDist() throws Exception {
        //close players
        double dist = 20;
        GameSession session = new GameSession();
        session.setMaxPlayers(10);
        session.add2Teams("34342", new Player("creator"));
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798013, 144.958151, 10));
        Player three = dataGenerator.generateRandomPlayer(new LatLng(32.232, 147.2524, 10));
        Player four = dataGenerator.generateRandomPlayer(new LatLng(36.322, 140.36164, 10));
        one.setDisplayName("one");
        two.setDisplayName("two");
        three.setDisplayName("three");
        four.setDisplayName("four");
        assertTrue(session.currentPlayerCount() == 0);
        session.addPlayerToCapturingTeam(one);
        assertTrue(session.currentPlayerCount() == 1);
        session.addPlayerToCapturingTeam(two);
        assertTrue(session.currentPlayerCount() == 2);
        session.addPlayerToEscapingTeam(three);
        session.addPlayerToEscapingTeam(four);
        assertTrue(session.currentPlayerCount() == 4);
        assertEquals(session.getPlayersWithinDistance(one, dist).contains(one), true);
        assertEquals(session.getPlayersWithinDistance(one, dist).contains(two), true);
        assertEquals(session.getPlayersWithinDistance(one, dist).contains(three), false);
        assertEquals(session.getPlayersWithinDistance(one, dist).contains(four), false);

    }

    @Test
    public void capturePlayer() throws Exception {
        GameSession session = new GameSession();
        session.setMaxPlayers(10);
        session.add2Teams("34342", new Player("creator"));
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798013, 144.958151, 10));
        Player three = dataGenerator.generateRandomPlayer(new LatLng(32.232, 147.2524, 10));
        Player four = dataGenerator.generateRandomPlayer(new LatLng(36.322, 140.36164, 10));
        one.setCapturing(true);
        one.setHasBeenCaptured(false);
        two.setCapturing(false);
        two.setHasBeenCaptured(false);
        three.setCapturing(true);
        three.setHasBeenCaptured(false);
        four.setCapturing(false);
        four.setHasBeenCaptured(false);
        session.addPlayerToCapturingTeam(one);
        session.addPlayerToCapturingTeam(three);
        session.addPlayerToEscapingTeam(two);
        session.addPlayerToEscapingTeam(four);
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getPlayerCapturedList().size(), 0);
        session.capturePlayer(one, two);
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getPlayerCapturedList().size(), 1);
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getPlayerCapturedList().get(0), two.getDisplayName());
        assertEquals(session.getPlayerDetails(two.getDisplayName()).getCapturing(), true);
        assertEquals(session.getPlayerDetails(two.getDisplayName()).getHasBeenCaptured(), true);
        assertEquals(session.getPlayerDetails(two.getDisplayName()).getCapturedBy(), one.getDisplayName());
        assertEquals(session.getCapturedList().size(), 1);
        assertEquals(session.capturedCount(), 1);
        assertEquals(session.capturingCount(), 3);
        assertEquals(session.getCapturedList().size(), 1);
        assertEquals(session.getCapturedList().get(0).getDisplayName(), two.getDisplayName());
        session.capturePlayer(three, four);
        session.setEndTime(System.currentTimeMillis() - 20000);
        assertEquals(session.isGameOver(System.currentTimeMillis()), true);
        session.setEndTime(System.currentTimeMillis() + 20000);
        assertEquals(session.isGameOver(System.currentTimeMillis()), false);
        session.capturePlayer(one, three);
        assertEquals(one.getPlayerCapturedList().contains(three), false);
        assertEquals(three.getHasBeenCaptured(), false);
    }

    @Test
    public void addAndCheckFootsteps() {
        latLngs.clear();
        for (int i = 0; i < 10; i++) {
            latLngs.add(dataGenerator.generateRandomLocation());
        }
        GameSession session = new GameSession();
        session.setMaxPlayers(10);
        session.add2Teams("34342", new Player("creator"));

        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        session.addPlayerToCapturingTeam(one);
        int i = 0;
        for (LatLng point : latLngs) {
            i++;
            session.updatePlayerLocation(one, point);
            session.updatePaths(10);
            if (i < 10) {
                assertEquals(session.getPlayerDetails(one.getDisplayName()).getPath().get(i - 1).equals(point), true);
                assertEquals(session.getPlayerDetails(one.getDisplayName()).getPath().size(), i);
            } else {
                assertEquals(session.getPlayerDetails(one.getDisplayName()).getPath().size(), 10);

            }
        }
        i = 0;
        session.clearRelativeLocations();

    }

    @Test
    public void checkRelativeUpdate() {
        GameSession session = new GameSession();
        double dist = 10;
        session.setMaxPlayers(10);
        session.add2Teams("34342", new Player("creator"));
        CoordinateLocation zero = new CoordinateLocation(0.0, 0.0, 0.0, 0.0);
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798013, 144.958151, 10));
        Player three = dataGenerator.generateRandomPlayer(new LatLng(32.232, 147.2524, 10));
        Player four = dataGenerator.generateRandomPlayer(new LatLng(36.322, 140.36164, 10));
        session.addPlayerToCapturingTeam(one);
        session.addPlayerToCapturingTeam(three);
        session.addPlayerToEscapingTeam(two);
        session.addPlayerToEscapingTeam(four);
        one.setDisplayName("one");
        two.setDisplayName("two");
        three.setDisplayName("three");
        four.setDisplayName("four");
        session.capturePlayer(one, two);
        session.updateRelativeLocations(one.getAbsLocation());
        assertEquals(session.getPlayerDetails(one.getDisplayName()).getCoordinateLocation().equals(zero), true);
        CoordinateLocation locationTwo = session.getPlayerDetails(two.getDisplayName()).getCoordinateLocation();
        double square = locationTwo.getX() * locationTwo.getX() + locationTwo.getY() + locationTwo.getY() + locationTwo.getZ() * locationTwo.getZ();
        double sqrt = Math.sqrt(square);
        assertEquals(sqrt == GameSession.distanceBetweenTwoPlayers(one, two), true);
        CoordinateLocation locationOne = session.getPlayerDetails(one.getDisplayName()).getCoordinateLocation();
        square = locationOne.getX() * locationOne.getX() + locationOne.getY() + locationOne.getY() + locationOne.getZ() * locationOne.getZ();
        sqrt = Math.sqrt(square);
        assertTrue(session.getPlayersWithinDistance(one, dist).contains(two));
        assertEquals(sqrt == 0.0, true);
        one.setLastPing(System.currentTimeMillis() - 100000);
        two.setLastPing(System.currentTimeMillis() - 100000);
        three.setLastPing(System.currentTimeMillis() - 20000);
        four.setLastPing(System.currentTimeMillis());
        session.refreshActivePlayers(System.currentTimeMillis(), 20000);
        assertTrue(!session.getPlayerDetails(one.getDisplayName()).getActive());
        assertTrue(session.getPlayerDetails(four.getDisplayName()).getActive());

    }

    @Test
    public void determineCapturedBetweenSessions() {
        GameSession clientSession = new GameSession();
        GameSession serverSession = new GameSession();
        double dist = 10;
        serverSession.setMaxPlayers(10);
        serverSession.add2Teams("34342", new Player("creator"));
        CoordinateLocation zero = new CoordinateLocation(0.0, 0.0, 0.0, 0.0);
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798013, 144.958151, 10));
        Player three = dataGenerator.generateRandomPlayer(new LatLng(32.232, 147.2524, 10));
        Player four = dataGenerator.generateRandomPlayer(new LatLng(36.322, 140.36164, 10));
        one.setCapturing(true);
        one.setHasBeenCaptured(false);
        two.setCapturing(false);
        two.setHasBeenCaptured(false);
        three.setCapturing(true);
        three.setHasBeenCaptured(false);
        four.setCapturing(false);
        four.setHasBeenCaptured(false);
        serverSession.addPlayerToCapturingTeam(one);
        serverSession.addPlayerToCapturingTeam(three);
        serverSession.addPlayerToEscapingTeam(two);
        serverSession.addPlayerToEscapingTeam(four);
        one.setDisplayName("one");
        two.setDisplayName("two");
        three.setDisplayName("three");
        four.setDisplayName("four");
        serverSession.capturePlayer(one, two);
        clientSession = gson.fromJson(gson.toJson(serverSession), gameSessionType);
        serverSession.capturePlayer(three, four);
        assertTrue(GameSession.determineIndividualsCapturedFromUpdate(clientSession, serverSession).contains(four));
    }

    @Test
    public void checkIndexing() {
        GameSession session = new GameSession();
        session.setMaxPlayers(10);
        session.add2Teams("34342", new Player("creator"));
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798013, 144.958151, 10));
        Player three = dataGenerator.generateRandomPlayer(new LatLng(32.232, 147.2524, 10));
        Player four = dataGenerator.generateRandomPlayer(new LatLng(36.322, 140.36164, 10));
        one.setCapturing(true);
        one.setHasBeenCaptured(false);
        two.setCapturing(false);
        two.setHasBeenCaptured(false);
        three.setCapturing(true);
        three.setHasBeenCaptured(false);
        four.setCapturing(false);
        four.setHasBeenCaptured(false);
        session.addPlayerToCapturingTeam(one);
        session.addPlayerToCapturingTeam(three);
        session.addPlayerToEscapingTeam(two);
        session.addPlayerToEscapingTeam(four);
        assertEquals(session.getPlayerIndexInTeam(one), 0);
        assertEquals(session.getTeamIndex(one), 0);
        assertEquals(session.getPlayerIndexInTeam(two), 0);
        assertEquals(session.getTeamIndex(two), 1);
    }




}