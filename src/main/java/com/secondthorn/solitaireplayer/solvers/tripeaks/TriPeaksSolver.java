package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * An interface for TriPeaks Solitaire solvers.
 */
public interface TriPeaksSolver {
    /**
     * Evaluates a deck for TriPeaks Solitaire (some cards may be unknown) and returns lists of steps to take to play
     * the game towards a goal. There may be one or more solutions, even if there's nothing that can be done there
     * will be a Solution object that says that.
     *
     * @param deck          a deck of cards for TriPeaks Solitaire
     * @param startingState the starting state for the solver to begin at (might not be the beginning of the game)
     * @return a list of one or more Solution objects (some may indicate there is nothing possible to do)
     */
    List<Solution> solve(Deck deck, int startingState);

    /**
     * Given a state and a mapping from state to previous state, return a list of Actions to go from the start of the
     * game to the given state.
     *
     * @param state      the resulting state
     * @param seenStates a mapping from state to previous state
     * @param deck       a deck of cards for TriPeaks Solitaire
     * @return a list of Actions to go from the start of the game to the resulting state
     */
    static List<Action> actions(int state, TIntIntMap seenStates, Deck deck) {
        ArrayDeque<Action> actions = new ArrayDeque<>();
        while (seenStates.containsKey(state)) {
            actions.push(new Action(deck.cardAt(State.getWasteIndex(state)), deck));
            state = seenStates.get(state);
        }
        return new ArrayList<>(actions);
    }

    /**
     * Returns a list of Actions in order to lose the game quickly, when there is no solution.
     * @param deck a deck of cards for TriPeaks Solitaire
     * @return a list of Actions to lose quickly in TriPeaks Solitaire
     */
    static List<Action> loseQuicklyActions(Deck deck) {
        List<Action> actions = new ArrayList<>();
        for (int i=29; i<52; i++) {
            actions.add(new Action(deck.cardAt(i), deck));
        }
        return actions;
    }
}
