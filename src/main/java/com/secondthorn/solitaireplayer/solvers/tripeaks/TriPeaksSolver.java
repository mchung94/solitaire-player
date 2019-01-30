package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An interface for TriPeaks Solitaire solvers.
 */
public interface TriPeaksSolver {
    /**
     * Evaluates a deck for TriPeaks Solitaire (some cards may be unknown) and returns lists of steps to take to play
     * the game towards a goal. There may be no solution or there may be multiple solutions, so each solution is keyed
     * by a description of the solution.
     * @param deck a deck of cards for TriPeaks Solitaire
     * @return a mapping from solution descriptions to lists of cards to move to the waste pile
     */
    Map<String, List<String>> solve(Deck deck);

    /**
     * Given a state and a mapping from state to previous state, return a list of cards to click from the start of the
     * game to reach the given state.
     * @param state the resulting state
     * @param seenStates a mapping from state to previous state
     * @param deck a deck of cards for TriPeaks Solitaire
     * @return a list of cards to click to get from the start of the game to the resulting state
     */
    static List<String> cards(int state, TIntIntMap seenStates, Deck deck) {
        ArrayDeque<String> cardsToClick = new ArrayDeque<>();
        while (seenStates.containsKey(state)) {
            cardsToClick.push(deck.cardAt(State.getWasteIndex(state)));
            state = seenStates.get(state);
        }
        return new ArrayList<>(cardsToClick);
    }
}
