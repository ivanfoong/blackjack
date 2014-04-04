package com.ivanfoong.insightdataengineering.blackjack.card;

import java.util.ArrayList;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class Deck {
    private ArrayList<Card> mCards;

    public Deck(final ArrayList<Card> aCards) {
        mCards = aCards;
    }

    public ArrayList<Card> getCards() {
        return mCards;
    }

    public static Deck getStandardDeck() {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (CardSuit cardSuit : CardSuit.values()) {
            for (CardValue cardValue : CardValue.values()) {
                Card newCard = new Card(cardSuit, cardValue);
                cards.add(newCard);
            }
        }
        return new Deck(cards);
    }
}
