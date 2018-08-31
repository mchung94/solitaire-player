package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
