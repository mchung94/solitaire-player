package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.List;

public class CardChallengeSolver implements TriPeaksSolver {
    /**
     * The card rank that counts towards the goal.
     */
    char cardRankToClear;

    /**
     * The number of cards of the goal rank to remove from the tableau to reach the goal.
     */
    int numCardsToClear;

    /**
     * Creates a Card Challenge solver that tries to reach the goal.
     * @param goalNumCardsToClear the number of cards of the goal rank to remove from the tableau
     * @param cardRankToClear the goal rank
     * @param currentNumCardsCleared the number of cards of the goal rank removed in previous games
     */
    public CardChallengeSolver(int goalNumCardsToClear, char cardRankToClear, int currentNumCardsCleared) {
        numCardsToClear = goalNumCardsToClear - currentNumCardsCleared;
        if (numCardsToClear <= 0) {
            throw new IllegalArgumentException("The current number of cards cleared must be smaller than the goal");
        }
        this.cardRankToClear = cardRankToClear;
    }

    /**
     * Returns a solution for the best possible steps to play.  Some cards may be unknown.
     * The solution will be one of the following in priority order:
     * <ol>
     *     <li>A Solution that can reach the goal.</li>
     *     <li>A Solution that removes all tableau cards.</li>
     *     <li>A Solution that removes as many goal cards from the tableau as possible.</li>
     * </ol>
     * <p>
     * Only cards of the goal rank in the tableau can count towards the goal - cards of the goal rank that are in the
     * stock pile can't be counted towards the goal.
     * <p>
     * If the Solution can reach the goal or clear the tableau, then it will be a definitive solution.  Otherwise, the
     * Solution is only definitive if all cards are known.  If a solution is not definitive, then it may be possible to
     * find a better solution by revealing more unknown cards.
     *
     * @param deck          a deck of cards for TriPeaks Solitaire
     * @param startingState the starting state for the solver to begin at (might not be the beginning of the game)
     * @return
     */
    @Override
    public Solution solve(Deck deck, int startingState) {
        int tableauRankMask = deck.tableauRankMask(cardRankToClear);
        int numGoalCardsOnTableau = numBitsSet(tableauRankMask);
        IntFIFOQueue fringe = new IntFIFOQueue();
        TIntIntMap seenStates = new TIntIntHashMap();
        fringe.enqueue(startingState);

        int bestNonClearState = -1;
        int bestNonClearStateScore = -1;

        while (!fringe.isEmpty()) {
            int state = fringe.dequeue();
            int numGoalCardsRemoved = numGoalCardsRemoved(state, tableauRankMask, numGoalCardsOnTableau);
            if (State.isTableauEmpty(state) || (numGoalCardsRemoved == numCardsToClear)) {
                return buildSolution(true, state, seenStates, deck, numGoalCardsRemoved);
            }
            int[] successors = State.successors(state, deck);
            if (successors.length == 0) {
                if (numGoalCardsRemoved > bestNonClearStateScore) {
                    bestNonClearState = state;
                    bestNonClearStateScore = numGoalCardsRemoved;
                }
            }
            for (int nextState : successors) {
                if (!seenStates.containsKey(nextState)) {
                    seenStates.put(nextState, state);
                    fringe.enqueue(nextState);
                }
            }
        }
        return buildSolution(!deck.hasUnknownCards(), bestNonClearState, seenStates, deck, bestNonClearStateScore);
    }

    /**
     * Returns a human-readable Solution description string.
     */
    private String removeMessage(int numCardsToRemove, char rankToRemove, int numSteps) {
        String cards = numCardsToRemove == 1 ? "card" : "cards";
        String steps = numSteps == 1 ? "step" : "steps";
        return String.format("Remove %d %s of rank %c in %d %s",
                numCardsToRemove, cards, rankToRemove, numSteps, steps);
    }

    /**
     * Creates a new Solution instance.
     */
    Solution buildSolution(boolean definitiveSolution, int state, TIntIntMap seenStates, Deck deck, int numRemoved) {
        List<Action> actions = TriPeaksSolver.actions(state, seenStates, deck);
        String description = removeMessage(numRemoved, cardRankToClear, actions.size());
        return new Solution(description, definitiveSolution, actions, state);
    }

    /**
     * Returns the number of cards of the goal rank removed from the tableau.
     * Cards of the goal rank in the stock pile never count towards the goal.
     */
    private int numGoalCardsRemoved(int state, int tableauRankMask, int numGoalCardsOnTableau) {
        int tableauIndex = State.getTableauIndex(state);
        int tableauFlags = State.TABLEAU_FLAGS[tableauIndex];
        int numRemainingCards = numBitsSet(tableauFlags & tableauRankMask);
        return numGoalCardsOnTableau - numRemainingCards;
    }

    /**
     * Returns the number of bits set in the integer n.
     */
    private int numBitsSet(int n) {
        int count = 0;
        while (n != 0) {
            n &= n - 1;
            count++;
        }
        return count;
    }
}
