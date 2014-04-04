package com.ivanfoong.insightdataengineering.blackjack.wallet;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class NegativeWalletValueException extends RuntimeException {
    public NegativeWalletValueException() {
        super();
    }

    public NegativeWalletValueException(final String aErrorMessage) {
        super(aErrorMessage);
    }
}
