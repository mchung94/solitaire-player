package com.secondthorn.solitaireplayer.players;

import com.secondthorn.solitaireplayer.players.pyramid.PyramidPlayer;

import java.util.Arrays;

/**
 * An abstract base class to represent Sikuli-automated solitaire players. Each subclass should
 * use Sikuli to automatically play a running instance of Microsoft Solitaire Collection on Windows 10.
 * They should not implement game-solving logic themselves, but instead consult solvers in the solvers package
 * to find out what to look for and click on.
 */
public abstract class SolitairePlayer {

    /**
     * A static factory method to instantiate Solitaire Players.
     * This throws IllegalArgumentException to indicate command line argument problems.
     * @param args command line args
     * @return a new instance of a concrete subclass of SolitairePlayer
     */
    public static SolitairePlayer newInstance(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Too few arguments to create a solitaire player.");
        }
        String game = args[0];
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        SolitairePlayer player;
        switch (game) {
            case "Klondike":
            case "Spider":
            case "FreeCell":
            case "TriPeaks":
                throw new IllegalArgumentException(game + " is not implemented yet.");
            case "Pyramid":
                player = new PyramidPlayer(remainingArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown game: " + game);
        }
        return player;
    }

    /**
     * A utility method for subclasses to parse command line strings into ints.
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
     * Subclasses implement this method to automate playing a solitaire game that has just started.
     */
    public abstract void play();
}
