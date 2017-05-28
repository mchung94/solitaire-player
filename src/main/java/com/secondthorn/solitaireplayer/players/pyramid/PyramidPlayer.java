package com.secondthorn.solitaireplayer.players.pyramid;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.SolitairePlayer;
import com.secondthorn.solitaireplayer.solvers.pyramid.BoardChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.pyramid.CardChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.pyramid.PyramidSolver;
import com.secondthorn.solitaireplayer.solvers.pyramid.ScoreChallengeSolver;

/**
 * A class that uses Sikuli to automate playing a game of Pyramid Solitaire in
 * Windows 10's Microsoft Solitaire Collection.
 */
public class PyramidPlayer extends SolitairePlayer {
    private PyramidSolver solver;

    /**
     * Create a new PyramidPlayer instance based on command line args.
     * It will throw IllegalArgumentException if the args don't make sense.
     * The player can specialize in either clearing the board/table, maximizing score, or
     * clearing cards of a certain rank.
     * @param args command line args
     */
    public PyramidPlayer(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing goal for Pyramid Solitaire");
        }
        String goalType = args[0];
        switch (goalType) {
            case "Board":
                if (args.length != 1) {
                    throw new IllegalArgumentException("Wrong number of args for Pyramid Solitaire Board Challenge");
                }
                solver = new BoardChallengeSolver();
                break;
            case "Score":
                if (args.length != 3) {
                    throw new IllegalArgumentException("Wrong number of args for Pyramid Solitaire Score Challenge");
                }
                int goalScore = parseInt(args[1]);
                int currentScore = parseInt(args[2]);
                solver = new ScoreChallengeSolver(goalScore, currentScore);
                break;
            case "Card":
                if (args.length != 4) {
                    throw new IllegalArgumentException("Wrong number of args for Pyramid Solitaire Card Challenge");
                }
                int goalNumCardsToClear = parseInt(args[1]);
                char cardRankToClear = parseCardRank(args[2]);
                int currentNumCardsCleared = parseInt(args[3]);
                solver = new CardChallengeSolver(goalNumCardsToClear, cardRankToClear, currentNumCardsCleared);
                break;
            default:
                throw new IllegalArgumentException("Unknown goal for Pyramid Solitaire: " + goalType);
        }
    }

    /**
     * Play a Pyramid Solitaire game.
     */
    public void play() {
        MSCWindow.positionForPlay();
    }
}
