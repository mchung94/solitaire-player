package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PyramidTest {
    /**
     * Pyramid indexes arranged in the order they are in Pyramid.flags.
     */
    private static final int[][] PYRAMID_INDEXES = {
            {0, 2, 5, 9, 14, 20, 27},
            {1, 4, 8, 13, 19, 26},
            {3, 7, 12, 18, 25},
            {6, 11, 17, 24},
            {10, 16, 23},
            {15, 22},
            {21}
    };

    /**
     * Return true if the card at index exists according to the flags (i.e., the nth bit is set)
     *
     * @param index a deck index (0-51)
     * @param flags a bit flags value
     * @return true if the card at the index exists in the deck (bit is set in the flags)
     */
    private boolean cardExists(int index, long flags) {
        return ((1L << index) & flags) != 0L;
    }

    @Test
    public void coverMasks() {
        for (int pyramidIndex = 0; pyramidIndex < 28; pyramidIndex++) {
            PyramidLocation location = new PyramidLocation(pyramidIndex);
            long mask = Pyramid.COVER_MASKS[pyramidIndex];
            for (int i = 0; i < 28; i++) {
                assertEquals(cardExists(i, mask), new PyramidLocation(i).isCovering(location));
            }
        }
    }

    @Test
    public void unrelatedCardMasks() {
        for (int pyramidIndex = 0; pyramidIndex < 28; pyramidIndex++) {
            PyramidLocation location = new PyramidLocation(pyramidIndex);
            long mask = Pyramid.UNRELATED_CARD_MASKS[pyramidIndex];
            for (int i = 0; i < 27; i++) {
                PyramidLocation testLocation = new PyramidLocation(i);
                boolean isUnrelated = !(testLocation.isCovering(location) ||
                        location.isCovering(testLocation) ||
                        testLocation.pyramidIndex == location.pyramidIndex);
                assertEquals(cardExists(i, mask), isUnrelated);
            }
            for (int stockIndex = 28; stockIndex < 52; stockIndex++) {
                assertTrue(cardExists(stockIndex, mask));
            }
        }
    }

    /**
     * Return true if all the descendants of pyramidIndex (cards covering pyramidIndex) are removed from pyramidFlags.
     *
     * @param pyramidIndex a pyramid index (0-27)
     * @param pyramidFlags a pyramid flags (28 bit flags)
     * @return true if pyramidIndex is uncovered
     */
    private boolean allDescendantsAreRemoved(int pyramidIndex, long pyramidFlags) {
        PyramidLocation location = new PyramidLocation(pyramidIndex);
        for (int i = 0; i < 28; i++) {
            PyramidLocation otherLocation = new PyramidLocation(i);
            if (otherLocation.isCovering(location) && cardExists(i, pyramidFlags)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if pyramidFlags is a valid value for the arrangement of cards in the pyramid.
     * The rule is: a card in the pyramid can't be removed unless all its descendants (cards covering it from below)
     * have been removed first.
     *
     * @param pyramidFlags a pyramid flags (28 bit flags)
     * @return true if pyramidFlags is a valid value
     */
    private boolean isValidPyramidFlags(long pyramidFlags) {
        for (int i = 0; i < 28; i++) {
            if (!cardExists(i, pyramidFlags) && !(allDescendantsAreRemoved(i, pyramidFlags))) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testAllFlags() {
        assertEquals(1430, Pyramid.ALL.length);
        for (Pyramid pyramid : Pyramid.ALL) {
            assertTrue(isValidPyramidFlags(pyramid.getFlags()));
        }
    }

    private boolean isInArray(int number, int[] array) {
        for (int element : array) {
            if (number == element) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testUncoveredIndexes() {
        for (Pyramid pyramid : Pyramid.ALL) {
            long pyramidFlags = pyramid.getFlags();
            for (int i = 0; i < 28; i++) {
                boolean expected = cardExists(i, pyramidFlags) && allDescendantsAreRemoved(i, pyramidFlags);
                boolean actual = isInArray(i, pyramid.getUncoveredIndexes());
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testAllIndexes() {
        for (Pyramid pyramid : Pyramid.ALL) {
            for (int i = 0; i < 28; i++) {
                boolean expected = cardExists(i, pyramid.getFlags());
                boolean actual = isInArray(i, pyramid.getAllIndexes());
                assertEquals(expected, actual);
            }
        }
    }

    /**
     * Define a location in PYRAMID_INDEXES by its row and column.
     */
    class PyramidLocation {
        int pyramidIndex;
        int row;
        int column;

        /**
         * Get the location inside PYRAMID_INDEX that pyramidIndex is at.
         *
         * @param pyramidIndex an index from 0-27 inclusive
         */
        PyramidLocation(int pyramidIndex) {
            this.pyramidIndex = pyramidIndex;
            for (int row = 0; row < 7; row++) {
                for (int column = 0; column < 7 - row; column++) {
                    if (PYRAMID_INDEXES[row][column] == pyramidIndex) {
                        this.row = row;
                        this.column = column;
                        return;
                    }
                }
            }
        }

        /**
         * Return true if the given location is covered by this.  Another way to put it is
         * that the location passed in is an ancestor of this location.
         *
         * @param location another PyramidLocation
         * @return true if the given location is covered by this location
         */
        boolean isCovering(PyramidLocation location) {
            return ((location.row <= this.row) &&
                    (location.column <= this.column) &&
                    (location.pyramidIndex != this.pyramidIndex));
        }
    }
}
