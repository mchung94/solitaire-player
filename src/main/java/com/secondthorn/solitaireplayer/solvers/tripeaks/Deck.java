package com.secondthorn.solitaireplayer.solvers.tripeaks;

import java.util.Arrays;
import java.util.List;

/**
 * A Deck in TriPeaks is a list of 52 cards.  It's a standard 52 card deck, where each card is a two letter string
 * consisting of a rank (A23456789TJQK) followed by a suit (cdhs).  Some cards may be unknown, they are represented by
 * the string "??".
 * <p>
 * The first 28 cards of the deck are arranged on the board or tableau in the TriPeaks formation.  The 29th card starts
 * off as the top card of the waste pile, and the 30th card onward is the stock pile with the 30th card at the top.
 */
public class Deck {
    private String[] cards;
    private int[] values;

    /**
     * Creates a Deck from an array of 52 cards.
     * @param cards an array of 52 cards, some of which may be unknown, represented by "??"
     */
    public Deck(String[] cards) {
        if (cards.length != 52) {
            throw new IllegalArgumentException("A Deck must be 52 cards, " + cards.length + " sent in instead.");
        }
        this.cards = cards;
        this.values = Arrays.stream(cards).mapToInt(Deck::cardValue).toArray();
    }

    /**
     * Creates a Deck from a List of 52 cards.
     * @param cards a list of 52 cards
     */
    public Deck(List<String> cards) {
        this(cards.toArray(new String[0]));
    }

    /**
     * Creates a Deck from a string containing 52 cards, separated by whitespace.
     * @param cards a string containing 52 cards
     */
    public Deck(String cards) {
        this(cards.trim().split("\\s+"));
    }

    /**
     * Returns the card at the given deck index.
     * @param index an integer from 0 to 51
     * @return the card at the location in the deck
     */
    public String cardAt(int index) {
        return cards[index];
    }

    /**
     * Returns true if the card at deck index2 is one rank above or below the card at deck index1.
     * Ranks wrap around, so A is one above K and K is one below A.
     * @param index1 a deck index pointing to a card in the deck
     * @param index2 a deck index pointing to another card in the deck
     * @return true if the two cards are one rank apart (wrapping around for K and A)
     */
    public boolean isOneAboveOrBelow(int index1, int index2) {
        int value1 = values[index1];
        int value2 = values[index2];
        if (value1 == 13 || value2 == 13) {
            return false;
        }
        int plusOneValue = (value2 + 1) % 13;
        int minusOneValue = value2 == 0 ? 12 : (value2 - 1);
        return value1 == plusOneValue || value1 == minusOneValue;
    }

    /**
     * Returns true if the deck has some unknown cards.
     * @return true if any card is unknown, represented by "??"
     */
    public boolean hasUnknownCards() {
        return Arrays.stream(cards).anyMatch(c -> c.equals("??"));
    }

    /**
     * Returns a card's rank, one of A23456789TJQK.
     */
    private static char cardRank(String card) {
        return card.charAt(0);
    }

    /**
     * Returns a numeric value for the card rank, Ace=0, ..., King=12, Unknown=13.
     */
    private static int cardValue(String card) {
        return "A23456789TJQK?".indexOf(cardRank(card));
    }
}
