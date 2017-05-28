package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.List;

/**
 * A Pyramid Solitaire Card Challenge solver.
 * <p>
 * Card challenges take the form: "Clear N [Rank] in M deals", e.g. "Clear 12 Aces in 2 deals".
 * This solver is the most complicated because sometimes there isn't one right solution.  There may
 * be games where you can either clear the board and clear few cards of the relevant rank, or
 * remove more cards of the relevant rank but get stuck and not be able to clear the board.  The
 * only way to know the correct choice is to know what the cards in the next game is.
 * <p>
 * This solver will look for two solutions: one where you remove as many goal cards as possible but clear the board,
 * and one where you remove as many goal cards as possible but don't clear the board.
 * <p>
 * If it can reach the goal without clearing the board, it will figure out how to do so in the minimum number of steps.
 * <p>
 * Otherwise, if it's impossible to clear the board, it will just maximize removing goal cards.
 * <p>
 * If it is possible to clear the board, and it is the better solution, it will just return that solution.
 * <p>
 * When it's not clear what's a better solution, it will return both solutions for the player to decide.
 */
public class CardChallengeSolver implements PyramidSolver {
    private int numCardsToClear;
    private char cardRankToClear;

    public CardChallengeSolver(int goalNumCardsToClear, char cardRankToClear, int currentNumCardsCleared) {
        if (currentNumCardsCleared > goalNumCardsToClear) {
            throw new IllegalArgumentException("The current number of cards cleared must be smaller than the goal number");
        }
        this.numCardsToClear = goalNumCardsToClear - currentNumCardsCleared;
        this.cardRankToClear = cardRankToClear;
    }

    public List<List<Action>> solve(Deck deck) {
        return null;
    }
}
