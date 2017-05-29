package com.secondthorn.solitaireplayer.solvers.pyramid;

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
    private boolean[][] matchFlags;

    /**
     * Create a new deck out of a String array of cards.
     * @param cards an array of 52 cards
     */
    public Deck(String[] cards) {
        this.cards = cards;
        this.kingFlags = getKingFlags(cards);
        this.matchFlags = getMatches(cards);
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
