package com.ivanfoong.insightdataengineering.blackjack.wallet;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Wallet {
    private Double mTotalValue;

    public Wallet(final Double aTotalValue) {
        mTotalValue = aTotalValue;
    }

    public Double getTotalValue() {
        return mTotalValue;
    }

    public void increaseValue(final Double aValue) {
        mTotalValue += aValue;
    }

    public void decreaseValue(final Double aValue) throws NegativeWalletValueException {
        if ((mTotalValue - aValue) < 0) {
            throw new NegativeWalletValueException();
        }
        mTotalValue -= aValue;
    }
}
