package com.secondthorn.solitaireplayer.players;

import com.secondthorn.solitaireplayer.players.freecell.FreeCellPlayer;
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

import static org.sikuli.script.Sikulix.inputText;

/**
 * An abstract base class to represent SikuliX-automated solitaire players. Each subclass should
 * use SikuliX to automatically play a running instance of Microsoft Solitaire Collection on Windows 10.
 * They should not implement game-solving logic themselves, but instead consult solvers in the solvers package
 * to find out what to look for and click on.
 */
public abstract class SolitairePlayer {
    /**
     * Automates playing a solitaire game that has just started.
     */
    public abstract void autoplay() throws PlayException, InterruptedException;

    /**
     * Reads the filename and prints out steps to follow without automation.
     */
    public abstract void preview(String filename) throws PlayException, InterruptedException;

    /**
     * Returns true if the list of cards is valid for the game of Solitaire being played.
     */
    protected abstract boolean isValidCards(List<String> cards,
                                            List<String> missing,
                                            List<String> duplicates,
                                            List<String> malformed,
                                            long numUnknownCards);

    /**
     * Returns a string representation of the cards in the Solitaire game.
     */
    protected abstract String cardsToString(List<String> cards);

    /**
     * Show verification/confirmation prompts so you can check what it's doing.
     */
    protected boolean showPrompts = false;

    /**
     * Instantiates and returns supported Solitaire Players.
     *
     * @param args command line args
     * @return a new instance of a concrete subclass of SolitairePlayer
     * @throws IllegalArgumentException if there is a command line argument problem
     */
    public static SolitairePlayer newInstance(String[] args) {
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        boolean showPrompts = false;
        for (int i=0; i<argsList.size(); i++) {
            if (argsList.get(i).equalsIgnoreCase("--show-prompts")) {
                showPrompts = true;
                argsList.remove(i);
                break;
            }
        }
        if (argsList.size() < 1) {
            throw new IllegalArgumentException("Too few arguments to create a solitaire player.");
        }
        String game = argsList.get(0);
        argsList.remove(0);
        args = argsList.toArray(new String[0]);
        SolitairePlayer player;
        switch (game.toLowerCase()) {
            case "klondike":
            case "spider":
                throw new IllegalArgumentException(game + " is not implemented yet.");
            case "freecell":
                player = new FreeCellPlayer(args);
                break;
            case "tripeaks":
                player = new TriPeaksPlayer(args);
                break;
            case "pyramid":
                player = new PyramidPlayer(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown game: " + game);
        }
        player.showPrompts = showPrompts;
        return player;
    }

    /**
     * Ask the user to verify and edit the list of cards being used for the game.
     *
     * @param cards a list of cards
     * @return an updated list of cards that passes verification checks
     * @throws PlayException if the user cancels verification and wants to exit
     */
    public List<String> verifyCards(List<String> cards) throws PlayException {
        List<String> missing = missingCards(cards);
        List<String> duplicates = duplicateCards(cards);
        List<String> malformed = malformedCards(cards);
        long numUnknownCards = numUnknownCards(cards);
        if (showPrompts) {
            do {
                String message = "Please verify the list of cards is correct and edit if necessary.";
                message += "\nClick Cancel to quit.";
                message += "\nNumber of Cards: " + cards.size();
                message += "\nMissing Cards: " + ((missing.size() > 0) ? missing : "None");
                message += "\nDuplicate Cards: " + ((duplicates.size() > 0) ? duplicates : "None");
                message += "\nMalformed Cards: " + ((malformed.size() > 0) ? malformed : "None");
                message += "\nNumber of Unknown Cards: " + (numUnknownCards > 0 ? numUnknownCards : "None");
                String newDeckText = inputText(message, "Deck Verification", 8, 120, cardsToString(cards));
                if (newDeckText == null) {
                    throw new PlayException("User cancelled verification of deck cards and the program will quit.");
                }
                cards = new ArrayList<>(Arrays.asList(newDeckText.trim().split("\\s+")));
                missing = missingCards(cards);
                duplicates = duplicateCards(cards);
                malformed = malformedCards(cards);
                numUnknownCards = numUnknownCards(cards);

            } while (!isValidCards(cards, missing, duplicates, malformed, numUnknownCards));
        } else {
            if (!isValidCards(cards, missing, duplicates, malformed, numUnknownCards)) {
                String message = "The cards don't make sense. Try again with --show-prompts to tell it the right cards.";
                message += "\nNumber of Cards: " + cards.size();
                message += "\nMissing Cards: " + ((missing.size() > 0) ? missing : "None");
                message += "\nDuplicate Cards: " + ((duplicates.size() > 0) ? duplicates : "None");
                message += "\nMalformed Cards: " + ((malformed.size() > 0) ? malformed : "None");
                message += "\nNumber of Unknown Cards: " + (numUnknownCards > 0 ? numUnknownCards : "None");
                throw new PlayException(message);
            }
        }
        return cards;
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
     * Lower case input will be converted to upper case.
     *
     * @param s a command line argument
     * @return a char card rank of the argument
     */
    protected char parseCardRank(String s) {
        s = s.toUpperCase();
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
     *
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
            throw new PlayException("Unable to read file \"" + filename + "\".", ex);
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
