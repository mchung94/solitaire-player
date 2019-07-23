package com.secondthorn.solitaireplayer.solvers.tripeaks;

import java.util.List;

/**
 * A solution returned by the TriPeaks solvers.  These are a list of Actions.  It also contains a description of the
 * solution, as well as a flag to indicate if this is the best solution possible, or if there are unknown cards
 * preventing the solver from knowing if this solution is really the optimal solution.
 */
public class Solution {
    private String description;
    private boolean definitiveSolution;
    private List<Action> actions;
    private int endingState;
    private int previousState;

    /**
     * Creates a Solution indicating a list of steps to play and other details around the list.
     *
     * @param description        a human-readable description of what the solution accomplishes
     * @param definitiveSolution true if there is no better solution or false if a better solution may exist
     * @param actions            a list of Actions to reach a goal
     * @param endingState        the ending state after following the actions
     * @param previousState      the state before the endingState (when undoing the last move is necessary)
     */
    public Solution(String description, boolean definitiveSolution, List<Action> actions, int endingState, int previousState) {
        this.description = description;
        this.definitiveSolution = definitiveSolution;
        this.actions = actions;
        this.endingState = endingState;
        this.previousState = previousState;
    }

    /**
     * Returns a human-readable description of what the solution accomplishes.
     *
     * @return a string describing the solution
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns true if there cannot exist a better solution, or false if there may be a better solution, but unknown
     * cards prevent it from being known yet.  This helps determine if we need to turn over any more face down cards
     * to find a true solution.
     *
     * @return a boolean flag indicating if it's the best possible solution or not
     */
    public boolean isDefinitiveSolution() {
        return definitiveSolution;
    }

    /**
     * Returns a list of Actions to reach a goal.
     *
     * @return a list of Actions for the player to take
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Returns the ending state after following the list of Actions.
     *
     * @return the ending state
     */
    public int getEndingState() {
        return endingState;
    }

    /**
     * Returns the state before the ending state after following the list of Actions.
     * This is used when we have to undo the last move because there are no more possible moves.
     *
     * @return the state one step before the ending state
     */
    public int getPreviousState() {
        return previousState;
    }
}
