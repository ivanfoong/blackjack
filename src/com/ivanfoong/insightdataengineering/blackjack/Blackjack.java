package com.ivanfoong.insightdataengineering.blackjack;

/**
 * Created by ivanfoong on 4/4/14
 *
 * Based on blackjack rules mentioned at http://en.wikipedia.org/wiki/Blackjack
 * Assuming dealer to stand on soft 17
 *
 * Sample usage
 *
 * # setting amount for next bet
 * Bet amount? (1-100): 1
 *
 * # game start
 * Dealer: 7 ?
 * You: A A (12)
 * h(hit) / s(stand) / sp(split)?: sp
 *
 * # insurance
 * Dealer: A ?
 * You: 2 3 (5)
 * insurance? (0-99): no
 * h(hit) / s(stand) / sp(split)?: h
 *
 * # game draw result
 * Dealer: A 9 (20)
 * You: A 9 (20)
 * Result: Draw
 *
 * # game won result
 * Dealer: A 8 (19)
 * You: A 9 (20)
 * Result: You win! (Chip: 101)
 *
 * # game lost result
 * Dealer: A Q (Blackjack)
 * You: A 9 (20)
 * Result: You lost! (Chip: 99)
 *
 * TODO possible multi player support, but will need to support multi player phase for player bust and blackjack scenarios as current implementation is assuming 1 player
 */

import com.ivanfoong.insightdataengineering.blackjack.card.Card;
import com.ivanfoong.insightdataengineering.blackjack.card.CardValue;
import com.ivanfoong.insightdataengineering.blackjack.card.Deck;
import com.ivanfoong.insightdataengineering.blackjack.game.GameStatus;
import com.ivanfoong.insightdataengineering.blackjack.game.PlayerGame;
import com.ivanfoong.insightdataengineering.blackjack.wallet.NegativeWalletValueException;
import com.ivanfoong.insightdataengineering.blackjack.game.Game;
import com.ivanfoong.insightdataengineering.blackjack.game.GameHand;
import com.ivanfoong.insightdataengineering.blackjack.tool.CardDiscardHolder;
import com.ivanfoong.insightdataengineering.blackjack.tool.CardShoe;
import com.ivanfoong.insightdataengineering.blackjack.user.Dealer;
import com.ivanfoong.insightdataengineering.blackjack.user.Player;

import java.util.*;

public class Blackjack {
    // START configurations
    private static final Integer CARD_SHOE_NUMBER_OF_DECKS = 1;
    private static final Integer CARD_SHOE_MINIMUM_CARDS_COUNT = 13;
    private static final Double DEALER_STARTING_TOTAL_CHIP_VALUE = 1000.0;
    private static final Double PLAYER_STARTING_TOTAL_CHIP_VALUE = 100.0;
    private static final Double MINIMUM_BET_AMOUNT = 1.0;
    private static final Integer SHUFFLE_COUNT = 3;
    private static final Boolean SHOW_CARD_SUIT = false;
    // END configurations

    // START game rules
    private static final Integer BLACKJACK_INITIAL_DEALED_CARDS_COUNT = 2;
    private static final Float BLACKJACK_WINNING_RATE = 3.0f/2.0f;
    private static final Float INSURANCE_WINNING_RATE = 2.0f/1.0f;
    private static final Integer DEALER_STAND_ON_TOTAL_CARDS_VALUE = 17;
    private static final String UNKNOWN_CARD_VALUE_STRING = "?";
    // END game rules

    private static final boolean DEBUGGING = false;

    public static void main(final String[] aArguments) {
        Blackjack blackjack = new Blackjack();
        blackjack.start(CARD_SHOE_NUMBER_OF_DECKS, CARD_SHOE_MINIMUM_CARDS_COUNT, DEALER_STARTING_TOTAL_CHIP_VALUE
                , PLAYER_STARTING_TOTAL_CHIP_VALUE, MINIMUM_BET_AMOUNT, SHUFFLE_COUNT, SHOW_CARD_SUIT);
    }

    public void start(final Integer aDeckCount, final Integer aShoeMinimumCardsCount,
                       final Double aDealerStartingChipsValue, final Double aPlayerStartingChipsValue,
                       final Double aMinimumBetAmount, final Integer aShuffleCount, final Boolean aShowCardSuit) {
        // START setup
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i=0; i < aDeckCount; i++) {
            cards.addAll(Deck.getStandardDeck().getCards());
        }
        CardShoe cardShoe = new CardShoe(cards, aShoeMinimumCardsCount);
        cardShoe.shuffleCards(aShuffleCount); // using automatic card shuffler and shoe
        CardDiscardHolder cardDiscardHolder = new CardDiscardHolder();
        Dealer dealer = new Dealer(aDealerStartingChipsValue);
        Player player = new Player(aPlayerStartingChipsValue, "Player");

