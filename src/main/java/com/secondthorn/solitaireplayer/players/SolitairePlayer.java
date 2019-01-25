package com.secondthorn.solitaireplayer.players;

import com.secondthorn.solitaireplayer.players.pyramid.PyramidPlayer;

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
    public abstract void autoplay() throws PlayException;

    /**
     * Subclasses implement this method to read the filename and print out steps to follow without automation.
     */
    public abstract void preview(String filename) throws PlayException;

    /**
     * A static factory method to instantiate Solitaire Players.
     * This throws IllegalArgumentException to indicate command line argument problems.
     *
     * @param args command line args
     * @return a new instance of a concrete subclass of SolitairePlayer
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
            case "TriPeaks":
                throw new IllegalArgumentException(game + " is not implemented yet.");
            case "Pyramid":
                player = new PyramidPlayer(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown game: " + game);
        }
        return player;
    }

    /**
     * A utility method for subclasses to parse command line strings into ints.
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
     * A utility method for subclasses to parse command line strings into card ranks
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
     * Check if a a list of cards is missing any of the standard 52 cards.
     *
     * @param cards a list of cards being played in Pyramid Solitaire
     * @return a list (possibly empty) of the cards missing in the Deck
     */
    protected List<String> missingCards(List<String> cards) {
        List<String> missingCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>(cards);
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                String card = "" + rank + suit;
                if (!cardSet.contains(card)) {
                    missingCards.add(card);
                }
            }
        }
        return missingCards;
    }

    /**
     * Return a list of duplicate cards found in the list of cards
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
                cardSet.add(card);
            }
        }
        return duplicateCards;
    }

    /**
     * Return a list of malformed cards, that aren't two letter strings with a
     * rank character (A23456789TJQK) and a suit character (cdhs).
     *
     * @param cards a list of cards
     * @return a list of the malformed cards in the given card list
     */
    protected List<String> malformedCards(List<String> cards) {
        List<String> malformedCards = new ArrayList<>();
        for (String card : cards) {
            if ((card == null) ||
                    (card.length() != 2) ||
                    ("A23456789TJQK".indexOf(card.charAt(0)) == -1) ||
                    ("cdhs".indexOf(card.charAt(1)) == -1)) {
                malformedCards.add(card);
            }
        }
        return malformedCards;
    }

    /**
     * Given a filename containing cards, read the cards into a list of cards,
     * which are two-letter strings.
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
}
