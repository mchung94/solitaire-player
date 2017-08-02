package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

class StateCache {
    private boolean isPyramidClear;
    private int heuristicCost;
    long[] unwinnableMasks;
    long[][][] successorMaskTable;

    StateCache(boolean isPyramidClear, int heuristicCost, long[] unwinnableMasks, long[][][] successorMaskTable) {
        this.isPyramidClear = isPyramidClear;
        this.heuristicCost = heuristicCost;
        this.unwinnableMasks = unwinnableMasks;
        this.successorMaskTable = successorMaskTable;
    }

    /**
     * Return true if all the 28 pyramid cards have been removed in this pyramid state.  This means
     * the game has ended successfully (the board is cleared) and no more moves can be made.
     * Depending on the player's goal, it may or may not mean they have "won", for example if
     * they are trying to maximize the score or removing cards of a certain rank.
     *
     * @return true if all the 28 pyramid cards have been removed
     */
    boolean isPyramidClear() {
        return isPyramidClear;
    }

    /**
     * For the A* algorithm, return an estimate of the cost to reach the goal.
     * This is an admissible and consistent heuristic.
     * This is only used for Board Challenges where the player needs to clear the board,
     * so it's an estimate of how many more steps to clear the 28 pyramid cards.
     *
     * @return an estimate of the cost to reach the goal
     */
    int getHeuristicCost() {
        return heuristicCost;
    }

    /**
     * Return true if the state of the game is unwinnable.
     * This is only used for Board Challenges where the player needs to clear the board,
     * so it's checking if there exists a card on the table that can't be removed, because
     * there is no matching card to remove it with that isn't covering or covered by the card.
     * <p>
     * When this method returns true, the board definitely can't be cleared.  But returning false
     * isn't a guarantee that the board can be cleared, because it just doesn't do a complete check
     * for performance reasons.
     *
     * @param state a long value for the Pyramid Solitaire state
     * @return true if the board can't be cleared
     */
    boolean isUnwinnable(long state) {
        for (long mask : unwinnableMasks) {
            if ((state & mask) == 0L) {
                return true;
            }
        }
        return false;
    }

    /**
     * For a given state, return a list of the states resulting from applying all applicable actions
     * to the state.
     *
     * @param state a long value for the Pyramid Solitaire state
     * @return a list of the successor states (longs)

     */
    TLongList getSuccessors(long state) {
        TLongList successors = new TLongArrayList();
        long deckFlags = State.getDeckFlags(state);
        int stockIndex = State.getStockIndex(state);
        int cycle = State.getCycle(state);
        int wasteIndex = State.getWasteIndex(deckFlags, stockIndex);
        long[] successorMasks = this.successorMaskTable[stockIndex][wasteIndex];
        if (State.isStockEmpty(stockIndex)) {
            if (cycle != 3) {
                successors.add(State.createState(deckFlags, 28, cycle + 1));
            }
        } else {
            successors.add(State.createState(deckFlags, stockIndex + 1, cycle));
        }
        for (long mask : successorMasks) {
            successors.add(State.createState(deckFlags & mask, stockIndex, cycle));
        }
        return successors;
    }
}