        Scanner scanner = new Scanner(System.in);
        // END setup

        while (startGame(scanner, cardShoe, cardDiscardHolder, dealer, player, aMinimumBetAmount, aShuffleCount, aShowCardSuit)) {}

        // START cleanup
        scanner.close();
        // END cleanup

        System.out.println("> Player have no more chips, blackjack game ended.");
    }

    private boolean startGame(final Scanner aScanner, final CardShoe aCardShoe,
                                     final CardDiscardHolder aCardDiscardHolder, final Dealer aDealer,
                                     final Player aPlayer, final Double aMinimumBetAmount, final Integer aShuffleCount,
                                     final Boolean aShowCardSuit) {
        if (aCardShoe.requireNewShoe()) {
            reShoe(aCardShoe, aCardDiscardHolder, aShuffleCount);
        }

        Integer betAmount = placeBet(aScanner, aPlayer, aMinimumBetAmount);

        ArrayList<Player> players = new ArrayList<Player>();
        players.add(aPlayer);

        Game currentGame = dealCards(aCardShoe, aDealer, players, betAmount);

        // dealer ask for insurance first if his hand's first card is Ace
        doInsurancePhase(aScanner, currentGame, aShowCardSuit);

        // dealer win if blackjack
        if (!currentGame.getDealerHand().hasBlackJack()) {
            // query user actions
            boolean allPlayerBust = true;
            boolean allPlayerBlackjack = true;
            Enumeration<Player> playerEnumeration = currentGame.getPlayerGames().keys();
            while (playerEnumeration.hasMoreElements()) {
                Player player = playerEnumeration.nextElement();
                PlayerGame playerGame = currentGame.getPlayerGame(player);
                playerAction(aScanner, currentGame.getDealerHand(), playerGame, aCardShoe, aShowCardSuit);
                for (GameHand gameHand : playerGame.getGameHands()) {
                    if (allPlayerBust && !gameHand.hasBust()) {
                        allPlayerBust = false;
                    }

                    if (allPlayerBlackjack && !gameHand.hasBlackJack()) {
                        allPlayerBlackjack = false;
                    }
                }
            }

            if (!allPlayerBust && !allPlayerBlackjack) {
                // process dealer actions
                dealerAction(currentGame.getDealerHand(), aCardShoe, aShowCardSuit);
            }
        }

        // resolve winning/losing
        resolveGame(currentGame);

        // only add game to discard holder after the game
        aCardDiscardHolder.addGame(currentGame);

        if (DEBUGGING) {
            printDiscardedCards(aCardDiscardHolder);
        }

        return aPlayer.getWallet().getTotalValue() > 0;
    }

    private void doInsurancePhase(final Scanner aScanner, final Game aGame, final Boolean aShowCardSuit) {
        if (aGame.getDealerHand().getCards().get(0).getCardValue() == CardValue.ACE) {

            Enumeration<Player> playerEnumeration = aGame.getPlayerGames().keys();
            Hashtable<Player, Integer> insurancePot = new Hashtable<Player, Integer>();
            while (playerEnumeration.hasMoreElements()) {
                Player player = playerEnumeration.nextElement();

                Integer insuranceAmount = -1;

                int gameHandIndex = 0;
                for (GameHand gameHand : aGame.getPlayerGame(player).getGameHands()) {
                    gameHandIndex++;
                    while(!gameHand.hasBlackJack() && insuranceAmount < 0) {
                        printDivider();
                        printGameProgress(aGame.getDealerHand(), player.getName(), gameHand, gameHandIndex, aShowCardSuit);
                        System.out.println("> Dealer has Ace!");
                        System.out.print("< Insurance? (0-" + player.getWallet().getTotalValue() + "): ");
                        String inputString = aScanner.nextLine();
                        try {
                            insuranceAmount = Integer.parseInt(inputString);
                        }
                        catch (NumberFormatException e) {
                            System.out.println("> Invalid input!");
                        };
                    }
                }

                if (insuranceAmount > 0) {
                    player.getWallet().decreaseValue(insuranceAmount.doubleValue());
                    insurancePot.put(player, insuranceAmount);
                }
            }

            if (aGame.getDealerHand().hasBlackJack()) {
                System.out.println("> Dealer had blackjack!");

                playerEnumeration = aGame.getPlayerGames().keys();
                while (playerEnumeration.hasMoreElements()) {
                    Player player = playerEnumeration.nextElement();

                    Integer insuranceAmount = insurancePot.get(player);

                    if (insuranceAmount != null) {
                        Float winInsuranceAmount = (insuranceAmount * INSURANCE_WINNING_RATE);
                        player.getWallet().increaseValue(winInsuranceAmount.doubleValue());
                        System.out.println(player.getName() + ": wins insurance total of " + String.format("%.2f",winInsuranceAmount));
                    }
                }
            }
            else {
                System.out.println("> Dealer did not had blackjack");

                for (Integer insuranceAmount : insurancePot.values()) {
                    aGame.getDealer().getWallet().increaseValue(insuranceAmount.doubleValue());
                }
            }
        }
    }

    private void printDivider() {
        System.out.println("- - - - - - - - - - - - - - - - - - -");
    }

    private void resolveGame(final Game aGame) {
        Integer dealerValue = aGame.getDealerHand().getTotalCardsValue();

        Enumeration<Player> playerEnumeration = aGame.getPlayerGames().keys();

        ArrayList<String> playerBalances = new ArrayList<String>();

        while (playerEnumeration.hasMoreElements()) {
            Player player = playerEnumeration.nextElement();
            PlayerGame playerGame = aGame.getPlayerGame(player);

            int gameHandIndex = 0;
            for (GameHand gameHand : playerGame.getGameHands()) {
                gameHandIndex++;

                Integer playerValue = gameHand.getTotalCardsValue();

                GameStatus gameStatus;
                if (gameHand.hasBlackJack() && aGame.getDealerHand().hasBlackJack()) {
                    gameStatus = GameStatus.PUSH;
                }
                else if (gameHand.hasBlackJack()) {
                    gameStatus = GameStatus.WIN_BLACKJACK;
                }
                else if (aGame.getDealerHand().hasBlackJack()) {
                    gameStatus = GameStatus.LOSE_BLACKJACK;
                }
                else if (playerValue <= 21) {
                    if (dealerValue <= 21) {
                        if (playerValue > dealerValue) {
                            gameStatus = GameStatus.WIN;
                        }
                        else if (playerValue < dealerValue) {
                            gameStatus = GameStatus.LOSE;
                        }
                        else {
                            gameStatus = GameStatus.PUSH;
                        }
                    }
                    else {
                        gameStatus = GameStatus.WIN;
                    }
                }
                else {
                    gameStatus = GameStatus.BUST;
                }

                Double betAmount = playerGame.getBetAmount().doubleValue();
                if (playerGame.isDoubleBet()) {
                    betAmount = betAmount * 2.0;
                }

                String status = "";
                switch (gameStatus) {
                    case WIN_BLACKJACK: {
                        status = "wins " + String.valueOf(betAmount) + " chip(s), Blackjack!(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                        player.getWallet().increaseValue(betAmount+(betAmount*BLACKJACK_WINNING_RATE));
                        aGame.getDealer().getWallet().decreaseValue(betAmount*BLACKJACK_WINNING_RATE);
                        break;
                    }
                    case LOSE_BLACKJACK: {
                        status = "loses " + String.valueOf(betAmount) + " chip(s), " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs Blackjack!(dealer)";
                        aGame.getDealer().getWallet().increaseValue(betAmount);
                        break;
                    }
                    case WIN: {
                        status = "wins " + String.valueOf(betAmount) + " chip(s), " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                        player.getWallet().increaseValue(betAmount*2);
                        aGame.getDealer().getWallet().decreaseValue(betAmount);
                        break;
                    }
                    case LOSE: {
                        status = "loses " + String.valueOf(betAmount) + " chip(s), " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                        aGame.getDealer().getWallet().increaseValue(betAmount);
                        break;
                    }
                    case PUSH: {
                        status = "pushed, " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                        player.getWallet().increaseValue(betAmount);
                        break;
                    }
                    case BUST: {
                        status = "busted and loses " + String.valueOf(betAmount) + " chip(s), " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ")";
                        aGame.getDealer().getWallet().increaseValue(betAmount);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                printDivider();
                System.out.println(player.getName() + " Game#" + String.valueOf(gameHandIndex) + ": " + status + "!");
            }

            playerBalances.add(player.getName() + " has " + String.valueOf(player.getWallet().getTotalValue()) + " chips left");
        }

        for (String playerBalance : playerBalances) {
            System.out.println(playerBalance);
        }
    }

    private void dealerAction(final GameHand aDealerGameHand, final CardShoe aCardShoe, final Boolean aShowCardSuit) {
        System.out.println("Dealer: " + aDealerGameHand.generateCardValueString(aShowCardSuit));

        while (aDealerGameHand.getTotalCardsValue() < DEALER_STAND_ON_TOTAL_CARDS_VALUE) {
            Card dealedCard = aCardShoe.popFirstCard();
            System.out.println("> Dealer receives " + dealedCard.getCardValueString() + "!");
            aDealerGameHand.addCard(dealedCard);
            System.out.println("Dealer: " + aDealerGameHand.generateCardValueString(aShowCardSuit));
        }
    }

    private void playerAction(final Scanner aScanner, final GameHand aDealerGameHand, final PlayerGame aPlayerGame,
                              final CardShoe aCardShoe, final Boolean aShowCardSuit) {
        LinkedList<GameHand> gameHandProcessQueue = new LinkedList<GameHand>(aPlayerGame.getGameHands());

        int gameHandIndex = 0;
        while (gameHandProcessQueue.size() > 0) {
            String inputString = "";
            gameHandIndex++;
            GameHand gameHand = gameHandProcessQueue.pop();
            while(gameHand.getTotalCardsValue() < 21 && !inputString.equals("s")) {
                printDivider();
                printGameProgress(aDealerGameHand, aPlayerGame.getPlayer().getName(), gameHand, gameHandIndex, aShowCardSuit);

                boolean isDoublePossible = (gameHand.getCards().size() == 2);
                boolean isSplitPossible = isDoublePossible && (gameHand.getCards().get(0).getCardValue() == gameHand.getCards().get(1).getCardValue());

                System.out.print("< h(hit) / s(stand)" + (isSplitPossible?" / sp(split)":"") + (isDoublePossible?" / d(double)?":"") + ": ");
                inputString = aScanner.nextLine();

                if (inputString.equals("h")) {
                    Card newCard = aCardShoe.popFirstCard();
                    gameHand.addCard(newCard);
                    System.out.println("> " + aPlayerGame.getPlayer().getName() + " receives " + newCard.getCardValueString() + ", total: " + String.valueOf(gameHand.getTotalCardsValue()));
                }
                else if (isSplitPossible && inputString.equals("sp")) {
                    Card card2 = gameHand.getCards().get(1);
                    GameHand newGameHand = new GameHand();
                    newGameHand.addCard(card2);

                    gameHand.getCards().remove(card2);
                    Card newCard = aCardShoe.popFirstCard();
                    gameHand.getCards().add(newCard);
                    System.out.println("> " + aPlayerGame.getPlayer().getName() + " receives " + newCard.getCardValueString() + ", total: " + String.valueOf(gameHand.getTotalCardsValue()));
                    System.out.println(aPlayerGame.getPlayer().getName() + " Game#" + String.valueOf(gameHandIndex) + ": " + gameHand.generateCardValueString(aShowCardSuit));

                    newCard = aCardShoe.popFirstCard();
                    newGameHand.getCards().add(newCard);
                    System.out.println("> " + aPlayerGame.getPlayer().getName() + " receives " + newCard.getCardValueString() + ", total: " + String.valueOf(newGameHand.getTotalCardsValue()));
                    System.out.println(aPlayerGame.getPlayer().getName() + " Game#" + String.valueOf(gameHandIndex+1) + ": " + newGameHand.generateCardValueString(aShowCardSuit));

                    aPlayerGame.addGameHand(newGameHand);
                    gameHandProcessQueue.addFirst(newGameHand);
                    gameHandProcessQueue.addFirst(gameHand);
                    gameHandIndex--;
                    break;
                }
                else if (isDoublePossible && inputString.equals("d")) {
                    Integer doubleBetTopupAmount = aPlayerGame.getBetAmount();
                    aPlayerGame.getPlayer().getWallet().decreaseValue(doubleBetTopupAmount.doubleValue());
                    aPlayerGame.setDoubleBet(true);

                    Card newCard = aCardShoe.popFirstCard();
                    gameHand.addCard(newCard);
                    System.out.println("> " + aPlayerGame.getPlayer().getName() + " receives " + newCard.getCardValueString() + ", total: " + String.valueOf(gameHand.getTotalCardsValue()));
                    break;
                }
            }
        }

    }

    private void reShoe(final CardShoe aCardShoe, final CardDiscardHolder aCardDiscardHolder, final Integer aShuffleCount) {
        // simulate physical card shuffling
        System.out.println("> shuffling cards and re-shoe!");
        ArrayList<Card> cards = new ArrayList<Card>(aCardShoe.popRemainingCards());
        for (Game game : aCardDiscardHolder.getGames()) {
            cards.addAll(game.getDealerHand().getCards());
            for (PlayerGame playerGame : game.getPlayerGames().values()) {
                for (GameHand gameHand : playerGame.getGameHands()) {
                    cards.addAll(gameHand.getCards());
                }
            }
        }
        aCardDiscardHolder.clearGames();

        aCardShoe.setCards(cards);
        aCardShoe.shuffleCards(aShuffleCount);
    }

    private Integer placeBet(final Scanner aScanner, final Player aPlayer, final Double aMinimumBetAmount) {
        // assuming bets to be place in whole number
        Integer betAmount = 0;
        while (!(betAmount >= aMinimumBetAmount.intValue())) {
            System.out.println("=====================================");

            System.out.print("< Bet amount? (" + String.valueOf(aMinimumBetAmount.intValue()) + "-" + String.valueOf(aPlayer.getWallet().getTotalValue().intValue()) + "), q to quit: ");

            String inputString = aScanner.nextLine();

            if (inputString.equals("q")) {
                exitGame();
            }

            try {
                betAmount = Integer.parseInt(inputString);
                if (betAmount > aPlayer.getWallet().getTotalValue()) {
                    System.out.println("> Bet amount, " + String.valueOf(betAmount) + ", exceed total chips, using highest available chips of " + String.valueOf(aPlayer.getWallet().getTotalValue()));
                    betAmount = aPlayer.getWallet().getTotalValue().intValue();
                }

                try {
                    aPlayer.getWallet().decreaseValue(betAmount.doubleValue());
                } catch (NegativeWalletValueException e) {
                    // TODO
                }
            }
            catch (NumberFormatException e) {
                System.out.println("> Invalid input!");
            }

            if (betAmount < aMinimumBetAmount) {
                System.out.println("> Minimum bet of " + String.valueOf(aMinimumBetAmount.intValue()));
            }
        }

        return betAmount;
    }

    private void exitGame() {
        System.out.println("Exiting...");
        System.exit(0);
    }

    private Game dealCards(final CardShoe aCardShoe, final Dealer aDealer, final ArrayList<Player> aPlayers, final Integer aBetAmount) {
        Game newGame = new Game(aDealer);
        GameHand dealerHand = new GameHand();
        newGame.setDealerHand(dealerHand);

        for (int i=0; i < BLACKJACK_INITIAL_DEALED_CARDS_COUNT; i++) {
            dealerHand.addCard(aCardShoe.popFirstCard());

            for (Player player : aPlayers) {
                PlayerGame playerGame = newGame.getPlayerGame(player);
                if (playerGame == null) {
                    playerGame = new PlayerGame(player, aBetAmount);
                    newGame.addPlayerGame(player, playerGame);
                }
                for (GameHand gameHand : newGame.getPlayerGame(player).getGameHands()) {
                    gameHand.addCard(aCardShoe.popFirstCard());
                }
            }
        }

        return newGame;
    }

    private void printGameProgress(final GameHand aDealerGameHand, final String aPlayerName,
                                   final GameHand aPlayerGameHand, final Integer aPlayerGameHandIndex,
                                   final Boolean aShowCardSuit) {
        System.out.println("Dealer: " + aDealerGameHand.generateDealerProgressCardValueString(UNKNOWN_CARD_VALUE_STRING, aShowCardSuit));
        System.out.println(aPlayerName + " Game#" + String.valueOf(aPlayerGameHandIndex) + ": " + aPlayerGameHand.generateCardValueString(aShowCardSuit));
    }

    private void printDiscardedCards(final CardDiscardHolder aCardDiscardHolder) {
        int cardDiscardedCount = 0;
        for (Game game : aCardDiscardHolder.getGames()) {
            cardDiscardedCount += game.getDealerHand().getCards().size();
            for (PlayerGame playerGame : game.getPlayerGames().values()) {
                for (GameHand gameHand : playerGame.getGameHands()) {
                    cardDiscardedCount += gameHand.getCards().size();
                }
            }
        }
        System.out.println(String.valueOf(cardDiscardedCount) + " cards in discard holder");
    }
}
