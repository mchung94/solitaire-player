package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

/**
 * Solves Board Challenges for TriPeaks Solitaire, which means to move all 28 tableau cards into the waste pile.
 * It will either return a single shortest solution, or no solution at all if it's impossible to clear.
 */
public class BoardChallengeSolver implements TriPeaksSolver {
    /**
     * Return either a solution for finishing a Board Challenge in TriPeaks, a solution indicating it's impossible,
     * or a solution indicating there are unknown cards so it's unknown whether or not it's possible.
     *
     * @param deck          the deck of cards to solve
     * @param startingState the starting state of the game for the solver to begin searching from
     * @return a list containing one Solution
     */
    @Override
    public Solution solve(Deck deck, int startingState) {
        IntFIFOQueue fringe = new IntFIFOQueue();
        TIntIntMap seenStates = new TIntIntHashMap();
        fringe.enqueue(startingState);
        while (!fringe.isEmpty()) {
            int state = fringe.dequeue();
            if (State.isTableauEmpty(state)) {
                List<Action> actions = TriPeaksSolver.actions(state, seenStates, deck);
                String description = String.format("Clear the board in %d steps", actions.size());
                return new Solution(description, true, actions, state, seenStates.get(state));
            }
            for (int nextState : State.successors(state, deck)) {
                if (!seenStates.containsKey(nextState)) {
                    seenStates.put(nextState, state);
                    fringe.enqueue(nextState);
                }
            }
        }
        String description = "Lose Quickly: Impossible to clear the board";
        List<Action> actions = TriPeaksSolver.loseQuicklyActions(deck);
        return new Solution(description, !deck.hasUnknownCards(), actions, startingState, seenStates.get(startingState));
    }
}
