package com.ivanfoong.insightdataengineering.blackjack.card;

import java.util.ArrayList;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Card {
    private CardSuit mCardSuit;
    private CardValue mCardValue;

    public Card(final CardSuit aCardSuit, final CardValue aCardValue) {
        mCardSuit = aCardSuit;
        mCardValue = aCardValue;
    }

    public CardSuit getCardSuit() {
        return mCardSuit;
    }

    public CardValue getCardValue() {
        return mCardValue;
    }

    public String getCardValueString() {
        switch (mCardValue) {
            case ACE: {
                return "A";
            }
            case TWO: {
                return "2";
            }
            case THREE: {
                return "3";
            }
            case FOUR: {
                return "4";
            }
            case FIVE: {
                return "5";
            }
            case SIX: {
                return "6";
            }
            case SEVEN: {
                return "7";
            }
            case EIGHT: {
                return "8";
            }
            case NINE: {
                return "9";
            }
            case TEN: {
                return "10";
            }
            case JACK: {
                return "J";
            }
            case QUEEN: {
                return "Q";
            }
            case KING: {
                return "K";
            }
            default: {
                return "";
            }
        }
    }
}
