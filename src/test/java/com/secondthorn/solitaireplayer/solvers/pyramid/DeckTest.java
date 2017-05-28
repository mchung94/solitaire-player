package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeckTest {
    public static List<String> orderedCards;

    static {
        orderedCards = new ArrayList<>(52);
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                orderedCards.add("" + rank + suit);
            }
        }
    }

    private void assertDeckCardsMatch(List<String> cards, Deck deck) {
        for (int i=0; i<52; i++) {
            assertEquals(orderedCards.get(i), deck.cardAt(i));
        }
    }

    @Test
    public void constructorWithListOfCards() {
        assertDeckCardsMatch(orderedCards, new Deck(orderedCards));
    }

    @Test
    public void constructorWithStringOfCards() {
        String stringOfCards = "";
        for (String card : orderedCards) {
            stringOfCards += card + " ";
        }
        assertDeckCardsMatch(orderedCards, new Deck(stringOfCards));
    }

    @Test
    public void constructorWithArrayOfCards() {
        String[] cards = orderedCards.toArray(new String[52]);
        assertDeckCardsMatch(orderedCards, new Deck(cards));
    }

    private char cardRank(String card) {
        return card.charAt(0);
    }

    @Test
    public void kingsFoundOnOrderedDeck() {
        Deck orderedDeck = new Deck(orderedCards);
        for (int i=0; i<52; i++) {
            char rank = cardRank(orderedDeck.cardAt(i));
            assertEquals(rank == 'K', orderedDeck.isKing(i));
        }
    }

    private boolean cardRanksAddTo13(String card1, String card2) {
        switch (cardRank(card1)) {
            case 'A': return cardRank(card2) == 'Q';
            case '2': return cardRank(card2) == 'J';
            case '3': return cardRank(card2) == 'T';
            case '4': return cardRank(card2) == '9';
            case '5': return cardRank(card2) == '8';
            case '6': return cardRank(card2) == '7';
            case '7': return cardRank(card2) == '6';
            case '8': return cardRank(card2) == '5';
            case '9': return cardRank(card2) == '4';
            case 'T': return cardRank(card2) == '3';
            case 'J': return cardRank(card2) == '2';
            case 'Q': return cardRank(card2) == 'A';
            default: return false;
        }
    }

    @Test
    public void cardsMatchOnOrderedDeck() {
        Deck orderedDeck = new Deck(orderedCards);
        for (int i=0; i<52; i++) {
            String card1 = orderedDeck.cardAt(i);
            boolean[] matches = orderedDeck.getMatchesForCard(i);
            for (int j=0; j<52; j++) {
                String card2 = orderedDeck.cardAt(j);
                assertEquals(cardRanksAddTo13(card1, card2), matches[j]);
            }
        }
    }
}
