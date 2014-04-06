package com.ivanfoong.insightdataengineering.blackjack.user;

import com.ivanfoong.insightdataengineering.blackjack.wallet.Wallet;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Person {
    private Wallet mWallet;

    public Person(final Double aTotalValue) {
        mWallet = new Wallet(aTotalValue);
    }

    public Wallet getWallet() {
        return mWallet;
    }
}
