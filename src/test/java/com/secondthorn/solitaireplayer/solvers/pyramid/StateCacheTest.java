package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateCacheTest {
    @Test
    public void isPyramidClear() {
        for (Pyramid pyramid : Pyramid.ALL) {
            long pyramidFlags = pyramid.getFlags();
            StateCache stateCache = DeckTest.deck.getStateCache(pyramidFlags);
            assertEquals((pyramidFlags & 0xFFFFFFF) == 0L, stateCache.isPyramidClear());
        }
    }

    /**
     * Count how many cards of the given cardValue (1-13) are in pyramidIndexes
     *
     * @param cardValue      a card rank value (from 1 - 13)
     * @param pyramidIndexes the pyramid card indexes to check
     * @param deck           a deck, just used to determine card values
     * @return how many cards in pyramidIndexes have value equal to cardValue
     */
    private int countCardsOfValue(int cardValue, int[] pyramidIndexes, Deck deck) {
        int count = 0;
        for (int index : pyramidIndexes) {
            if (deck.cardValue(index) == cardValue) {
                count++;
            }
        }
        return count;
    }

    /**
     * An alternate implementation of the heuristic cost function, estimating the number of steps to remove all the
     * pyramid cards given by pyramidIndexes.
     *
     * @param pyramidIndexes the indexes cards remaining in the pyramid
     * @param deck           a deck, just used to determine card values
     * @return an estimate for the number of steps to remove the remaining cards from the pyramid
     */
    private int heuristicCost(int[] pyramidIndexes, Deck deck) {
        return countCardsOfValue(13, pyramidIndexes, deck) +
                Math.max(countCardsOfValue(1, pyramidIndexes, deck), countCardsOfValue(12, pyramidIndexes, deck)) +
                Math.max(countCardsOfValue(2, pyramidIndexes, deck), countCardsOfValue(11, pyramidIndexes, deck)) +
                Math.max(countCardsOfValue(3, pyramidIndexes, deck), countCardsOfValue(10, pyramidIndexes, deck)) +
                Math.max(countCardsOfValue(4, pyramidIndexes, deck), countCardsOfValue(9, pyramidIndexes, deck)) +
                Math.max(countCardsOfValue(5, pyramidIndexes, deck), countCardsOfValue(8, pyramidIndexes, deck)) +
                Math.max(countCardsOfValue(6, pyramidIndexes, deck), countCardsOfValue(7, pyramidIndexes, deck));
    }

    @Test
    public void heuristicCost() {
        for (Pyramid pyramid : Pyramid.ALL) {
            StateCache stateCache = DeckTest.deck.getStateCache(pyramid.getFlags());
            int expected = heuristicCost(pyramid.getAllIndexes(), DeckTest.deck);
            int actual = stateCache.getHeuristicCost();
            assertEquals(expected, actual);
        }
    }

    /**
     * An alternate implementation of the unwinnableMasks function, which generates all the masks to determine
     * if there exists a pyramid card that can't be removed.
     *
     * @param pyramidIndexes the indexes cards remaining in the pyramid
     * @param deck           a deck, just used to determine card values
     * @return an array of masks used to check if
     */
    private long[] unwinnableMasks(int[] pyramidIndexes, Deck deck) {
        TLongSet unwinnableMasks = new TLongHashSet();
        for (int pyramidIndex : pyramidIndexes) {
            int matchingCardValue = 13 - deck.cardValue(pyramidIndex);
            if (matchingCardValue != 0) {
                long mask = 0L;
                for (int i = 0; i < 52; i++) {
                    if (deck.cardValue(i) == matchingCardValue) {
                        mask |= 1L << i;
                    }
                }
                unwinnableMasks.add(mask & Pyramid.UNRELATED_CARD_MASKS[pyramidIndex]);
            }
        }
        return unwinnableMasks.toArray();
    }

    private void assertArraysContainSameValues(long[] expected, long[] actual) {
        TLongSet expectedSet = new TLongHashSet(expected);
        assertEquals(expectedSet.size(), actual.length);
        assertTrue(expectedSet.containsAll(actual));
    }

    @Test
    public void unwinnableMasks() {
        for (Pyramid pyramid : Pyramid.ALL) {
            StateCache stateCache = DeckTest.deck.getStateCache(pyramid.getFlags());
            long[] expected = unwinnableMasks(pyramid.getAllIndexes(), DeckTest.deck);
            long[] actual = stateCache.unwinnableMasks;
            assertArraysContainSameValues(expected, actual);
        }
    }

    /**
     * Create a card removal mask that excludes the bits in indexesToRemove
     *
     * @param indexesToRemove deck indexes (0-51) to exclude in the mask
     * @return a card removal mask with the bits at indexesToRemove unset
     */
    private long makeRemovalMask(int... indexesToRemove) {
        long mask = 0L;
        for (int index : indexesToRemove) {
            mask |= 1L << index;
        }
        return ~mask & 0xFFFFFFFFFFFFFL;
    }

    /**
     * An alternate implementation of successor masks for a given set of uncovered pyramid indexes, stock index, and
     * waste index.
     *
     * @param uncoveredIndexes the uncovered pyramid indexes
     * @param stockIndex       the stock index (top card of the stock pile or 52 if empty)
     * @param wasteIndex       the waste index (top card of the waste pile or 27 if empty)
     * @param deck             a deck, just used to determine card values
     * @return all the successor card removal masks for the given indexes available for removal
     */
    private long[] successorMasks(int[] uncoveredIndexes, int stockIndex, int wasteIndex, Deck deck) {
        TLongList masks = new TLongArrayList();
        TIntList indexes = new TIntArrayList(uncoveredIndexes);
        if (stockIndex != 52) {
            indexes.add(stockIndex);
        }
        if (wasteIndex != 27) {
            indexes.add(wasteIndex);
        }
        for (int i = 0; i < indexes.size(); i++) {
            int cardIndex1 = indexes.get(i);
            int cardValue = deck.cardValue(cardIndex1);
            if (cardValue == 13) {
                masks.add(makeRemovalMask(cardIndex1));
            } else {
                for (int j = i + 1; j < indexes.size(); j++) {
                    int cardIndex2 = indexes.get(j);
                    if (cardValue + deck.cardValue(cardIndex2) == 13) {
                        masks.add(makeRemovalMask(cardIndex1, cardIndex2));
                    }
                }
            }
        }
        return masks.toArray();
    }

    @Test
    public void successorMasks() {
        for (Pyramid pyramid : Pyramid.ALL) {
            StateCache stateCache = DeckTest.deck.getStateCache(pyramid.getFlags());
            for (int stockIndex = 28; stockIndex < 53; stockIndex++) {
                for (int wasteIndex = 27; wasteIndex < stockIndex; wasteIndex++) {
                    long[] expected = successorMasks(pyramid.getUncoveredIndexes(), stockIndex, wasteIndex, DeckTest.deck);
                    long[] actual = stateCache.successorMaskTable[stockIndex][wasteIndex];
                    assertArraysContainSameValues(expected, actual);
                }
            }
        }
    }

    @Test
    public void isUnwinnableWithUnwinnableState() {
        Deck deck = new Deck(
                "2d 9s 7c 5d 2s Qc Jd 5c Jc Td 4s 6s 8c 8s Jh 5h As Js 6d 2c Qd Qh 4c 8h Ks 7d " +
                "Ah 4d 9h 3d 5s 4h Th Ad 3s 8d Ts Tc 9d Kc 7h Kd 6h Qs 2h Ac 7s 6c 3c 3h 9c Kh"
        );
        StateCache stateCache = deck.getStateCache(State.getPyramidFlags(StateTest.FULL_STATE));
        assertTrue(stateCache.isUnwinnable(StateTest.FULL_STATE));
    }

    @Test
    public void isUnwinnableWithWinnableState() {
        Deck deck = new Deck(
                "6d 5h Ah Jd 4s Ks 6s 8c 2h 4d 9s Kd 6c Ad 8s Ac 5c 9d 7h 3h 8d 5s 4c Qc Jh Kc " +
                "Kh 3c 3s 9c As 5d Qh Ts 4h 7s Td 9h Th 7c 8h 2c 7d Tc 2d 6h 2s Js Qd 3d Qs Jc"
        );
        StateCache stateCache = deck.getStateCache(State.getPyramidFlags(StateTest.FULL_STATE));
        assertFalse(stateCache.isUnwinnable(StateTest.FULL_STATE));
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
        StateCache stateCache = deck.getStateCache(State.getPyramidFlags(startState));
        TLongSet actual = new TLongHashSet(stateCache.getSuccessors(startState));
        assertEquals(expected, actual);
    }

    @Test
    public void successorsWithDraw() {
        assertSuccessors(DeckTest.deck, StateTest.FULL_STATE,
                successorState(DeckTest.deck, StateTest.FULL_STATE, 29, 1),
                successorState(DeckTest.deck, StateTest.FULL_STATE, 28, 1, "Jd", "2h"),
                successorState(DeckTest.deck, StateTest.FULL_STATE, 28, 1, "Qd", "Ah"),
                successorState(DeckTest.deck, StateTest.FULL_STATE, 28, 1, "Kd"),
                successorState(DeckTest.deck, StateTest.FULL_STATE, 29, 1, "Td", "3h"));
    }

    @Test
    public void successorsWithRecycle() {
        long state = State.createState(0xFFFFFFFFFFFFFL, 52, 1);
        assertSuccessors(DeckTest.deck, state,
                successorState(DeckTest.deck, state, 28, 2),
                successorState(DeckTest.deck, state, 52, 1, "Jd", "2h"),
                successorState(DeckTest.deck, state, 52, 1, "Qd", "Ah"),
                successorState(DeckTest.deck, state, 52, 1, "Kd"),
                successorState(DeckTest.deck, state, 52, 1, "Ks"));
    }

    @Test
    public void successorsAtCycle3() {
        long state = State.createState(0xFFFFFFFFFFFFFL, 52, 3);
        assertSuccessors(DeckTest.deck, state,
                successorState(DeckTest.deck, state, 52, 3, "Jd", "2h"),
                successorState(DeckTest.deck, state, 52, 3, "Qd", "Ah"),
                successorState(DeckTest.deck, state, 52, 3, "Kd"),
                successorState(DeckTest.deck, state, 52, 3, "Ks"));
    }

    @Test
    public void successorsAtEndOfDeck() {
        long state = State.createState(0xFFFFFFFFFFFFFL, 51, 1);
        assertSuccessors(DeckTest.deck, state,
                successorState(DeckTest.deck, state, 52, 1),
                successorState(DeckTest.deck, state, 51, 1, "Jd", "2h"),
                successorState(DeckTest.deck, state, 51, 1, "Qd", "Ah"),
                successorState(DeckTest.deck, state, 51, 1, "Kd"),
                successorState(DeckTest.deck, state, 51, 1, "Ah", "Qs"),
                successorState(DeckTest.deck, state, 52, 1, "Ks"));
    }

    @Test
    public void successorsWithDeckAndWastePair() {
        long state = State.createState(0xFFFFFFFFFFFFFL, 32, 1);
        assertSuccessors(DeckTest.deck, state,
                successorState(DeckTest.deck, state, 33, 1),
                successorState(DeckTest.deck, state, 33, 1, "6h", "7h"),
                successorState(DeckTest.deck, state, 32, 1, "Jd", "2h"),
                successorState(DeckTest.deck, state, 32, 1, "Qd", "Ah"),
                successorState(DeckTest.deck, state, 32, 1, "Kd"));
    }

    @Test
    public void successorsWithEmptyWaste() {
        long state = State.createState(0xFFFFF0FFFFFFFL, 32, 1);
        assertSuccessors(DeckTest.deck, state,
                successorState(DeckTest.deck, state, 33, 1),
                successorState(DeckTest.deck, state, 32, 1, "Jd", "2h"),
                successorState(DeckTest.deck, state, 32, 1, "Qd", "Ah"),
                successorState(DeckTest.deck, state, 32, 1, "Kd"));
    }

}
