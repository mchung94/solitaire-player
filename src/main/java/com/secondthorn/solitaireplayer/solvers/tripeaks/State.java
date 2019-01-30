package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * States represent the state of the world of each step while playing TriPeaks solitaire.
 * For performance purposes, states are just ints, and this class contains static methods for handling them.
 * <p>
 * States are represented using 26 bits:
 * <ul>
 * <li>Bits  0-14: 15 bits to store an ID indicating which cards remain on the tableau.</li>
 * <li>Bits 15-20: 6 bits for the deck index of the top card in the waste pile.</li>
 * <li>Bits 21-26: 6 bits for the deck index of the top card in the stock pile.</li>
 * </ul>
 * There are only 22932 possible combinations of cards remaining on the tableau because a card can only be removed when
 * there are no cards blocking it from below.  Also, the above 3 fields are sufficient to describe the current state
 * of the game when paired with a Deck of cards, but calculating the score requires knowing the steps taken from the
 * beginning of the game.
 */
public class State {
    /**
     * All 22932 possible values for tableau flags. Tableau flags are 28-bit values indicating which of the 28 cards
     * in the tableau remain in the game. The index into this array represents a tableau index or unique ID for each
     * possible value, and it's the index which is encoded in the state values.
     */
    public static int[] TABLEAU_FLAGS = allTableauFlags();

    /**
     * A mapping from tableau flags values to their index into TABLEAU_FLAGS, also known as its ID.
     */
    public static TIntIntMap TABLEAU_FLAGS_TO_ID = tableauFlagsToIdMap(TABLEAU_FLAGS);

    /**
     * For each tableau flags value, an array of the deck indexes of the face up cards on the tableau.
     */
    public static int[][] TABLEAU_FACE_UP_INDEXES = allTableauFaceUpIndexes(TABLEAU_FLAGS);

    /**
     * The initial state for any TriPeaks Solitaire game - no cards removed yet from the tableau, one card in the
     * waste pile.
     */
    public static int INITIAL_STATE = create(22931, 28, 29);

    /**
     * Creates a new state (an int value) from the given parameters.
     *
     * @param tableauIndex the tableau ID representing the cards remaining on the tableau
     * @param wasteIndex   the deck index of the top card of the waste pile (0 - 51)
     * @param stockIndex   the deck index of the top card of the stock pile, or 52 if it's empty (29 - 52)
     * @return
     */
    public static int create(int tableauIndex, int wasteIndex, int stockIndex) {
        return tableauIndex | (wasteIndex << 15) | (stockIndex << 21);
    }

    /**
     * Returns the tableau index for the given state. This can be used as an index into the TABLEAU_FLAGS and
     * TABLEAU_FACE_UP_INDEXES arrays.
     *
     * @param state a TriPeaks Solitaire state
     * @return the tableau index encoded in the state
     */
    public static int getTableauIndex(int state) {
        return state & 0x7FFF;
    }

    /**
     * Returns the deck index for the top card of the waste pile.
     *
     * @param state a TriPeaks Solitaire state
     * @return the index of the card on top of the waste pile
     */
    public static int getWasteIndex(int state) {
        return (state >> 15) & 0b111111;
    }

    /**
     * Returns the deck index for the top card of the stock pile.
     *
     * @param state a TriPeaks Solitaire state
     * @return the index of the card on top of the stock pile
     */
    public static int getStockIndex(int state) {
        return (state >> 21) & 0b111111;
    }

    /**
     * Returns true if there are no more cards on the board/tableau.
     *
     * @param state a TriPeaks Solitaire state
     * @return true if all 28 cards in the tableau have all moved to the waste pile
     */
    public static boolean isTableauEmpty(int state) {
        return getTableauIndex(state) == 0;
    }

    /**
     * Returns true if the stock pile is empty
     *
     * @param stockIndex the deck index for the card on top of the stock pile
     * @return true if the stock pile is empty
     */
    public static boolean isStockEmpty(int stockIndex) {
        return stockIndex == 52;
    }

    /**
     * Return an array of successor states for the given state. These represent all the valid moves the player can
     * make from the given state.
     *
     * @param state a TriPeaks Solitaire state
     * @param deck  a deck of cards, some of which may be unknown
     * @return an array of states resulting from each valid move the player can make
     */
    public static int[] successors(int state, Deck deck) {
        int tableauIndex = getTableauIndex(state);
        int tableauFlags = TABLEAU_FLAGS[tableauIndex];
        int[] faceUpIndexes = TABLEAU_FACE_UP_INDEXES[tableauIndex];
        int wasteIndex = getWasteIndex(state);
        int stockIndex = getStockIndex(state);
        TIntList nextStates = new TIntArrayList();
        if (!isTableauEmpty(state)) {
            if (!isStockEmpty(stockIndex)) {
                nextStates.add(create(tableauIndex, stockIndex, stockIndex + 1));
            }
            for (int faceUpIndex : faceUpIndexes) {
                if (deck.isOneAboveOrBelow(wasteIndex, faceUpIndex)) {
                    int newTableauFlags = tableauFlags ^ (1 << faceUpIndex);
                    nextStates.add(create(TABLEAU_FLAGS_TO_ID.get(newTableauFlags), faceUpIndex, stockIndex));
                }
            }
        }
        return nextStates.toArray();
    }

