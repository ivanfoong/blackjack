package com.ivanfoong.insightdataengineering.blackjack;

/**
 * Created by ivanfoong on 4/4/14
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
 * insurance? (1-99) / no: no
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
 */

import com.ivanfoong.insightdataengineering.blackjack.card.Card;
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class Blackjack {
    private static final Integer CARD_SHOE_NUMBER_OF_DECKS = 1;
    private static final Integer CARD_SHOE_MINIMUM_CARDS_COUNT = 13;
    private static final Integer DEALER_STARTING_TOTAL_CHIP_VALUE = 1000;
    private static final Integer PLAYER_STARTING_TOTAL_CHIP_VALUE = 100;
    private static final Integer BLACKJACK_INITIAL_DEALED_CARDS_COUNT = 2;
    private static final String UNKNOWN_CARD_VALUE_STRING = "?";
    private static final Integer DEALER_STAND_ON_TOTAL_CARDS_VALUE = 17;
    private static final boolean DEBUGGING = true;

    public static void main(final String[] aArguments) {
        startBlackjack();
    }

    private static void startBlackjack() {
        // START setup
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i=0; i < CARD_SHOE_NUMBER_OF_DECKS; i++) {
            cards.addAll(Deck.getStandardDeck().getCards());
        }
        CardShoe cardShoe = new CardShoe(cards, CARD_SHOE_MINIMUM_CARDS_COUNT);
        cardShoe.shuffleCards(); // using automatic card shuffler and shoe
        CardDiscardHolder cardDiscardHolder = new CardDiscardHolder();
        Dealer dealer = new Dealer(DEALER_STARTING_TOTAL_CHIP_VALUE);
        Player player = new Player(PLAYER_STARTING_TOTAL_CHIP_VALUE, "player");

        Scanner scanner = new Scanner(System.in);
        // END setup

        while (startGame(scanner, cardShoe, cardDiscardHolder, dealer, player)) {}

        // START cleanup
        scanner.close();
        // END cleanup

        System.out.println("Player have no more chips, blackjack game ended.");
    }

    private static boolean startGame(final Scanner aScanner, final CardShoe aCardShoe, final CardDiscardHolder aCardDiscardHolder, final Dealer aDealer, final Player aPlayer) {
        if (aCardShoe.requireNewShoe()) {
            reShoe(aCardShoe, aCardDiscardHolder);
        }

        Integer betAmount = placeBet(aScanner, aPlayer);

        ArrayList<Player> players = new ArrayList<Player>();
        players.add(aPlayer);

        Game currentGame = dealCards(aCardShoe, aDealer, players, betAmount);
        printGameProgress(currentGame);

        // query user actions
        boolean allPlayerBust = true;
        Enumeration<Player> playerEnumeration = currentGame.getPlayerGames().keys();
        while (playerEnumeration.hasMoreElements()) {
            Player player = playerEnumeration.nextElement();
            PlayerGame playerGame = currentGame.getPlayerGame(player);
            playerAction(aScanner, playerGame, aCardShoe);
            if (!playerGame.hasBust()) {
                allPlayerBust = false;
            }
        }


        if (!allPlayerBust) {
            // process dealer actions
            dealerAction(currentGame.getDealerHand(), aCardShoe);
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

    private static void resolveGame(final Game aGame) {
        Integer dealerValue = aGame.getDealerHand().getTotalCardsValue();

        Enumeration<Player> playerEnumeration = aGame.getPlayerGames().keys();

        while (playerEnumeration.hasMoreElements()) {
            Player player = playerEnumeration.nextElement();
            PlayerGame playerGame = aGame.getPlayerGame(player);

            Integer playerValue = playerGame.getGameHand().getTotalCardsValue();

            GameStatus gameStatus;
            if (playerValue <= 21) {
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

            final Integer betAmount = playerGame.getBetAmount();
            String status = "";
            switch (gameStatus) {
                case WIN: {
                    status = "win, " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                    player.getWallet().increaseValue(betAmount*2);
                    aGame.getDealer().getWallet().decreaseValue(betAmount);
                    break;
                }
                case LOSE: {
                    status = "lose, " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                    aGame.getDealer().getWallet().increaseValue(betAmount);
                    break;
                }
                case PUSH: {
                    status = "draw, " + String.valueOf(playerValue) + "(" + playerGame.getPlayer().getName() + ") vs " + String.valueOf(dealerValue) + "(dealer)";
                    player.getWallet().increaseValue(betAmount);
                    break;
                }
                case BUST: {
                    status = "bust";
                    aGame.getDealer().getWallet().increaseValue(betAmount);
                    break;
                }
                default: {
                    break;
                }
            }
            System.out.println(player.getName() + " " + status + "!");
        }
    }

    private static void dealerAction(final GameHand aDealerGameHand, final CardShoe aCardShoe) {
        System.out.println("Dealer: " + generateCardValueString(aDealerGameHand));

        while (aDealerGameHand.getTotalCardsValue() < DEALER_STAND_ON_TOTAL_CARDS_VALUE) {
            System.out.println("Dealer hits!");
            aDealerGameHand.addCard(aCardShoe.popFirstCard());
            System.out.println("Dealer: " + generateCardValueString(aDealerGameHand));
        }
    }

    private static void playerAction(final Scanner aScanner, final PlayerGame aPlayerGame, final CardShoe aCardShoe) {
        String inputString = "";

        while(aPlayerGame.getGameHand().getTotalCardsValue() < 21 && !inputString.equals("s")) {
            System.out.print("h(hit) / s(stand) / sp(split) / d(double) / q(quit)?: ");
            inputString = aScanner.nextLine();

            if (inputString.equals("h")) {
                aPlayerGame.getGameHand().addCard(aCardShoe.popFirstCard());
                System.out.println(aPlayerGame.getPlayer().getName() + ": " + generateCardValueString(aPlayerGame.getGameHand()));
            }
            else if (inputString.equals("sp")) {
                // TODO
                System.out.println("Not implemented yet!");
            }
            else if (inputString.equals("d")) {
                // TODO
                System.out.println("Not implemented yet!");
            }
            else if (inputString.equals("q")) {
                System.exit(0);
            }
        }
    }

    private static void reShoe(final CardShoe aCardShoe, final CardDiscardHolder aCardDiscardHolder) {
        // simulate physical card shuffling // TODO allow more efficient re-shoe
        System.out.println("shuffling cards and re-shoe1");
        ArrayList<Card> cards = new ArrayList<Card>(aCardShoe.popRemainingCards());
        for (Game game : aCardDiscardHolder.getGames()) {
            cards.addAll(game.getDealerHand().getCards());
            for (PlayerGame playerGame : game.getPlayerGames().values()) {
                cards.addAll(playerGame.getGameHand().getCards());
            }
        }
        aCardDiscardHolder.clearGames();

        aCardShoe.setCards(cards);
        aCardShoe.shuffleCards();
    }

    private static Integer placeBet(final Scanner aScanner, final Player aPlayer) {
        Integer betAmount = 0;
        while (!(betAmount > 0)) {
            System.out.println("=======================");

            System.out.print("Bet amount? (1-" + String.valueOf(aPlayer.getWallet().getTotalValue()) + "), q to quit: ");

            String inputString = aScanner.nextLine();

            if (inputString.equals("q")) {
                exitGame();
            }

            betAmount = Integer.parseInt(inputString);
            if (betAmount > aPlayer.getWallet().getTotalValue()) {
                System.out.println("Bet amount, " + String.valueOf(betAmount) + ", exceed total chips, using highest available chips of " + String.valueOf(aPlayer.getWallet().getTotalValue()));
                betAmount = aPlayer.getWallet().getTotalValue();
            }

            try {
                aPlayer.getWallet().decreaseValue(betAmount);
            }
            catch (NegativeWalletValueException e) {
                // TODO
            }
        }

        return betAmount;
    }

    private static void exitGame() {
        System.out.println("Exiting...");
        System.exit(0);
    }

    private static Game dealCards(final CardShoe aCardShoe, final Dealer aDealer, final ArrayList<Player> aPlayers, final Integer aBetAmount) {
        Game newGame = new Game(aDealer);
        GameHand dealerHand = new GameHand();
        newGame.setDealerHand(dealerHand);

        for (int i=0; i < BLACKJACK_INITIAL_DEALED_CARDS_COUNT; i++) {
            dealerHand.addCard(aCardShoe.popFirstCard());

            for (Player player : aPlayers) {
                PlayerGame playerGame = newGame.getPlayerGame(player);
                if (playerGame == null) {
                    playerGame = new PlayerGame(player, new GameHand(), aBetAmount);
                    newGame.addPlayerGame(player, playerGame);
                }
                newGame.getPlayerGame(player).getGameHand().addCard(aCardShoe.popFirstCard());
            }
        }

        return newGame;
    }

    private static void printGameProgress(final Game aGame) {
        GameHand dealerHand = aGame.getDealerHand();
        System.out.println("Dealer: " + generateDealerProgressCardValueString(dealerHand));

        // create enumeration for keys
        Enumeration<Player> playerEnumeration = aGame.getPlayerGames().keys();

        // display search result
        while (playerEnumeration.hasMoreElements()) {
            Player player = playerEnumeration.nextElement();

            //You: A A (12)
            PlayerGame playerGame = aGame.getPlayerGame(player);

            System.out.println(player.getName() + ": " + generateCardValueString(playerGame.getGameHand()));
        }
    }

    private static void printDiscardedCards(final CardDiscardHolder aCardDiscardHolder) {
        int cardDiscardedCount = 0;
        for (Game game : aCardDiscardHolder.getGames()) {
            cardDiscardedCount += game.getDealerHand().getCards().size();
            for (PlayerGame playerGame : game.getPlayerGames().values()) {
                cardDiscardedCount += playerGame.getGameHand().getCards().size();
            }
        }
        System.out.println(String.valueOf(cardDiscardedCount) + " cards in discard holder");
    }

    private static String generateDealerProgressCardValueString(final GameHand aDealerHand) {
        return aDealerHand.getCards().get(0).getCardValueString() + "," + UNKNOWN_CARD_VALUE_STRING;
    }

    private static String generateCardValueString(final GameHand aGameHand) {
        StringBuilder individualCardValueStringBuilder = new StringBuilder();

        for (Card card : aGameHand.getCards()) {
            if (individualCardValueStringBuilder.length() > 0) {
                individualCardValueStringBuilder.append(",");
            }
            individualCardValueStringBuilder.append(card.getCardValueString());
        }

        return individualCardValueStringBuilder.toString() + " (" + aGameHand.getTotalCardsValue() + ")";
    }

    private static void handleInput(final String aInputString) {
        System.out.println(aInputString);
    }
}