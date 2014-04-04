package com.ivanfoong.insightdataengineering.blackjack.user;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Player extends Person {
    private String mName;

    public Player(final Integer aTotalValue, final String aName) {
        super(aTotalValue);
        mName = aName;
    }

    public String getName() {
        return mName;
    }
}
