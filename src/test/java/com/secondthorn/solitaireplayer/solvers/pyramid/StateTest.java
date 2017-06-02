package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class StateTest {
    private static final long FULL_STATE = State.createState(0xFFFFFFFFFFFFFL, 28, 1);
    private static final long EMPTY_STATE = State.createState(0, 28, 1);

    @Test
    public void initialState() {
        assertEquals(FULL_STATE, State.INITIAL_STATE);
    }

    @Test
    public void isTableClearEmptyTable() {
        assertTrue(State.isTableClear(EMPTY_STATE));
    }

    @Test
    public void isTableClearFullTable() {
        assertFalse(State.isTableClear(FULL_STATE));
    }

    @Test
    public void isTableClearJustOneTableCard() {
        long state = State.createState(1, 28, 1);
        assertFalse(State.isTableClear(state));
    }

    private int firstChildIndex(int tableIndex) {
        int[][] tableIndexes = {
                {0, 2, 5, 9, 14, 20, 27},
                {1, 4, 8, 13, 19, 26},
                {3, 7, 12, 18, 25},
                {6, 11, 17, 24},
                {10, 16, 23},
                {15, 22},
                {21},
        };
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < (6 - row); col++) {
                if (tableIndex == tableIndexes[row][col]) {
                    return tableIndexes[row + 1][col];
                }
            }
        }
        return 0;
    }

    @Test
    public void checkTableUncoveredMasks() {
        assertEquals(28, State.TABLE_UNCOVERED_MASKS.length);
        for (int tableIndex = 0; tableIndex < 28; tableIndex++) {
            long mask = 1 << tableIndex;
            if (tableIndex < 21) {
                int firstChildIndex = firstChildIndex(tableIndex);
                mask = mask | (0b11 << firstChildIndex);
            }
            assertEquals(mask, State.TABLE_UNCOVERED_MASKS[tableIndex]);
        }
    }

    private void assertUncoveredIndexesMatch(int[] expected, long state, int... indexesToRemove) {
        for (int i : indexesToRemove) {
            state &= ~(1L << i);
        }
        TIntList expectedIndexes = new TIntArrayList(expected);
        TIntList actualIndexes = State.uncoveredTableIndexes(state);
        assertEquals(new TIntHashSet(expectedIndexes), new TIntHashSet(actualIndexes));
    }

    @Test
    public void uncoveredTableIndexesBottomRow() {
        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 26, 27}, FULL_STATE);
        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 27}, FULL_STATE, 26);
        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 26}, FULL_STATE, 27);
        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25, 20}, FULL_STATE, 26, 27);
        assertUncoveredIndexesMatch(new int[]{21, 22, 23, 24, 25}, FULL_STATE, 20, 26, 27);
    }

    @Test
    public void uncoveredTableIndexesLastCard() {
        long state = State.createState(0b111, 28, 1);
        assertUncoveredIndexesMatch(new int[]{0}, state, 1, 2);
        assertUncoveredIndexesMatch(new int[]{2}, state, 1);
        assertUncoveredIndexesMatch(new int[]{1}, state, 2);
        assertUncoveredIndexesMatch(new int[]{1, 2}, state);
        assertUncoveredIndexesMatch(new int[]{}, state, 0, 1, 2);
    }

    private int getDeckIndex(Deck deck, String card) {
        for (int i = 0; i < 52; i++) {
            if (deck.cardAt(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    private long successorState(Deck deck, long startState, int deckIndex, int cycle, String... cardsToRemove) {
        long successorState = (startState & 0xFFFFFFFFFFFFFL) | ((long) deckIndex << 52) | ((long) cycle << 58);
        for (String card : cardsToRemove) {
            int index = getDeckIndex(deck, card);
            assertNotEquals(-1, index);
            successorState &= ~(1L << index);
        }
        return successorState;
    }

    private void assertSuccessors(Deck deck, long startState, long... expectedSuccessors) {
        TLongSet expected = new TLongHashSet(expectedSuccessors);
        TLongSet actual = new TLongHashSet(State.successors(startState, deck));
        assertEquals(expected, actual);
    }

    @Test
    public void successorsWithDraw() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        assertSuccessors(orderedDeck, FULL_STATE,
                successorState(orderedDeck, FULL_STATE, 29, 1),
                successorState(orderedDeck, FULL_STATE, 28, 1, "Jd", "2h"),
                successorState(orderedDeck, FULL_STATE, 28, 1, "Qd", "Ah"),
                successorState(orderedDeck, FULL_STATE, 28, 1, "Kd"),
                successorState(orderedDeck, FULL_STATE, 29, 1, "Td", "3h"));
    }

    @Test
    public void successorsWithRecycle() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        long state = State.createState(0xFFFFFFFFFFFFFL, 52, 1);
        assertSuccessors(orderedDeck, state,
                successorState(orderedDeck, state, 28, 2),
                successorState(orderedDeck, state, 52, 1, "Jd", "2h"),
                successorState(orderedDeck, state, 52, 1, "Qd", "Ah"),
                successorState(orderedDeck, state, 52, 1, "Kd"),
                successorState(orderedDeck, state, 52, 1, "Ks"));
    }

    @Test
    public void successorsAtCycle3() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        long state = State.createState(0xFFFFFFFFFFFFFL, 52, 3);
        assertSuccessors(orderedDeck, state,
                successorState(orderedDeck, state, 52, 3, "Jd", "2h"),
                successorState(orderedDeck, state, 52, 3, "Qd", "Ah"),
                successorState(orderedDeck, state, 52, 3, "Kd"),
                successorState(orderedDeck, state, 52, 3, "Ks"));
    }

    @Test
    public void successorsAtEndOfDeck() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        long state = State.createState(0xFFFFFFFFFFFFFL, 51, 1);
        assertSuccessors(orderedDeck, state,
                successorState(orderedDeck, state, 52, 1),
                successorState(orderedDeck, state, 51, 1, "Jd", "2h"),
                successorState(orderedDeck, state, 51, 1, "Qd", "Ah"),
                successorState(orderedDeck, state, 51, 1, "Kd"),
                successorState(orderedDeck, state, 51, 1, "Ah", "Qs"),
                successorState(orderedDeck, state, 52, 1, "Ks"));
    }

    @Test
    public void successorsWithDeckAndWastePair() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        long state = State.createState(0xFFFFFFFFFFFFFL, 32, 1);
        assertSuccessors(orderedDeck, state,
                successorState(orderedDeck, state, 33, 1),
                successorState(orderedDeck, state, 33, 1, "6h", "7h"),
                successorState(orderedDeck, state, 32, 1, "Jd", "2h"),
                successorState(orderedDeck, state, 32, 1, "Qd", "Ah"),
                successorState(orderedDeck, state, 32, 1, "Kd"));
    }

    @Test
    public void successorsWithEmptyWaste() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        long state = State.createState(0xFFFFF0FFFFFFFL, 32, 1);
        assertSuccessors(orderedDeck, state,
                successorState(orderedDeck, state, 33, 1),
                successorState(orderedDeck, state, 32, 1, "Jd", "2h"),
                successorState(orderedDeck, state, 32, 1, "Qd", "Ah"),
                successorState(orderedDeck, state, 32, 1, "Kd"));
    }

    @Test
    public void hCostWithFullDeck() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        assertEquals(16, State.hCost(FULL_STATE, orderedDeck));
    }

    @Test
    public void hCostWithEmptyDeck() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        assertEquals(0, State.hCost(EMPTY_STATE, orderedDeck));
    }

    @Test
    public void hCostWithPartialTable() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        long partialState = ~(0b11L << 25) & FULL_STATE;
        assertEquals(14, State.hCost(partialState, orderedDeck));
    }

    @Test
    public void isUnwinnableWithUnwinnableState() {
        Deck deck = new Deck(
                "2d 9s 7c 5d 2s Qc Jd 5c Jc Td 4s 6s 8c 8s Jh 5h As Js 6d 2c Qd Qh 4c 8h Ks 7d " +
                "Ah 4d 9h 3d 5s 4h Th Ad 3s 8d Ts Tc 9d Kc 7h Kd 6h Qs 2h Ac 7s 6c 3c 3h 9c Kh"
        );
        assertTrue(State.isUnwinnable(FULL_STATE, deck));
    }

    @Test
    public void isUnwinnableWithWinnableState() {
        Deck deck = new Deck(
                "6d 5h Ah Jd 4s Ks 6s 8c 2h 4d 9s Kd 6c Ad 8s Ac 5c 9d 7h 3h 8d 5s 4c Qc Jh Kc " +
                "Kh 3c 3s 9c As 5d Qh Ts 4h 7s Td 9h Th 7c 8h 2c 7d Tc 2d 6h 2s Js Qd 3d Qs Jc"
        );
        assertFalse(State.isUnwinnable(FULL_STATE, deck));
    }

    private long removeACard(long state, char rankToRemove, Deck deck) {
        for (int i=0; i<52; i++) {
            long cardMask = 1L << i;
            if (((state & cardMask) != 0) && (deck.cardAt(i).charAt(0) == rankToRemove)) {
                return ~cardMask & state;
            }
        }
        return state;
    }

    private void assertNumCardsOfRankRemoved(long state, char rank, Deck deck) {
        assertEquals(0, State.numCardsOfRankRemoved(state, rank, deck));
        state = removeACard(state, rank, deck);
        assertEquals(1, State.numCardsOfRankRemoved(state, rank, deck));
        state = removeACard(state, rank, deck);
        assertEquals(2, State.numCardsOfRankRemoved(state, rank, deck));
        state = removeACard(state, rank, deck);
        assertEquals(3, State.numCardsOfRankRemoved(state, rank, deck));
        state = removeACard(state, rank, deck);
        assertEquals(4, State.numCardsOfRankRemoved(state, rank, deck));
    }

    @Test
    public void numCardsOfRankRemoved() {
        Deck orderedDeck = new Deck(DeckTest.orderedCards);
        for (char rank : "A23456789TJQK".toCharArray()) {
            assertNumCardsOfRankRemoved(FULL_STATE, rank, orderedDeck);
        }
    }
}
