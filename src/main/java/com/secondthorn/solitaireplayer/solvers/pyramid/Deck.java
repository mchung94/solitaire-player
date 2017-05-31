package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.Arrays;
import java.util.List;

/**
 * A Deck is a standard deck of 52 cards containing the suits c d h s, and the ranks A 2 3 4 5 6 7 8 9 T J Q K.
 * In Pyramid Solitaire, the first 28 cards will be the table cards and the remaining 24 will be the deck.
 * Card index 28 is the top of the deck at the start of the game and index 51 will be the bottom.
 * <p>
 * Cards are represented as two-letter strings containing rank and suit, for example Kc or 7s.
 * <p>
 * A Deck precalculates a lot of information about the cards so that anything the solver requires just turns into
 * an array index operation and nothing too complicated.  For example, which cards match with each other for removal
 * or which cards are kings.
 */
public class Deck {
    private String[] cards;
    private boolean[] kingFlags;
    private int[] rankBuckets;
    private boolean[][] matchFlags;
    private long[] unwinnableMasks;

    /**
     * Create a new deck out of a String array of cards.
     * @param cards an array of 52 cards
     */
    public Deck(String[] cards) {
        this.cards = cards;
        this.kingFlags = getKingFlags(this.cards);
        this.rankBuckets = getRankBuckets(this.cards);
        this.matchFlags = getMatches(this.cards);
        this.unwinnableMasks = getUnwinnableMasks(this.kingFlags, this.matchFlags);
    }

    /**
     * Create a new deck out of a single string containing all the cards.
     * @param cards a String containing 52 space-delimited cards
     */
    public Deck(String cards) {
        this(cards.trim().split("\\s+"));
    }

    /**
     * Create a new deck out of a List of cards.
     * @param cards a List containing 52 cards
     */
    public Deck(List<String> cards) {
        this(cards.toArray(new String[cards.size()]));
    }

    /**
     * Return a string representation of the card at the given deck index.
     * @param deckIndex the index into the deck of cards
     * @return the card at the deckIndex
     */
    public String cardAt(int deckIndex) {
        return cards[deckIndex];
    }

    /**
     * Return true if the card at the given deck index is a King.
     * @param deckIndex the index into the deck of cards
     * @return true if the card at the index is a King, false otherwise
     */
    public boolean isKing(int deckIndex) {
        return kingFlags[deckIndex];
    }

    /**
     * Return a numeric rank bucket value for a card for use when bucketing cards
     * by rank.  Kings are 0 but every other card is its normal numeric rank value.
     */
    public int cardRankBucket(int deckIndex) {
        return rankBuckets[deckIndex];
    }

    /**
     * Return a 52-element array of booleans indicating if the card at the index is a match with
     * the card at the given deck index (i.e. if the two cards have ranks that add up to 13).
     * <p>
     * The usage of this is to find out for a single card, which out of a list of other cards match with it.
     * So for performance, instead of being a 2D lookup, it can return a whole 52-element boolean array for
     * the card so it can just do single lookups to find each other card that matches with it.
     * @param deckIndex the index into the deck of cards
     * @return a 52-element boolean array indicating if the card matches with the other card
     */
    public boolean[] getMatchesForCard(int deckIndex) {
        return matchFlags[deckIndex];
    }

    /**
     * Given a table index, return a mask which can be used to tell if a state is unwinnable.
     * The mask has bits set for the table card plus any card that can be removed with it as a pair
     * whose ranks add up to 13.  So if the table card hasn't been removed yet, but the cards that
     * can remove it are gone, then the state is unwinnable.
     * @param tableIndex 0-27 for the Pyramid table card index
     * @return a bit mask that can check if the table card can't be removed
     */
    public long getUnwinnableMask(int tableIndex) {
        return unwinnableMasks[tableIndex];
    }

    /**
     * Return the card's rank, assuming the card is a two-letter string and the rank character is the first one.
     * @param card a two-letter representation of the card's rank and suit
     * @return the card's rank as a char
     */
    private char cardRank(String card) {
        return card.charAt(0);
    }

    /**
     * Build and return a boolean array indicating which cards in the list are kings.
     * @param cards an array of 52 cards
     * @return an array of booleans showing which cards are kings
     */
    private boolean[] getKingFlags(String[] cards) {
        boolean[] flags = new boolean[52];
        for (int i = 0; i < cards.length; i++) {
            flags[i] = cardRank(cards[i]) == 'K';
        }
        return flags;
    }

    /**
     * Return the numeric rank value of a card for the purposes of Pyramid Solitaire.
     * @param card a two-letter representation of the card's rank and suit
     * @return the card's numeric rank value
     */
    private int cardNumericRank(String card) {
        return " A23456789TJQK".indexOf(cardRank(card));
    }

    /**
     * Build and return a numeric value for the card to use when bucketing cards by rank.
     * This differs from the card's numeric rank because Kings are equal to 0 here,
     * in order to save space in the bucket array.
     * @param cards an array of 52 cards
     * @return a 52 element int array showing the array index to use for each card when bucketing by rank
     */
    private int[] getRankBuckets(String[] cards) {
        int[] buckets = new int[52];
        for (int i=0; i<cards.length; i++) {
            buckets[i] = cardNumericRank(cards[i]) % 13;
        }
        return buckets;
    }

    /**
     * Return true if the cards are a pair whose ranks add up to 13.
     * @param card1 the first card
     * @param card2 the second card
     * @return true if the card ranks add up to 13
     */
    private boolean cardRanksAddTo13(String card1, String card2) {
        return cardNumericRank(card1) + cardNumericRank(card2) == 13;
    }

