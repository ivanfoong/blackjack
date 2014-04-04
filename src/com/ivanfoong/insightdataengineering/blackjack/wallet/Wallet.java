package com.ivanfoong.insightdataengineering.blackjack.wallet;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Wallet {
    private Integer mTotalValue;

    public Wallet(final Integer aTotalValue) {
        mTotalValue = aTotalValue;
    }

    public Integer getTotalValue() {
        return mTotalValue;
    }

    public void increaseValue(final Integer aValue) {
        mTotalValue += aValue;
    }

    public void decreaseValue(final Integer aValue) throws NegativeWalletValueException {
        if ((mTotalValue - aValue) < 0) {
            throw new NegativeWalletValueException();
        }
        mTotalValue -= aValue;
    }
}
