package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.List;

/**
 * A Deck is a standard deck of 52 cards containing the suits c d h s, and the ranks A 2 3 4 5 6 7 8 9 T J Q K.
 * In Pyramid Solitaire, the first 28 cards will be the table cards and the remaining 24 will be the deck.
 * Card index 28 is the top of the deck at the start of the game and index 51 will be the bottom.
 * <p>
 * Cards are represented as two-letter strings containing rank and suit, for example Kc or 7s.
 * <p>
 * A Deck precalculates a lot of information about the cards so that anything the solver requires just turns into
 * an array index operation and nothing too complicated.
 */
public class Deck {
    private String[] cards;
    private int[] values;
    private long[] cardRankMasks;
    private TLongObjectMap<StateCache> stateCaches;

    /**
     * Create a new deck out of a String array of cards.
     *
     * @param cards an array of 52 cards
     */
    public Deck(String[] cards) {
        if (cards.length != 52) {
            throw new IllegalArgumentException("A Deck must be 52 cards, " + cards.length + " sent in instead.");
        }
        this.cards = cards;
        this.values = calcCardValues(cards);
        this.cardRankMasks = calcCardRankMasks(values);
        this.stateCaches = calcStateCaches();
    }

    /**
     * Create a new deck out of a single string containing all the cards.
     *
     * @param cards a String containing 52 space-delimited cards
     */
    public Deck(String cards) {
        this(cards.trim().split("\\s+"));
    }

    /**
     * Create a new deck out of a List of cards.
     *
     * @param cards a List containing 52 cards
     */
    public Deck(List<String> cards) {
        this(cards.toArray(new String[cards.size()]));
    }

    /**
     * Return a string representation of the card at deckIndex.
     *
     * @param deckIndex the index into the deck of cards
     * @return the card at the deckIndex
     */
    String cardAt(int deckIndex) {
        return cards[deckIndex];
    }

    /**
     * Return the card's numeric value.  Aces are always 1, Jacks are 11, Queens are 12, and Kings are 13.
     *
     * @param deckIndex the index into the deck of cards
     * @return the card's numeric value
     */
    int cardValue(int deckIndex) {
        return values[deckIndex];
    }

    /**
     * Return true if the card at deckIndex is a King.
     *
     * @param deckIndex the index into the deck of cards
     * @return true if the card at deckIndex is a King, false otherwise
     */
    boolean isKing(int deckIndex) {
        return values[deckIndex] == 13;
    }

    /**
     * For a given card value, return a bit mask with the bits set only for the four cards in the deck
     * of that value.
     *
     * @param value a card value, from 1 to 13 representing Ace through King
     * @return a long bit mask with the bits set for the positions of the four cards in the deck of that rank
     */
    long cardRankMask(int value) {
        return cardRankMasks[value];
    }

    /**
     * Return true if the cards at the deck indexes add up to 13 and potentially can be removed together if
     * they're both uncovered.
     *
     * @param deckIndex1 a card deck index
     * @param deckIndex2 another card deck index
     * @return true if they match (add up to 13 together)
     */
    boolean cardsMatch(int deckIndex1, int deckIndex2) {
        return cardValue(deckIndex1) + cardValue(deckIndex2) == 13;
    }

    /**
     * For a given Pyramid flags value, return the StateCache that has all the precalculated data for it.
     *
     * @param pyramidFlags 28-bit flags (as a long) for the 28 pyramid cards
     * @return a StateCache with all the data relating to that particular pyramidFlags value
     */
    StateCache getStateCache(long pyramidFlags) {
        return stateCaches.get(pyramidFlags);
    }

    /**
     * Return the card's rank, assuming the card is a two-letter string and the rank character is the first one.
     *
     * @param card a two-letter representation of the card's rank and suit
     * @return the card's rank as a char
     */
    private char calcRank(String card) {
        return card.charAt(0);
    }

    /**
     * Return the card's value.  Aces are always 1, Jacks are 11, Queens are 12, and Kings are 13.
     *
     * @param card a two-letter representation of the card's rank and suit
     * @return the card's numeric rank value
     */
    private int calcValue(String card) {
        return "A23456789TJQK".indexOf(calcRank(card)) + 1;
    }

