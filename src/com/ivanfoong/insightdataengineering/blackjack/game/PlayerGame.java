package com.ivanfoong.insightdataengineering.blackjack.game;

import com.ivanfoong.insightdataengineering.blackjack.user.Player;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class PlayerGame {
    private Player mPlayer;
    private GameHand mGameHand;
    private Integer mBetAmount;

    public PlayerGame(final Player aPlayer, final GameHand aGameHand, final Integer aBetAmount) {
        mPlayer = aPlayer;
        mGameHand = aGameHand;
        mBetAmount = aBetAmount;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public GameHand getGameHand() {
        return mGameHand;
    }

    public Integer getBetAmount() {
        return mBetAmount;
    }

    public boolean hasBust() { return mGameHand.getTotalCardsValue() > 21; }
}
