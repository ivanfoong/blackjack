package com.ivanfoong.insightdataengineering.blackjack.game;

import com.ivanfoong.insightdataengineering.blackjack.user.Dealer;
import com.ivanfoong.insightdataengineering.blackjack.user.Player;

import java.util.Hashtable;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Game {
    private Dealer mDealer;
    private GameHand mDealerHand;
    private Hashtable<Player, PlayerGame> mPlayerGames;

    public Game(final Dealer aDealer) {
        mDealer = aDealer;
        mPlayerGames = new Hashtable<Player, PlayerGame>();
    }

    public Game(final Dealer aDealer, final GameHand aDealerHand, final Hashtable<Player, PlayerGame> aPlayerGames) {
        mDealer = aDealer;
        mDealerHand = aDealerHand;
        mPlayerGames = aPlayerGames;
    }

    public Dealer getDealer() {
        return mDealer;
    }

    public GameHand getDealerHand() {
        return mDealerHand;
    }

    public void setDealerHand(final GameHand aDealerHand) {
        mDealerHand = aDealerHand;
    }

    public Hashtable<Player, PlayerGame> getPlayerGames() {
        return mPlayerGames;
    }

    public PlayerGame getPlayerGame(final Player aPlayer) {
        return mPlayerGames.get(aPlayer);
    }

    public void addPlayerGame(final Player aPlayer, final PlayerGame aPlayerGame) {
        mPlayerGames.put(aPlayer, aPlayerGame);
    }
}
