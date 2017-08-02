package com.secondthorn.solitaireplayer.solvers.pyramid;

/**
 * States represent the "state of the world" of each step while playing Pyramid Solitaire.
 * For performance purposes, States are just longs, and this class implements static methods for
 * handling them.
 * <p>
 * The state is represented using 60 bits (the last four bits of the long are ignored):
 * <ul>
 *   <li>Bits 0-51 represent the existence of each card in the deck</li>
 *   <ul>
 *     <li>Bits 0-27 are the 28 pyramid cards</li>
 *     <li>Bits 28-51 are the 24 stock/waste cards, 28 is the initial top of the deck</li>
 *   </ul>
 *   <li>Bits 52-57 is a 6-bit number from 28-52 to indicate the stock index</li>
 *   <ul>
 *     <li>The card at the stock index is the top of the stock pile</li>
 *     <li>Cards above the stock index are the rest of the stock cards</li>
 *     <li>Cards below the stock index are the waste pile, the closest one to the stock index is the top</li>
 *     <li>Stock index 52 means the stock is empty, waste index 27 means the waste pile is empty</li>
 *   </ul>
 *   <li>Bits 58-60 is a 2-bit number from 1-3 indicating which cycle through the deck the player is on</li>
 * </ul>
 */
class State {
    /**
     * The stock index is an integer from 28 to 52.  52 means the stock pile is empty.
     */
    private static final int EMPTY_STOCK = 52;
    /**
     * The initial state when playing Pyramid Solitaire: no cards removed yet, stock index is at the 28th card
     * and the cycle is 1.
     */
    static final long INITIAL_STATE = createState(0xFFFFFFFFFFFFFL, 28, 1);
    /**
     * The waste index is an integer from 27 to 51.  27 means the waste pile is empty.
     */
    private static final int EMPTY_WASTE = 27;

    /**
     * Return true if the stock pile is empty.
     *
     * @param stockIndex the stock index (28-52)
     * @return true if the stock pile is empty, false otherwise
     */
    static boolean isStockEmpty(int stockIndex) {
        return stockIndex == EMPTY_STOCK;
    }

    /**
     * Return true if the waste pile is empty.
     *
     * @param wasteIndex the waste index (27-51)
     * @return true if the waste pile is empty, false otherwise
     */
    static boolean isWasteEmpty(int wasteIndex) {
        return wasteIndex == EMPTY_WASTE;
    }

    /**
     * Create a new state (a long value) given the three values to be packed into the state.
     *
     * @param deckFlags  52 bits showing which cards in the deck haven't been removed yet
     * @param stockIndex an integer from 28-52 indicating the top of the stock (or empty if 52)
     * @param cycle      an integer from 1-3 indicating which cycle through the deck cards we're on
     * @return a new long representing the state
     */
    static long createState(long deckFlags, int stockIndex, int cycle) {
        while (!isStockEmpty(stockIndex) && (((1L << stockIndex) & deckFlags) == 0)) {
            stockIndex++;
        }
        return deckFlags | ((long) stockIndex << 52) | ((long) cycle << 58);
    }

    /**
     * Given a state, return the 52 bits showing which cards in the deck haven't been removed yet.
     *
     * @param state a long value for the Pyramid Solitaire state
     * @return the 52 bit existence flag values showing which cards in the deck haven't been removed
     */
    static long getDeckFlags(long state) {
        return 0xFFFFFFFFFFFFFL & state;
    }

    /**
     * Given a state, return the 28 bits showing which cards in the pyramid haven't been removed yet.
     *
     * @param state a long value for the Pyramid Solitaire state
     * @return the 28 bit existence flag values showing which cards in the pyramid haven't been removed
     */
    static long getPyramidFlags(long state) {
        return 0xFFFFFFFL & state;
    }

    /**
     * Given a state, return the stock index.
     *
     * @param state a long value for the Pyramid Solitaire state
     * @return the stock index integer from 28-52, 52 means the deck is empty
     */
    static int getStockIndex(long state) {
        return 0b111111 & (int) (state >>> 52);
    }

    /**
     * Given a state, return the cycle.
     *
     * @param state a long value for the Pyramid Solitaire state
     * @return the cycle integer from 1-3
     */
    static int getCycle(long state) {
        return 0b11 & (int) (state >>> 58);
    }

    /**
     * Given 52 bit deck flags and the stock index, derive the waste index which points to the
     * first card in the waste pile.  27 means the waste pile is empty.
     *
     * @param deckFlags  the 52 bit existence flag values for a state
     * @param stockIndex the stock index for a state
     * @return the waste index for the state
     */
    static int getWasteIndex(long deckFlags, int stockIndex) {
        int wasteIndex = stockIndex - 1;
        while (!isWasteEmpty(wasteIndex)) {
            if (((1L << wasteIndex) & deckFlags) != 0) {
                return wasteIndex;
            }
            wasteIndex--;
        }
        return wasteIndex;
    }

    /**
     * Given a deck index, return a mask with that bit set to 1 to single out the card from the deck.  We use this
     * to check if the card exists in the deck.
     *
     * @param deckIndex a deck card index from 0 to 51 inclusive
     * @return a mask that singles out the card at the deck index
     */
    static long mask(int deckIndex) {
        return 1L << deckIndex;
    }

    /**
     * Given a deck index, return a mask to exclude the card from the deck.  We use this to remove cards from the deck.
     *
     * @param deckIndex a deck card index from 0 to 51 inclusive
     * @return a mask that excludes that card from the deck
     */
    static long removalMask(int deckIndex) {
        return ~mask(deckIndex) & 0xFFFFFFFFFFFFFL;
    }
}
