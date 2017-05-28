package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.List;

/**
 * A Pyramid Solitaire Board Challenge solver.
 * <p>
 * Board challenges take the form: "Clear N board(s) in M deal(s)", e.g. "Clear 3 board(s) in 2 deal(s)".
 * This solver figures out how to clear boards or skip them in the fewest number of Actions possible.
 * Boards are cleared by removing all 28 cards on the table, the deck and waste piles don't matter.
 * <p>
 * If it's possible to clear the board, it will determine how to do so in the minimum number of steps.
 * <p>
 * If it's impossible to clear the board, it will determine how to reach a dead end with no more possible
 * moves in the minimum number of steps.  Most likely, it will involve drawing and recycling cards over and over
 * until the player can't do it anymore.  There's no attempt to maximize score.
 */
public class BoardChallengeSolver implements PyramidSolver {

    public BoardChallengeSolver() {
    }

    public List<List<Action>> solve(Deck deck) {
        return null;
    }
}
