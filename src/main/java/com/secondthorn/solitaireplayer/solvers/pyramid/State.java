package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

/**
 * States represent the "state of the world" of each step while playing Pyramid Solitaire.
 * For performance purposes, States are just longs, and this class implements static methods for
 * handling them.
 * <p>
 * The state is represented using 60 bits (the last four bits of the long are ignored):
 * <ul>
 *     <li>Bits 0-51 represent the existence of each card in the deck</li>
 *     <ul>
 *         <li>Bits 0-27 are the 28 table cards</li>
 *         <li>Bits 28-51 are the 24 deck/waste cards, 28 is the initial top of the deck</li>
 *     </ul>
 *     <li>Bits 52-57 is a 6-bit number from 28-52 to indicate the deck index</li>
 *     <ul>
 *         <li>The card at the deck index is the top of the deck</li>
 *         <li>Cards above the deck index are the rest of the deck cards</li>
 *         <li>Cards below the deck index are the waste pile, the closest one to the deck index is the top</li>
 *         <li>Deck index 52 means the deck is empty, waste index 27 means the waste pile is empty</li>
 *     </ul>
 *     <li>Bits 58-60 is a 2-bit number from 1-3 indicating which cycle through the deck the player is on</li>
 * </ul>
 */
public class State {
    /**
     * The initial state when playing Pyramid Solitaire: no cards removed yet, deck index is at the 28th card
     * and the cycle is 1.
     */
    static final long INITIAL_STATE = createState(0xFFFFFFFFFFFFFL, 28, 1);

    /**
     * Bit masks for checking if a table card is uncovered.
     * The set bits are table index itself, plus its two children unless it doesn't have any,
     * like the bottom row of the table.
     */
    static final long[] TABLE_UNCOVERED_MASKS = {
            0b0000000000000000000000000111L,
            0b0000000000000000000000011010L,
            0b0000000000000000000000110100L,
            0b0000000000000000000011001000L,
            0b0000000000000000000110010000L,
            0b0000000000000000001100100000L,
            0b0000000000000000110001000000L,
            0b0000000000000001100010000000L,
            0b0000000000000011000100000000L,
            0b0000000000000110001000000000L,
            0b0000000000011000010000000000L,
            0b0000000000110000100000000000L,
            0b0000000001100001000000000000L,
            0b0000000011000010000000000000L,
            0b0000000110000100000000000000L,
            0b0000011000001000000000000000L,
            0b0000110000010000000000000000L,
            0b0001100000100000000000000000L,
            0b0011000001000000000000000000L,
            0b0110000010000000000000000000L,
            0b1100000100000000000000000000L,
            0b0000001000000000000000000000L,
            0b0000010000000000000000000000L,
            0b0000100000000000000000000000L,
            0b0001000000000000000000000000L,
            0b0010000000000000000000000000L,
            0b0100000000000000000000000000L,
            0b1000000000000000000000000000L,
    };

    /**
     * The deck index is an integer from 28 to 52.  52 means the deck is empty.
     */
    private static final int EMPTY_DECK = 52;

    /**
     * The waste index is an integer from 27 to 51.  27 means the waste pile is empty.
     */
    private static final int EMPTY_WASTE = 27;

    /**
     * Create a new state (a long value) given the three values to be packed into the state.
     * @param existFlags 52 bits showing which cards in the deck haven't been removed yet
     * @param deckIndex an integer from 28-52 indicating the top of the deck (or empty if 52)
     * @param cycle an integer from 1-3 indicating which cycle through the deck cards we're on
     * @return a new long representing the state
     */
    static long createState(long existFlags, int deckIndex, int cycle) {
        while ((deckIndex < 52) && (((1L << deckIndex) & existFlags) == 0)) {
            deckIndex++;
        }
        return existFlags | ((long) deckIndex << 52) | ((long) cycle << 58);
    }

    /**
     * Given a state, return the 52 bits showing which cards in the deck haven't been removed yet.
     * @param state a long value for the Pyramid Solitaire state
     * @return the 52 bit existence flag values showing which cards in the deck haven't been removed
     */
    static long getExistFlags(long state) {
        return 0xFFFFFFFFFFFFFL & state;
    }

    /**
     * Given a state, return the deck index.
     * @param state a long value for the Pyramid Solitaire state
     * @return the deck index integer from 28-52, 52 means the deck is empty
     */
    static int getDeckIndex(long state) {
        return 0b111111 & (int) (state >>> 52);
    }

    /**
     * Given a state, return the cycle.
     * @param state a long value for the Pyramid Solitaire state
     * @return the cycle integer from 1-3
     */
    static int getCycle(long state) {
        return 0b11 & (int) (state >>> 58);
    }

