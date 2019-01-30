package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solves Board Challenges for TriPeaks Solitaire, which means to move all 28 tableau cards into the waste pile.
 * It will either return a single shortest solution, or no solution at all if it's impossible to clear.
 */
public class BoardChallengeSolver implements TriPeaksSolver {
    /**
     * Return at most one optimal solution for finishing a Board Challenge in TriPeaks.
     * If there is no solution, an empty map is returned.  Otherwise it will contain one list of steps in
     * terms of cards to move to the waste pile, where the key is a description of the solution.
     *
     * @param deck the deck of cards to solve
     * @return zero or one solutions in a map: key = description, value = list of cards to move to the waste pile
     */
    @Override
    public Map<String, List<String>> solve(Deck deck) {
        Map<String, List<String>> solutions = new HashMap<>();
        IntFIFOQueue fringe = new IntFIFOQueue();
        TIntIntMap seenStates = new TIntIntHashMap();
        fringe.enqueue(State.INITIAL_STATE);
        while (!fringe.isEmpty()) {
            int state = fringe.dequeue();
            if (State.isTableauEmpty(state)) {
                List<String> cards = TriPeaksSolver.cards(state, seenStates, deck);
                solutions.put(String.format("Clear the board in %d steps", cards.size()), cards);
                break;
            }
            for (int nextState : State.successors(state, deck)) {
                if (!seenStates.containsKey(nextState)) {
                    seenStates.put(nextState, state);
                    fringe.enqueue(nextState);
                }
            }
        }
        return solutions;
    }
}
