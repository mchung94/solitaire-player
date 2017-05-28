package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.List;

/**
 * A Pyramid Solitaire Score Challenge solver.
 * <p>
 * Score challenges take the form: "Earn a score of N", e.g. "Earn a score of 2,400".
 * The solver searches for a solution that maximizes the score in the minimum number of steps.
 * Note that this may be more steps than the minimum required to clear the board.  Because
 * clearing the board maximizes the score, if it possible to clear the board the solution will do it.
 */

public class ScoreChallengeSolver implements PyramidSolver {
    private int pointsNeeded;

    public ScoreChallengeSolver(int goalScore, int currentScore) {
        if (currentScore > goalScore) {
            throw new IllegalArgumentException("The current score must be smaller than the goal score");
        }
        pointsNeeded = goalScore - currentScore;
    }

    public List<List<Action>> solve(Deck deck) {
        return null;
    }
}
