package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Finds a way to turn over face down cards on the tableau so they become known.
 */
public class CardRevealingSolver implements TriPeaksSolver {
    /**
     * Returns an optimal solution for turning over at least one face down card on the tableau if possible.
     *
     * @param deck          a deck of cards for TriPeaks Solitaire
     * @param startingState the starting state for the solver to begin at (might not be the beginning of the game)
     * @return a list containing one Solution to either reveal a card or do nothing
     */
    @Override
    public List<Solution> solve(Deck deck, int startingState) {
        List<Solution> solutions = new ArrayList<>();
        if (deck.hasUnknownCards()) {
            IntFIFOQueue fringe = new IntFIFOQueue();
            TIntIntMap seenStates = new TIntIntHashMap();
            fringe.enqueue(startingState);
            while (!fringe.isEmpty()) {
                int state = fringe.dequeue();
                int[] faceUpUnknowns = State.faceUpUnknowns(state, deck);
                int[] nextStates = State.successors(state, deck);
                if (faceUpUnknowns.length > 0) {
                    String description = String.format("Reveal the tableau cards at index(es): %s",
                            Arrays.toString(faceUpUnknowns));
                    List<Action> cards = TriPeaksSolver.actions(state, seenStates, deck);
                    solutions.add(new Solution(description, true, cards, state));
                    break;
                }
                for (int nextState : nextStates) {
                    if (!seenStates.containsKey(nextState)) {
                        seenStates.put(nextState, state);
                        fringe.enqueue(nextState);
                    }
                }
            }
        } else {
            String description = "All cards are known, can't turn over any face down cards";
            solutions.add(new Solution(description, true, new ArrayList<>(), startingState));
        }

        if ((solutions.size() == 0) && (startingState != State.INITIAL_STATE)) {
            List<Solution> solutionsFromBeginning = solve(deck, State.INITIAL_STATE);
            Solution solution = solutionsFromBeginning.get(0);
            solution.getActions().add(0, new Action("Undo Board", deck));
            solutions.add(solution);
        }

        return solutions;
    }
}
