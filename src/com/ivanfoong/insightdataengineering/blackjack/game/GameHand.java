package com.ivanfoong.insightdataengineering.blackjack.game;

import com.ivanfoong.insightdataengineering.blackjack.card.Card;
import com.ivanfoong.insightdataengineering.blackjack.card.CardValue;

import java.util.ArrayList;

/**
 * Created by ivanfoong on 4/4/14.
 */
public class GameHand {
    private ArrayList<Card> mCards;

    public GameHand() {
        mCards = new ArrayList<Card>();
    }

    public GameHand(final ArrayList<Card> aCards) {
        mCards = aCards;
    }

    public void addCard(final Card aCard) {
        mCards.add(aCard);
    }

    public ArrayList<Card> getCards() {
        return mCards;
    }

    public Integer getTotalCardsValue() {
        Integer numberOfAceCards = 0;
        Integer totalCardValue = 0;

        for (Card card : mCards) {
            Integer value = 0;
            switch (card.getCardValue()) {
                case ACE: {
                    numberOfAceCards++;
                    value = 1;
                    break;
                }
                case TWO: {
                    value = 2;
                    break;
                }
                case THREE: {
                    value = 3;
                    break;
                }
                case FOUR: {
                    value = 4;
                    break;
                }
                case FIVE: {
                    value = 5;
                    break;
                }
                case SIX: {
                    value = 6;
                    break;
                }
                case SEVEN: {
                    value = 7;
                    break;
                }
                case EIGHT: {
                    value = 8;
                    break;
                }
                case NINE: {
                    value = 9;
                    break;
                }
                case JACK:
                case QUEEN:
                case KING:
                case TEN: {
                    value = 10;
                    break;
                }
                default: {
                }
            }

            totalCardValue += value;
        }

        // handle special value calculation for "Ace"
        //   "A player and the dealer can count his or her own ace as 1 point or 11 points." - http://en.wikipedia.org/wiki/Blackjack
        for (int i=0; i<numberOfAceCards; i++) {
            Integer totalCardValueAwayFromBusting = 21 - totalCardValue;
            if (totalCardValueAwayFromBusting >= 10) {
                totalCardValue += 10;
            }
        }

        return totalCardValue;
    }

    public boolean hasBust() { return getTotalCardsValue() > 21; }

    public boolean hasBlackJack() { return getTotalCardsValue() == 21 && getCards().size() == 2; }
}