    /**
     * Return a mapping from deck index to card numeric value.
     *
     * @param cards the 52 cards in the deck
     * @return an array containing each card's value
     */
    private int[] calcCardValues(String[] cards) {
        int[] values = new int[52];
        for (int i = 0; i < cards.length; i++) {
            values[i] = calcValue(cards[i]);
        }
        return values;
    }

    /**
     * For each card value, build a bit mask with the bits set for the positions of the four cards in the deck with
     * that value.  So index 1 would be a mask with the bits for each Ace's position set to 1.
     *
     * @param values the value of each card in the deck
     * @return a bit mask for each card value indicating which cards in the deck have that value
     */
    private long[] calcCardRankMasks(int[] values) {
        long[] cardBucketMasks = new long[14];
        for (int i = 0; i < values.length; i++) {
            cardBucketMasks[values[i]] |= State.mask(i);
        }
        return cardBucketMasks;
    }

    /**
     * For the given Pyramid flags, return whether or not it's a goal state (all 28 pyramid cards removed).
     *
     * @param pyramidFlags 28-bit flags (as a long) for the 28 pyramid cards
     * @return true if the 28 pyramid cards are removed, false otherwise
     */
    private boolean calcIsPyramidClear(long pyramidFlags) {
        return pyramidFlags == 0L;
    }

    /**
     * Given all the cards existing in a Pyramid flags value, return an estimate of how many steps to clear the
     * pyramid.  The estimate is the number of kings remaining in the pyramid, plus the maximum count of the number
     * of cards in each matching pair.  For example if there are two fours and three nines in the pyramid, it would
     * take at least three steps to remove all the fours and nines.  Count this for each pair of ranks that add to 13.
     *
     * @param existingPyramidIndexes all the pyramid card indexes that exist in a Pyramid flags value
     * @return a heuristic estimate of how many steps to remove the remaining pyramid cards
     */
    private int calcHeuristicCost(int[] existingPyramidIndexes) {
        int[] buckets = new int[14];
        for (int pyramidIndex : existingPyramidIndexes) {
            buckets[cardValue(pyramidIndex)]++;
        }
        int hCost = buckets[13];
        for (int i = 1; i <= 6; i++) {
            hCost += Math.max(buckets[i], buckets[13 - i]);
        }
        return hCost;
    }

    /**
     * Given all the cards existing in a Pyramid flags value, return masks to check if any card in the pyramid is
     * unremovable.  This function locates the matching cards for each card in the pyramid and makes a mask singling
     * them out, then filters out the ones that are covering or covered by it.
     * <p>
     * Performing a logical bitwise AND on these masks with a given state, if the result is zero that means there
     * is a card in the pyramid that can't be removed.
     *
     * @param existingPyramidIndexes all the pyramid card indexes that exist in a Pyramid flags value
     * @return a list of masks to check if there's a card in the pyramid that can't be removed
     */
    private long[] calcUnwinnableMasks(int[] existingPyramidIndexes) {
        TLongList masks = new TLongArrayList();
        for (int pyramidIndex : existingPyramidIndexes) {
            int cardValue = cardValue(pyramidIndex);
            if (cardValue != 13) {
                long mask = cardRankMask(13 - cardValue);
                mask &= Pyramid.UNRELATED_CARD_MASKS[pyramidIndex];
                if (!masks.contains(mask)) {
                    masks.add(mask);
                }
            }
        }
        return masks.toArray();
    }

    /**
     * Return card removal masks that involve the card at deckIndex.
     * If that card is a king, it just returns just the mask to remove it.
     * Otherwise, it returns all masks for that card plus a matching card in otherIndexes, starting from the
     * one at firstOtherIndexes.  firstOtherIndexes is just an optimization so that you can only look at a subset
     * of otherIndexes without creating a new array.
     *
     * @param deckIndex       a card deck index from 0 - 51 that you want the masks for removing it
     * @param otherIndexes    other card deck indexes of uncovered cards to possibly match with the one at deckIndex
     * @param firstOtherIndex an index to the first index in otherIndexes that you want to use for checking
     * @return a list of masks that can remove the cards involving the one at deckIndex
     */
    private TLongList calcRemovalMasks(int deckIndex, int[] otherIndexes, int firstOtherIndex) {
        TLongList masks = new TLongArrayList();
        if (isKing(deckIndex)) {
            masks.add(State.removalMask(deckIndex));
        } else {
            for (int i = firstOtherIndex; i < otherIndexes.length; i++) {
                if (cardsMatch(deckIndex, otherIndexes[i])) {
                    masks.add(State.removalMask(deckIndex) & State.removalMask(otherIndexes[i]));
                }
            }
        }
        return masks;
    }

