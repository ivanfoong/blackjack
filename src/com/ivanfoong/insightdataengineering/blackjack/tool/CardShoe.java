package com.ivanfoong.insightdataengineering.blackjack.tool;

import com.ivanfoong.insightdataengineering.blackjack.card.Card;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class CardShoe {
    private Integer mMinimumCardCountToNewShoe;
    private ArrayList<Card> mCards;

    public CardShoe(final ArrayList<Card> aCards, final Integer aMinimumCardCountToNewShoe) {
        mCards = aCards;
        mMinimumCardCountToNewShoe = aMinimumCardCountToNewShoe;
    }

    public void setCards(final ArrayList<Card> aCards) {
        mCards = aCards;
    }

    public ArrayList<Card> popRemainingCards() {
        ArrayList<Card> remainingCards = new ArrayList<Card>(mCards);
        mCards.clear();
        return remainingCards;
    }

    public Card popFirstCard() {
        Card firstCard = mCards.get(0);
        mCards.remove(firstCard);

        return firstCard;
    }

    public void shuffleCards(final Integer aShuffleCount) {
        for (int i=0; i < aShuffleCount; i++) {
            SecureRandom secureRandom = new SecureRandom();
            Collections.shuffle(mCards, secureRandom);
        }
    }

    public boolean requireNewShoe() {
        return (mCards.size() < mMinimumCardCountToNewShoe);
    }
}
