package com.secondthorn.solitaireplayer.players;

import com.secondthorn.solitaireplayer.players.pyramid.PyramidPlayer;
import com.secondthorn.solitaireplayer.players.tripeaks.TriPeaksPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract base class to represent SikuliX-automated solitaire players. Each subclass should
 * use SikuliX to automatically play a running instance of Microsoft Solitaire Collection on Windows 10.
 * They should not implement game-solving logic themselves, but instead consult solvers in the solvers package
 * to find out what to look for and click on.
 */
public abstract class SolitairePlayer {
    /**
     * Subclasses implement this method to automate playing a solitaire game that has just started.
     */
    public abstract void autoplay() throws PlayException, InterruptedException;

    /**
     * Subclasses implement this method to read the filename and print out steps to follow without automation.
     */
    public abstract void preview(String filename) throws PlayException, InterruptedException;

    /**
     * Instantiates and returns supported Solitaire Players.
     *
     * @param args command line args
     * @return a new instance of a concrete subclass of SolitairePlayer
     * @throws IllegalArgumentException if there is a command line argument problem
     */
    public static SolitairePlayer newInstance(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Too few arguments to create a solitaire player.");
        }
        String game = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        SolitairePlayer player;
        switch (game) {
            case "Klondike":
            case "Spider":
            case "FreeCell":
                throw new IllegalArgumentException(game + " is not implemented yet.");
            case "TriPeaks":
                player = new TriPeaksPlayer(args);
                break;
            case "Pyramid":
                player = new PyramidPlayer(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown game: " + game);
        }
        return player;
    }

    /**
     * Returns command line strings as ints.
     *
     * @param s a command line argument
     * @return an int value of the argument
     */
    protected int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The command line arg \"" + s + "\" must be a number.");
        }
    }

    /**
     * Returns command line arguments as card ranks.
     * A card rank must be one of the following chars: A 2 3 4 5 6 7 8 9 T J Q K.
     *
     * @param s a command line argument
     * @return a char card rank of the argument
     */
    protected char parseCardRank(String s) {
        if ((s.length() == 1) && "A23456789TJQK".contains(s)) {
            return s.charAt(0);
        } else {
            throw new IllegalArgumentException("The command line arg \"" + s + "\" must be a card rank.");
        }
    }

    /**
     * Returns a list of cards from a standard 52 card deck that are missing from the given list of cards.
     *
     * @param cards a list of cards being played in Pyramid Solitaire
     * @return a list (possibly empty) of the cards missing in the Deck
     */
    protected List<String> missingCards(List<String> cards) {
        List<String> missingCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>(cards);
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                String card = String.format("%s%s", rank, suit);
                if (!cardSet.contains(card)) {
                    missingCards.add(card);
                }
            }
        }
        return missingCards;
    }

    /**
     * Returns a list of duplicate cards found in the list of cards, ignoring unknown cards.
     *
     * @param cards a list of cards
     * @return a list of the duplicate cards in the given card list
     */
    protected List<String> duplicateCards(List<String> cards) {
        List<String> duplicateCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>();
        for (String card : cards) {
            if (cardSet.contains(card)) {
                duplicateCards.add(card);
            } else {
                if (!card.equals("??")) {
                    cardSet.add(card);
                }
            }
        }
        return duplicateCards;
    }

    /**
     * Returns a list of malformed cards, ignoring unknown cards.
     *
     * @param cards a list of cards
     * @return a list of the malformed cards in the given card list
     */
    protected List<String> malformedCards(List<String> cards) {
        List<String> malformedCards = new ArrayList<>();
        for (String card : cards) {
            if (!(isUnknownCard(card) || isCard(card))) {
                malformedCards.add(card);
            }
        }
        return malformedCards;
    }

    /**
     * Returns the number of unknown cards, represented by "??".
     * @param cards a list of cards
     * @return the number of unknown cards in the given card list
     */
    protected long numUnknownCards(List<String> cards) {
        return cards.stream().filter(this::isUnknownCard).count();
    }

    /**
     * Returns a list of cards contained in the given file.
     *
     * @param filename the filename containing a deck of cards
     * @return a list of the cards in the file
     */
    protected List<String> readCardsFromFile(String filename) throws PlayException {
        try {
            return Arrays.asList(new String(Files.readAllBytes(Paths.get(filename))).trim().split("\\s+"));
        } catch (IOException ex) {
            throw new PlayException("Unable to read " + filename, ex);
        }
    }

    /**
     * Returns true if the card is an unknown card.
     */
    private boolean isUnknownCard(String card) {
        return card.equals("??");
    }

    /**
     * Returns true if the object is a card (a two letter string containing a rank followed by a suit).
     */
    private boolean isCard(String card) {
        return (card != null) && (card.length() == 2) &&
                ("A23456789TJQK".indexOf(card.charAt(0)) != -1) &&
                ("cdhs".indexOf(card.charAt(1)) != -1);
    }
}
