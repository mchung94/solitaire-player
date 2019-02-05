package com.secondthorn.solitaireplayer.solvers.tripeaks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A Deck in TriPeaks is a list of 52 cards.  It's a standard 52 card deck, where each card is a two letter string
 * consisting of a rank (A23456789TJQK) followed by a suit (cdhs).  Some cards may be unknown, they are represented by
 * the string "??".
 * <p>
 * The first 28 cards of the deck are arranged on the board or tableau in the TriPeaks formation.  The 29th card starts
 * off as the top card of the waste pile, and the 30th card onward is the stock pile with the 30th card at the top.
 */
public class Deck {
    public static final String UNKNOWN_CARD = "??";

    private List<String> cards;
    private int[] values;

    /**
     * Creates a Deck from a List of 52 cards.
     *
     * @param cards a list of 52 cards, some may be unknown
     */
    public Deck(List<String> cards) {
        if (cards.size() != 52) {
            throw new IllegalArgumentException("A Deck must be 52 cards, " + cards.size() + " sent in instead.");
        }
        this.cards = cards;
        this.values = cards.stream().mapToInt(Deck::cardValue).toArray();
    }

    /**
     * Creates a Deck from an array of 52 cards.
     *
     * @param cards an array of 52 cards, some may be unknown
     */
    public Deck(String[] cards) {
        this(Arrays.asList(cards));
    }

    /**
     * Creates a Deck from a string containing 52 cards, separated by whitespace.
     *
     * @param cards a string containing 52 cards, some may be unknown
     */
    public Deck(String cards) {
        this(cards.trim().split("\\s+"));
    }

    /**
     * Returns the list of cards in the deck.
     *
     * @return the cards in the deck
     */
    public List<String> getCards() {
        return cards;
    }

    /**
     * Returns the card at the given deck index.
     *
     * @param index an integer from 0 to 51
     * @return the card at the location in the deck
     */
    public String cardAt(int index) {
        return cards.get(index);
    }

    /**
     * Returns the deck index of the card.
     *
     * @param card a card to find the deck index of
     * @return the deck index of the card in the deck
     */
    public int indexOf(String card) {
        return cards.indexOf(card);
    }

    /**
     * Returns true if the card at the deck index is unknown, represented by "??".
     *
     * @param index an integer from 0 to 51
     * @return true if the card at location in the deck is unknown
     */
    public boolean isUnknownCard(int index) {
        return cardAt(index).equals(UNKNOWN_CARD);
    }

    /**
     * Returns an array of deck indexes of all the unknown cards in the deck.
     * @return an array of deck indexes (0-51)
     */
    public boolean hasUnknownCards() {
        return IntStream.range(0, cards.size()).anyMatch(this::isUnknownCard);
    }

    /**
     * Returns true if the card at deck index2 is one rank above or below the card at deck index1.
     * Ranks wrap around, so A is one above K and K is one below A.
     *
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
