package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Pyramid Solitaire Score Challenge solver.
 * <p>
 * Score challenges take the form: "Earn a score of N", e.g. "Earn a score of 2,400".
 * The solver searches for a solution that maximizes the score in the minimum number of steps.
 * Note that this may be more steps than the minimum required to clear the board.  Because
 * clearing the board maximizes the score, if it possible to clear the board the solution will do it.
 */
public class ScoreChallengeSolver implements PyramidSolver {
    /**
     * The maximum possible score if you remove every card in the whole deck before
     * clearing the table.  Note: I think if you clear the table you don't get to play anymore
     * to remove any more cards in the deck or waste piles.
     */
    private static final int MAX_POSSIBLE_SCORE = 1290;

    /**
     * Bit masks to apply to a state to see if a row has been cleared, starting from
     * the bottom row to the top of the pyramid.
     */
    private static final long[] ROW_CLEARED_MASKS = {
            0b1111111000000000000000000000L,
            0b0000000111111000000000000000L,
            0b0000000000000111110000000000L,
            0b0000000000000000001111000000L,
            0b0000000000000000000000111000L,
            0b0000000000000000000000000110L,
            0b0000000000000000000000000001L
    };

    /**
     * Points awarded for clearing each row of cards from the bottom row to the top
     * of the pyramid.
     */
    private static final int[] ROW_CLEARED_SCORES = {
            25,
            50,
            75,
            100,
            150,
            250,
            500
    };

    /**
     * The remaining number of points needed to win the challenge, goal score - current score.
     * If you reach this number of points, we quit searching for the maximum scoring solution
     * and use the solution we found.  Because it's breadth first search it should be the
     * minimum number of steps to reach this point score.
     */
    private int pointsNeeded;

    /**
     * The score of the solution found.  Programmers can query this value from the solver
     * after it finds a solution in the solve method.
     */
    private int bestScore;

    /**
     * Create a ScoreChallengeSolver to find the maximum scoring solution without a goal
     * score - just get the highest score possible.
     */
    public ScoreChallengeSolver() {
        pointsNeeded = MAX_POSSIBLE_SCORE;
    }

    /**
     * Create a ScoreChallengeSolver to find the maximum scoring solution or the solution
     * that will reach the goal score.
     * @param goalScore the goal score to win the challenge
     * @param currentScore the player's current score
     */
    public ScoreChallengeSolver(int goalScore, int currentScore) {
        if (currentScore > goalScore) {
            throw new IllegalArgumentException("The current score must be smaller than the goal score");
        }
        pointsNeeded = goalScore - currentScore;
    }

    /**
     * After running the solve method and finding a solution, this returns the score achieved by
     * the solution.
     * @return the score for the solution
     */
    public int getBestScore() {
        return bestScore;
    }

    /**
     * Search for the solution that gives the maximum score (not the shortest solution).
     * But if we can reach the goal score before clearing the table or reaching a dead end,
     * the solution returned will stop at the goal score.
     * @param deck a standard deck of 52 cards
     * @return a list of actions to perform to solve the game
     */
    public Map<String, List<Action>> solve(Deck deck) {
        Map<String, List<Action>> solutions = new HashMap<>();
        Deque<Node> fringe = new ArrayDeque<>();
        TLongSet seenStates = new TLongHashSet();
        long state = State.INITIAL_STATE;
        Node node = new Node(state, null);
        Node bestNode = null;
        bestScore = 0;
        fringe.addLast(node);
        while (fringe.size() > 0) {
            node = fringe.removeFirst();
            state = node.getState();
            TLongList successors = State.successors(state, deck);
            int score = score(state, deck);
            if ((score >= pointsNeeded) || (score == MAX_POSSIBLE_SCORE)) {
                bestNode = node;
                bestScore = score;
                break;
            }
            if (State.isTableClear(state) || (successors.size() == 0)) {
                if ((bestNode == null) || (score > bestScore)) {
                    bestNode = node;
                    bestScore = score;
                }
            } else {
                for (int i = 0, len = successors.size(); i < len; i++) {
                    long nextState = successors.get(i);
                    if (!seenStates.contains(nextState)) {
                        seenStates.add(nextState);
                        Node nextNode = new Node(nextState, node);
                        fringe.addLast(nextNode);
                    }
                }
            }
        }
        if (bestNode != null) {
            List<Action> solution = bestNode.actions(deck);
            String description;
            if (State.isTableClear(bestNode.getState())) {
                description = "Clear board, gain " + bestScore + " score in " + solution.size() + " steps";
            } else {
                description = "Can't clear board, gain " + bestScore + " score in " + solution.size() + " steps";
            }
            solutions.put(description, solution);
        }
        return solutions;
    }

    /**
     * Calculate the score for a given state, in order to find the best scoring solution
     * @param state a Pyramid Solitaire state
     * @param deck the Deck of 52 cards being used for Pyramid Solitaire
     * @return the current score at the given state
     */
    private int score(long state, Deck deck) {
        int score = 0;

        for (int i = 0; i < ROW_CLEARED_MASKS.length; i++) {
            if ((state & ROW_CLEARED_MASKS[i]) == 0) {
                score += ROW_CLEARED_SCORES[i];
            }
        }

        int numKingsRemoved = 0;
        int numNonKingsRemoved = 0;
        for (int i = 0; i < 52; i++) {
            long mask = 1L << i;
            if ((state & mask) == 0) {
                if (deck.isKing(i)) {
                    numKingsRemoved++;
                } else {
                    numNonKingsRemoved++;
                }
            }
        }
        score += (5 * numKingsRemoved) + (5 * (numNonKingsRemoved / 2));

        return score;
    }

}
