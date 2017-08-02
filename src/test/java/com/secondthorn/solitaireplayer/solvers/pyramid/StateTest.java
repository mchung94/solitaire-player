package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateTest {
    static final long FULL_STATE = makeUnadjustedState(0xFFFFFFFFFFFFFL, 28, 1);
    private static final long END_STATE = makeUnadjustedState(0L, 52, 3);

    /**
     * Create a State (a long value) with the given deckFlags/stockIndex/cycle.
     * For testing purposes, don't adjust the given stockIndex to the next existing card in the deck.
     *
     * @param deckFlags  a 52-bit flags value indicating which cards in the deck exist
     * @param stockIndex an index into deckFlags (value from 28 to 52) indicating the top card in the stock pile
     * @param cycle      the cycle through the stock pile the game is currently on (value from 1 to 3)
     * @return a State object, just a long value holding 60 bits encapsulating the 3 values
     */
    private static long makeUnadjustedState(long deckFlags, int stockIndex, int cycle) {
        return deckFlags | (long) stockIndex << 52 | (long) cycle << 58;
    }

    @Test
    public void initialState() {
        assertEquals(FULL_STATE, State.INITIAL_STATE);
    }

    @Test
    public void isStockEmpty() {
        for (int stockIndex = 28; stockIndex <= 52; stockIndex++) {
            assertEquals(stockIndex == 52, State.isStockEmpty(stockIndex));
        }
    }

    @Test
    public void isWasteEmpty() {
        for (int wasteIndex = 27; wasteIndex <= 51; wasteIndex++) {
            assertEquals(wasteIndex == 27, State.isWasteEmpty(wasteIndex));
        }
    }

    @Test
    public void createState() {
        assertEquals(FULL_STATE, State.createState(0xFFFFFFFFFFFFFL, 28, 1));
        assertEquals(END_STATE, State.createState(0L, 52, 3));
    }

    @Test
    public void getDeckFlags() {
        assertEquals(0xFFFFFFFFFFFFFL, State.getDeckFlags(FULL_STATE));
        assertEquals(0L, State.getDeckFlags(END_STATE));
    }

    @Test
    public void getPyramidFlags() {
        assertEquals(0xFFFFFFFL, State.getPyramidFlags(FULL_STATE));
        assertEquals(0L, State.getPyramidFlags(END_STATE));
    }

    @Test
    public void getStockIndex() {
        assertEquals(28, State.getStockIndex(FULL_STATE));
        assertEquals(52, State.getStockIndex(END_STATE));
    }

    @Test
    public void getCycle() {
        assertEquals(1, State.getCycle(FULL_STATE));
        assertEquals(3, State.getCycle(END_STATE));
    }

    @Test
    public void getWasteIndex() {
        assertEquals(27, State.getWasteIndex(State.getDeckFlags(FULL_STATE), State.getStockIndex(FULL_STATE)));
        assertEquals(27, State.getWasteIndex(State.getDeckFlags(END_STATE), State.getStockIndex(END_STATE)));
        int emptyStockPileIndex = 52;
        long noCardsRemovedYet = 0xFFFFFFFFFFFFFL;
        long allStockAndWasteCardsRemovedExceptFirst = 0x000001FFFFFFFL;
        assertEquals(51, State.getWasteIndex(noCardsRemovedYet, emptyStockPileIndex));
        assertEquals(28, State.getWasteIndex(allStockAndWasteCardsRemovedExceptFirst, emptyStockPileIndex));
    }

    @Test
    public void mask() {
        for (int i = 0; i < 52; i++) {
            assertEquals(0, (1L << i) ^ State.mask(i));
        }
    }

    @Test
    public void removalMask() {
        for (int i = 0; i < 52; i++) {
            assertEquals(0xFFFFFFFFFFFFFL, (1L << i) ^ State.removalMask(i));
        }
    }

//    private static final long FULL_STATE = State.createState(0xFFFFFFFFFFFFFL, 28, 1);
//    private static final long EMPTY_STATE = State.createState(0, 28, 1);
//
//    @Test
//    public void initialState() {
//        assertEquals(FULL_STATE, State.INITIAL_STATE);
//    }
//
//    @Test
//    public void isTableClearEmptyTable() {
//        assertTrue(State.isTableClear(EMPTY_STATE));
//    }
//
//    @Test
//    public void isTableClearFullTable() {
//        assertFalse(State.isTableClear(FULL_STATE));
//    }
//
//    @Test
//    public void isTableClearJustOneTableCard() {
//        long state = State.createState(1, 28, 1);
//        assertFalse(State.isTableClear(state));
//    }
//
//    private int firstChildIndex(int tableIndex) {
//        int[][] tableIndexes = {
//                {0, 2, 5, 9, 14, 20, 27},
//                {1, 4, 8, 13, 19, 26},
//                {3, 7, 12, 18, 25},
//                {6, 11, 17, 24},
//                {10, 16, 23},
//                {15, 22},
//                {21},
//        };
//        for (int row = 0; row < 6; row++) {
//            for (int col = 0; col < (6 - row); col++) {
//                if (tableIndex == tableIndexes[row][col]) {
//                    return tableIndexes[row + 1][col];
//                }
//            }
//        }
//        return 0;
//    }
//
//    @Test
//    public void checkTableUncoveredMasks() {
//        assertEquals(28, State.TABLE_UNCOVERED_MASKS.length);
//        for (int tableIndex = 0; tableIndex < 28; tableIndex++) {
//            long mask = 1 << tableIndex;
//            if (tableIndex < 21) {
//                int firstChildIndex = firstChildIndex(tableIndex);
//                mask = mask | (0b11 << firstChildIndex);
//            }
//            assertEquals(mask, State.TABLE_UNCOVERED_MASKS[tableIndex]);
//        }
//    }
//
//    private void assertUncoveredIndexesMatch(int[] expected, long state, int... indexesToRemove) {
//        for (int i : indexesToRemove) {
//            state &= ~(1L << i);
//        }
//        TIntList expectedIndexes = new TIntArrayList(expected);
//        TIntList actualIndexes = State.uncoveredTableIndexes(state);
//        assertEquals(new TIntHashSet(expectedIndexes), new TIntHashSet(actualIndexes));
//    }
//
//    @Test
//    public void uncoveredTableIndexesBottomRow() {
//        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 26, 27}, FULL_STATE);
//        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 27}, FULL_STATE, 26);
//        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 26}, FULL_STATE, 27);
//        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 20}, FULL_STATE, 26, 27);
//        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25}, FULL_STATE, 20, 26, 27);
//    }
//
//    @Test
//    public void uncoveredTableIndexesLastCard() {
//        long state = State.createState(0b111, 28, 1);
//        assertUncoveredIndexesMatch(new int[]{0}, state, 1, 2);
//        assertUncoveredIndexesMatch(new int[]{2}, state, 1);
//        assertUncoveredIndexesMatch(new int[]{1}, state, 2);
//        assertUncoveredIndexesMatch(new int[]{1, 2}, state);
//        assertUncoveredIndexesMatch(new int[]{}, state, 0, 1, 2);
//    }
//
//
//
//    @Test
//    public void hCostWithFullDeck() {
//        Deck orderedDeck = new Deck(DeckTest.orderedCards);
//        assertEquals(16, State.hCost(FULL_STATE, orderedDeck));
//    }
//
//    @Test
//    public void hCostWithEmptyDeck() {
//        Deck orderedDeck = new Deck(DeckTest.orderedCards);
//        assertEquals(0, State.hCost(EMPTY_STATE, orderedDeck));
//    }
//
//    @Test
//    public void hCostWithPartialTable() {
//        Deck orderedDeck = new Deck(DeckTest.orderedCards);
//        long partialState = ~(0b11L << 25) & FULL_STATE;
//        assertEquals(14, State.hCost(partialState, orderedDeck));
//    }
//
//
}