    /**
     * Return all the card removal masks removing any cards referred to by the deck indexes in uncoveredIndexes
     *
     * @param uncoveredIndexes the uncovered cards in the pyramid
     * @return card removal masks for any cards that can be removed from the pyramid
     */
    private TLongList calcPyramidRemovalMasks(int[] uncoveredIndexes) {
        TLongList allMasks = new TLongArrayList();
        for (int i = 0; i < uncoveredIndexes.length; i++) {
            allMasks.addAll(calcRemovalMasks(uncoveredIndexes[i], uncoveredIndexes, i + 1));
        }
        return allMasks;
    }

    /**
     * Return an array of card removal mask lists indexed by stock index, and involving stock index with
     * uncoveredIndexes.  When the index isn't a valid non-empty stock index (28-51), set the value to an empty list.
     *
     * @param uncoveredIndexes the uncovered cards in the pyramid
     * @return card removal masks for each stock index used with the uncovered indexes
     */
    private TLongList[] calcStockRemovalMasks(int[] uncoveredIndexes) {
        TLongList[] stockMasks = new TLongList[53];
        for (int i = 0; i < stockMasks.length; i++) {
            if ((i >= 28) && (i <= 51)) {
                stockMasks[i] = calcRemovalMasks(i, uncoveredIndexes, 0);
            } else {
                stockMasks[i] = new TLongArrayList();
            }
        }
        return stockMasks;
    }

    /**
     * Given all the uncovered cards in a Pyramid flags value, return all possible card removal masks for them.
     * For every possible combination of stock index and waste index (cards 28-51 in the deck), generate a list of
     * card removal masks using these indexes plus the uncoveredIndexes.  These lists will be put into an array of
     * arrays indexed by stock index first (28-52), then by waste index (27-51).  result[stockIndex][wasteIndex] gives
     * you the list.  This makes finding successor states
     *
     * @param uncoveredIndexes the uncovered indexes for a Pyramid flags value
     * @return an array indexed by stock index and waste index to a list of card removal masks
     */
    private long[][][] calcSuccessorMasks(int[] uncoveredIndexes) {
        long[][][] successorMasks = new long[53][52][];
        TLongList pyramidMasks = calcPyramidRemovalMasks(uncoveredIndexes);
        TLongList[] stockMasks = calcStockRemovalMasks(uncoveredIndexes);
        for (int stockIndex = 28; stockIndex < 53; stockIndex++) {
            for (int wasteIndex = 27; wasteIndex < stockIndex; wasteIndex++) {
                TLongList masks = new TLongArrayList();
                if (!State.isStockEmpty(stockIndex) &&
                        !State.isWasteEmpty(wasteIndex) &&
                        cardsMatch(stockIndex, wasteIndex)) {
                    masks.add(State.removalMask(stockIndex) & State.removalMask(wasteIndex));
                }
                masks.addAll(stockMasks[wasteIndex]);
                masks.addAll(stockMasks[stockIndex]);
                masks.addAll(pyramidMasks);
                successorMasks[stockIndex][wasteIndex] = masks.toArray();
            }
        }
        return successorMasks;
    }

    /**
     * For all 1430 possible values of Pyramid flags, precalculate and cache everything the search algorithm needs.
     *
     * @return a map from Pyramid flags to precalculated StateCaches
     */
    private TLongObjectMap<StateCache> calcStateCaches() {
        TLongObjectMap<StateCache> stateCaches = new TLongObjectHashMap<>();
        for (Pyramid pyramid : Pyramid.ALL) {
            boolean isPyramidClear = calcIsPyramidClear(pyramid.getFlags());
            int heuristicCost = calcHeuristicCost(pyramid.getAllIndexes());
            long[] unwinnableMasks = calcUnwinnableMasks(pyramid.getAllIndexes());
            long[][][] successorMasks = calcSuccessorMasks(pyramid.getUncoveredIndexes());
            StateCache stateCache = new StateCache(isPyramidClear, heuristicCost, unwinnableMasks, successorMasks);
            stateCaches.put(pyramid.getFlags(), stateCache);
        }
        return stateCaches;
    }


}
