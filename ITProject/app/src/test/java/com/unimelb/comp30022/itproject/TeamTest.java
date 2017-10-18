package com.unimelb.comp30022.itproject;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Kiptenai on 18/10/2017.
 */
public class TeamTest {
    private DataGenerator dataGenerator = new DataGenerator();
    private LatLng location = new LatLng(37.798012, 144.958150, 10);

    @Test
    public void addPlayer() throws Exception {
        Team team = new Team("teamName");
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        one.setDisplayName("one");
        team.addPlayer(one);
        assertTrue(team.getTeamSize() == 1);
        team.addPlayer(one);
        assertTrue(team.getTeamSize() == 1);

    }

    @Test
    public void getTeamSize() throws Exception {
        Team team = new Team("teamName");
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        one.setDisplayName("one");
        two.setDisplayName("two");
        team.addPlayer(one);
        team.addPlayer(two);
        assertTrue(team.getTeamSize() == 2);
    }

    @Test
    public void removePlayer() throws Exception {
        Team team = new Team("teamName");
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        Player two = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        one.setDisplayName("one");
        two.setDisplayName("two");
        team.addPlayer(one);
        team.addPlayer(two);
        assertFalse(team.addPlayer(two));
        assertFalse(team.addPlayer(one));
        assertTrue(team.getTeamSize() == 2);
        team.removePlayer(two);
        assertTrue(team.getTeamSize() == 1);
        assertTrue(team.containsPlayer(two) == false);

    }

    @Test
    public void containsPlayer() throws Exception {
        Team team = new Team("teamName");
        Player one = dataGenerator.generateRandomPlayer(new LatLng(37.798012, 144.958150, 10));
        one.setDisplayName("one");
        team.addPlayer(one);
        assertTrue(team.containsPlayer(one));
    }

}