    /**
     * Calculate all 22932 valid values for tableau flags, the 28-bit values showing which cards are remaining on the
     * board/tableau.
     */
    private static int[] allTableauFlags() {
        TIntList allFlags = new TIntArrayList();
        // basically, for all possible values for the bottom row of the tableau (10 cards), generate all possible
        // values for each row above it
        for (int row4 = 0; row4 < 1024; row4++) {
            for (int row3 : previousRows(row4, 4)) {
                for (int row2 : previousRows(row3, 3)) {
                    for (int row1 : previousRows(row2, 2)) {
                        allFlags.add(row1 | (row2 << 3) | (row3 << 9) | (row4 << 18));
                    }
                }
            }
        }
        return allFlags.toArray();
    }

    /**
     * Given existence bits for a row of cards on the tableau, returns a list of offsets on the row above it that don't
     * have to have a card. For example, if the bottom row of the tableau (10 cards) looks like this: 1111111100, it
     * means the third row of the tableau must be fully populated, except the last card.
     */
    private static TIntList previousRowOptionalIndexes(int rowFlags, int rowNumber) {
        int[] rowOffsets = rowOffsets(rowNumber);
        TIntList optionalIndexes = new TIntArrayList(rowOffsets.length);
        for (int i = 0; i < rowOffsets.length; i++) {
            if (((0b11 << rowOffsets[i]) & rowFlags) == 0) {
                optionalIndexes.add(i);
            }
        }
        return optionalIndexes;
    }

    /**
     * A helper for previousRowOptionalIndexes. For a given row of the tableau, returns the offsets to check if the
     * card on the previous row can be empty. For example, row 2 of the tableau contains six cards. If you check that
     * cards (0 and 1), (2 and 3), and (4 and 5) on that row are empty, you can tell if the three cards on row 1 of the
     * tableau must be in the tableau - only if the two blocking cards are gone can the card on row 1 be removed.
     */
    private static int[] rowOffsets(int rowNumber) {
        final int[][] offsets = {
                {0, 2, 4},
                {0, 1, 3, 4, 6, 7},
                {0, 1, 2, 3, 4, 5, 6, 7, 8},
        };
        return offsets[rowNumber - 2];
    }

    /**
     * Given existence bits for cards on a row of the tableau, return all possible existence bit values for the row
     * above it.
     */
    private static int[] previousRows(int rowFlags, int rowNumber) {
        final int[] allCardsMasks = {0b111, 0b111111, 0b111111111};
        TIntList rows = new TIntArrayList();
        TIntList optIndexes = previousRowOptionalIndexes(rowFlags, rowNumber);
        int allCardsMask = allCardsMasks[rowNumber - 2];
        for (int bits = 0; bits < (1 << optIndexes.size()); bits++) {
            int mask = 0;
            for (int i = 0; i < optIndexes.size(); i++) {
                if (((1 << i) & bits) == 0) {
                    mask |= (1 << optIndexes.get(i));
                }
            }
            rows.add(allCardsMask ^ mask);
        }
        return rows.toArray();
    }

    /**
     * Creates a mapping from tableau flags to tableau index, the array index to TABLEAU_FLAGS for that value.
     */
    private static TIntIntMap tableauFlagsToIdMap(int[] allTableauFlags) {
        TIntIntMap map = new TIntIntHashMap();
        for (int i = 0; i < allTableauFlags.length; i++) {
            map.put(allTableauFlags[i], i);
        }
        return map;
    }

    /**
     * Creates a list of deck indexes for face up cards on the given tableau flags value.
     */
    private static TIntList tableauFaceUpIndexes(int tableauFlags) {
        // bits set on the Ith card and the cards blocking it
        final int[] masks = {
                0b0000001111000000111000011001,
                0b0001111000000111000001100010,
                0b1111000000111000000110000100,
                0b0000000111000000011000001000,
                0b0000001110000000110000010000,
                0b0000111000000011000000100000,
                0b0001110000000110000001000000,
                0b0111000000011000000010000000,
                0b1110000000110000000100000000,
                0b0000000011000000001000000000,
                0b0000000110000000010000000000,
                0b0000001100000000100000000000,
                0b0000011000000001000000000000,
                0b0000110000000010000000000000,
                0b0001100000000100000000000000,
                0b0011000000001000000000000000,
                0b0110000000010000000000000000,
                0b1100000000100000000000000000,
                0b0000000001000000000000000000,
                0b0000000010000000000000000000,
                0b0000000100000000000000000000,
                0b0000001000000000000000000000,
                0b0000010000000000000000000000,
                0b0000100000000000000000000000,
                0b0001000000000000000000000000,
                0b0010000000000000000000000000,
                0b0100000000000000000000000000,
                0b1000000000000000000000000000
        };
        TIntList indexes = new TIntArrayList();
        for (int i = 0; i < 28; i++) {
            int mask = masks[i];
            if ((1 << i) == (tableauFlags & mask)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    /**
     * Creates a list of indexes of face up cards for every possible tableau flags value.
     */
    private static int[][] allTableauFaceUpIndexes(int[] allTableauFlags) {
        int[][] allIndexes = new int[allTableauFlags.length][];
        for (int i = 0; i < allTableauFlags.length; i++) {
            allIndexes[i] = tableauFaceUpIndexes(allTableauFlags[i]).toArray();
        }
        return allIndexes;
    }
}