    /**
     * Given 52 bit exist flags and the deck index, derive the waste index which points to the
     * first card in the waste pile.  27 means the waste pile is empty.
     * @param existFlags the 52 bit existence flag values for a state
     * @param deckIndex the deck index for a state
     * @return the waste index for the state
     */
    static int getWasteIndex(long existFlags, int deckIndex) {
        int wasteIndex = deckIndex - 1;
        while (wasteIndex > 27) {
            if (((1L << wasteIndex) & existFlags) != 0) {
                return wasteIndex;
            }
            wasteIndex--;
        }
        return wasteIndex;
    }

    /**
     * Return true if all the 28 table cards have been removed in this state.  This means
     * the game has ended successfully (the board is cleared) and no more moves can be made.
     * Depending on the player's goal, it may or may not mean they have "won", for example if
     * they are trying to maximize the score or removing cards of a certain rank.
     * @param state
     * @return
     */
    static boolean isTableClear(long state) {
        return (0xFFFFFFFL & state) == 0L;
    }

    /**
     * For the A* algorithm, return an estimate of the cost to reach the goal.
     * This is an admissible and consistent heuristic.
     * This is only used for Board Challenges where the player needs to clear the board,
     * so it's an estimate of how many more steps to clear the 28 table cards.
     * @param state a long value for the Pyramid Solitaire state
     * @param deck the Deck of cards being played
     * @return the estimated number of steps from the current state to clearing the board
     */
    static int hCost(long state, Deck deck) {
        int[] buckets = new int[13];
        for (int i = 0; i < 28; i++) {
            if (((1L << i) & state) != 0) {
                buckets[deck.cardRankBucket(i)]++;
            }
        }
        int hCost = buckets[0];
        for (int i = 1; i < 7; i++) {
            hCost += Math.max(buckets[i], buckets[13 - i]);
        }
        return hCost;
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
     * @param state a long value for the Pyramid Solitaire state
     * @param deck the Deck of cards being played
     * @return true if the board can't be cleared
     */
    static boolean isUnwinnable(long state, Deck deck) {
        for (int i = 0; i < 28; i++) {
            long cardExistsBit = 1L << i;
            long unwinnableMask = deck.getUnwinnableMask(i);
            if (cardExistsBit == (state & unwinnableMask)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For a given state, return a list of the states resulting from applying all applicable actions
     * to the state.
     * @param state a long value for the Pyramid Solitaire state
     * @param deck the Deck of cards being played
     * @return a list of the successor states (longs)
     */
    static TLongList successors(long state, Deck deck) {
        long existFlags = getExistFlags(state);
        int deckIndex = getDeckIndex(state);
        int wasteIndex = getWasteIndex(existFlags, deckIndex);
        int cycle = getCycle(state);
        TIntList uncoveredIndexes = uncoveredTableIndexes(state);
        TLongList successors = new TLongArrayList();

        if (deckIndex == EMPTY_DECK) {
            if (cycle != 3) {
                successors.add(createState(existFlags, 28, 1 + cycle));
            }
        } else {
            uncoveredIndexes.add(deckIndex);
            successors.add(createState(existFlags, 1 + deckIndex, cycle));
        }

        if (wasteIndex != EMPTY_WASTE) {
            uncoveredIndexes.add(wasteIndex);
        }

        for (int i = 0, len = uncoveredIndexes.size(); i < len; i++) {
            int index1 = uncoveredIndexes.get(i);
            boolean[] matches = deck.getMatchesForCard(index1);
            if (deck.isKing(index1)) {
                long mask = ~(1L << index1);
                successors.add(createState(mask & existFlags, deckIndex, cycle));
            } else {
                long bit1 = 1L << index1;
                for (int j = i + 1; j < len; j++) {
                    int index2 = uncoveredIndexes.get(j);
                    if (matches[index2]) {
                        long bit2 = 1L << index2;
                        long mask = ~(bit1 | bit2);
                        successors.add(createState(mask & existFlags, deckIndex, cycle));
                    }
                }
            }
        }

        return successors;
    }

    /**
     * Given a state, return a list of table indexes for cards that aren't covered by other cards
     * below it.  Covered cards are blocked, and can't be removed until they're uncovered first.
     * @param state a long value for the Pyramid Solitaire state
     * @return a list of table indexes for the uncovered table cards
     */
    static TIntList uncoveredTableIndexes(long state) {
        TIntList uncoveredIndexes = new TIntArrayList();

        for (int i = 0; i < 28; i++) {
            long cardExistsBit = 1L << i;
            long mask = TABLE_UNCOVERED_MASKS[i];
            if (cardExistsBit == (mask & state)) {
                uncoveredIndexes.add(i);
            }
        }

        return uncoveredIndexes;
    }

}
