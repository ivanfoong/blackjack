package com.ivanfoong.insightdataengineering.blackjack.tool;

import com.ivanfoong.insightdataengineering.blackjack.game.Game;

import java.util.ArrayList;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class CardDiscardHolder {
    private ArrayList<Game> mGames;

    public CardDiscardHolder() {
        mGames = new ArrayList<Game>();
    }

    public void addGame(final Game aGame) {
        mGames.add(aGame);
    }

    public void clearGames() { mGames.clear(); }

    public ArrayList<Game> getGames() {
        return mGames;
    }
}
