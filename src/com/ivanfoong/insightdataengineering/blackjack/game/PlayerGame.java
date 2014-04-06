package com.ivanfoong.insightdataengineering.blackjack.game;

import com.ivanfoong.insightdataengineering.blackjack.user.Player;

import java.util.ArrayList;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class PlayerGame {
    private Player mPlayer;
    private ArrayList<GameHand> mGameHands;
    private Integer mBetAmount;
    private boolean isDoubleBet;

    public PlayerGame(final Player aPlayer, final Integer aBetAmount) {
        mPlayer = aPlayer;
        mBetAmount = aBetAmount;
        mGameHands = new ArrayList<GameHand>();
        mGameHands.add(new GameHand());
        isDoubleBet = false;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public ArrayList<GameHand> getGameHands() {
        return mGameHands;
    }

    public void addGameHand(GameHand aGameHand) {
        mGameHands.add(aGameHand);
    }

    public Integer getBetAmount() {
        return mBetAmount;
    }

    public boolean isDoubleBet() {
        return isDoubleBet;
    }

    public void setDoubleBet(final boolean aIsDoubleBet) {
        isDoubleBet = aIsDoubleBet;
    }
}
