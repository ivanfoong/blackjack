package com.ivanfoong.insightdataengineering.blackjack.game;

import com.ivanfoong.insightdataengineering.blackjack.user.Player;

import java.util.Hashtable;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Game {
    private GameHand mDealerHand;
    private Hashtable<Player, GameHand> mPlayersHands;

    public Game() {
        mPlayersHands = new Hashtable<Player, GameHand>();
    }

    public Game(final GameHand aDealerHand, final Hashtable<Player, GameHand> aPlayersHands) {
        mDealerHand = aDealerHand;
        mPlayersHands = aPlayersHands;
    }

    public GameHand getDealerHand() {
        return mDealerHand;
    }

    public void setDealerHand(final GameHand aDealerHand) {
        mDealerHand = aDealerHand;
    }

    public Hashtable<Player, GameHand> getPlayersHands() {
        return mPlayersHands;
    }

    public GameHand getPlayerHand(final Player aPlayer) {
        if (mPlayersHands.get(aPlayer) == null) {
            mPlayersHands.put(aPlayer, new GameHand());
        }
        return mPlayersHands.get(aPlayer);
    }

    public void addPlayerHand(final Player aPlayer, final GameHand aGameHand) {
        mPlayersHands.put(aPlayer, aGameHand);
    }
}