    /**
     * Build and return an array of arrays.  For each card it will create a 52-element boolean array
     * showing which cards match with it or not.  This is so the solving algorithm can determine this
     * with just an array lookup and no expensive calculations.
     * @param cards an array of 52 cards
     * @return a 52-element boolean array, each containing another 52-element boolean array
     */
    private boolean[][] getMatches(String[] cards) {
        boolean[][] matches = new boolean[52][];
        for (int i=0; i<52; i++) {
            boolean[] matchesForCard = new boolean[52];
            matches[i] = matchesForCard;
            for (int j=0; j<52; j++) {
                matchesForCard[j] = cardRanksAddTo13(cards[i], cards[j]);
            }
        }
        return matches;
    }

    /**
     * For each of the 28 table indexes, list the other table indexes that aren't covering or covered by the index.
     * If table indexes are unrelated, then potentially the cards in them can be removed together as a pair
     * if their ranks add up to 13.  Otherwise, they can't because you have to remove one to uncover the
     * other and make it possible to remove.
     */
    private static final int[][] TABLE_UNRELATED_INDEXES = {
            {},
            {2, 5, 9, 14, 20, 27},
            {1, 3, 6, 10, 15, 21},
            {2, 4, 5, 8, 9, 13, 14, 19, 20, 26, 27},
            {3, 5, 6, 9, 10, 14, 15, 20, 21, 27},
            {1, 3, 4, 6, 7, 10, 11, 15, 16, 21, 22},
            {2, 4, 5, 7, 8, 9, 12, 13, 14, 18, 19, 20, 25, 26, 27},
            {5, 6, 8, 9, 10, 13, 14, 15, 19, 20, 21, 26, 27},
            {3, 6, 7, 9, 10, 11, 14, 15, 16, 20, 21, 22, 27},
            {1, 3, 4, 6, 7, 8, 10, 11, 12, 15, 16, 17, 21, 22, 23},
            {2, 4, 5, 7, 8, 9, 11, 12, 13, 14, 17, 18, 19, 20, 24, 25, 26, 27},
            {5, 8, 9, 10, 12, 13, 14, 15, 18, 19, 20, 21, 25, 26, 27},
            {6, 9, 10, 11, 13, 14, 15, 16, 19, 20, 21, 22, 26, 27},
            {3, 6, 7, 10, 11, 12, 14, 15, 16, 17, 20, 21, 22, 23, 27},
            {1, 3, 4, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18, 21, 22, 23, 24},
            {2, 4, 5, 7, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19, 20, 23, 24, 25, 26, 27},
            {5, 8, 9, 12, 13, 14, 15, 17, 18, 19, 20, 21, 24, 25, 26, 27},
            {9, 10, 13, 14, 15, 16, 18, 19, 20, 21, 22, 25, 26, 27},
            {6, 10, 11, 14, 15, 16, 17, 19, 20, 21, 22, 23, 26, 27},
            {3, 6, 7, 10, 11, 12, 15, 16, 17, 18, 20, 21, 22, 23, 24, 27},
            {1, 3, 4, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25},
            {2, 4, 5, 7, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 27},
            {5, 8, 9, 12, 13, 14, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27},
            {9, 13, 14, 15, 18, 19, 20, 21, 22, 24, 25, 26, 27},
            {10, 14, 15, 16, 19, 20, 21, 22, 23, 25, 26, 27},
            {6, 10, 11, 15, 16, 17, 20, 21, 22, 23, 24, 26, 27},
            {3, 6, 7, 10, 11, 12, 15, 16, 17, 18, 21, 22, 23, 24, 25, 27},
            {1, 3, 4, 6, 7, 8, 10, 11, 12, 13, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25, 26}
    };

    /**
     * For each of the 28 table indexes, build a 52-bit long bit mask with the bit for the table index
     * and the bits for the cards that can be removed with it as a pair.  To find out if a state is
     * unwinnable, use the mask to find out if the table card hasn't been removed, but all the cards
     * that can be removed with it are gone.
     * @param kingFlags flags to check which cards in the deck are kings
     * @param matchFlags flags to check which cards pair with others add up to 13
     * @return 28 bit masks that can check if each of the 28 table cards can't be removed
     */
    private long[] getUnwinnableMasks(boolean[] kingFlags, boolean[][] matchFlags) {
        long[] unwinnableMasks = new long[28];
        for(int i=0; i<28; i++) {
            long mask = 0;
            boolean[] matches = matchFlags[i];
            int[] unrelatedIndexes = TABLE_UNRELATED_INDEXES[i];
            if (!kingFlags[i]) {
                mask = 1L << i;
                for (int j=0; j<52; j++) {
                    if (((j > 27) || (Arrays.binarySearch(unrelatedIndexes, j) >= 0)) && matches[j]) {
                        mask |= 1L << j;
                    }
                }
            }
            unwinnableMasks[i] = mask;
        }
        return unwinnableMasks;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i=0; i<7; i++) {
            for (int j=0; j<12 - (i*2); j++) {
                sb.append(" ");
            }
            for (int j=0; j<i+1; j++) {
                sb.append(this.cardAt(count));
                if (j < i) {
                    sb.append("  ");
                }
                count++;
            }
            sb.append("\n");
        }
        while (count < 52) {
            sb.append(this.cardAt(count));
            count++;
            if (count < 52) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
