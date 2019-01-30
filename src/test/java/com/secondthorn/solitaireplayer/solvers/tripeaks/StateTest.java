package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateTest {
    /**
     * Returns all indexes of tableau cards blocking each card from below.
     *       00          01          02
     *     03  04      05  06      07  08
     *   09  10  11  12  13  14  15  16  17
     * 18  19  20  21  22  23  24  25  26  27
     */
    private int[] tableauChildIndexes(int index) {
        final int[][] childIndexes = {
                {3, 4, 9, 10, 11, 18, 19, 20, 21},
                {5, 6, 12, 13, 14, 21, 22, 23, 24},
                {7, 8, 15, 16, 17, 24, 25, 26, 27},
                {9, 10, 18, 19, 20},
                {10, 11, 19, 20, 21},
                {12, 13, 21, 22, 23},
                {13, 14, 22, 23, 24},
                {15, 16, 24, 25, 26},
                {16, 17, 25, 26, 27},
                {18, 19},
                {19, 20},
                {20, 21},
                {21, 22},
                {22, 23},
                {23, 24},
                {24, 25},
                {25, 26},
                {26, 27},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
        };
        return childIndexes[index];
    }

    private boolean isBitSet(int bits, int index) {
        return ((1 << index) & bits) != 0;
    }

    private boolean isValidTableauFlags(int tableauFlags) {
        for (int i=0; i<28; i++) {
            if (!isBitSet(tableauFlags, i) &&
                    Arrays.stream(tableauChildIndexes(i))
                            .anyMatch(index -> isBitSet(tableauFlags, index))) {
                return false;
            }
        }
        return true;
    }

    @Test
    void allTableauFlagsAreValid() {
        assertEquals(22932, State.TABLEAU_FLAGS.length);
        for (int tableauFlags : State.TABLEAU_FLAGS) {
            assertTrue(isValidTableauFlags(tableauFlags));
        }
    }

    @Test
    void initialStateIsValid() {
        assertEquals(0xFFFFFFF, State.TABLEAU_FLAGS[State.getTableauIndex(State.INITIAL_STATE)]);
        assertEquals(28, State.getWasteIndex(State.INITIAL_STATE));
        assertEquals(29, State.getStockIndex(State.INITIAL_STATE));
    }

    @Test
    void stockIndex() {
        for (int stockIndex=29; stockIndex<52; stockIndex++) {
            assertFalse(State.isStockEmpty(stockIndex));
        }
        assertTrue(State.isStockEmpty(52));
    }

    @Test
    void tableauEmpty() {
        assertTrue(State.isTableauEmpty(State.create(0, 28, 29)));
        assertFalse(State.isTableauEmpty(State.INITIAL_STATE));
    }

    @Test
    void tableauFlagsToId() {
        assertEquals(State.TABLEAU_FLAGS.length, State.TABLEAU_FLAGS_TO_ID.size());
        for (int i=0; i<State.TABLEAU_FLAGS.length; i++) {
            assertEquals(i, State.TABLEAU_FLAGS_TO_ID.get(State.TABLEAU_FLAGS[i]));
        }
    }

    private boolean isValidFaceUpIndexes(int tableauFlags, int[] faceUpIndexes) {
        TIntList expected = new TIntArrayList();
        for (int i=0; i<28; i++) {
            if (isBitSet(tableauFlags, i) &&
                    Arrays.stream(tableauChildIndexes(i))
                            .noneMatch(index -> isBitSet(tableauFlags, index))) {
                expected.add(i);
            }
        }
        return Arrays.equals(expected.toArray(), faceUpIndexes);
    }

    @Test
    void allFaceUpIndexesAreValid() {
        assertEquals(State.TABLEAU_FLAGS.length, State.TABLEAU_FACE_UP_INDEXES.length);
        for (int i=0; i<State.TABLEAU_FLAGS.length; i++) {
            int tableauFlags = State.TABLEAU_FLAGS[i];
            int[] faceUpIndexes = State.TABLEAU_FACE_UP_INDEXES[i];
            assertTrue(isValidFaceUpIndexes(tableauFlags, faceUpIndexes));
        }
    }
}
