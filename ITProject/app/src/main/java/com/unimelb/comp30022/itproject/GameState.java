package com.unimelb.comp30022.itproject;

import java.util.ArrayList;

/**
 * Created by Kiptenai on 10/10/2017.
 */

public class GameState {
    private ArrayList<Player> playerArrayList;

    public GameState(ArrayList<Player> players) {
        playerArrayList = players;
    }

    public static ArrayList<Player> getSnapshot(GameSession gameSession) {
        return gameSession.getAllPlayerInformation();
    }

}
