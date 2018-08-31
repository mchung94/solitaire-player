package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeckTest {
    static Deck deck;
    private static List<String> orderedCards;

    static {
        orderedCards = new ArrayList<>(52);
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                orderedCards.add("" + rank + suit);
            }
        }
        deck = new Deck(orderedCards);
    }

    private void assertDeckCardsMatch(List<String> cards, Deck deck) {
        for (int i = 0; i < 52; i++) {
            assertEquals(cards.get(i), deck.cardAt(i));
        }
    }

    @Test
    public void constructorWithListOfCards() {
        assertDeckCardsMatch(orderedCards, new Deck(orderedCards));
    }

    @Test
    public void constructorWithStringOfCards() {
        String stringOfCards = orderedCards.stream().collect(Collectors.joining(" "));
        assertDeckCardsMatch(orderedCards, new Deck(stringOfCards));
    }

    @Test
    public void constructorWithArrayOfCards() {
        String[] cards = orderedCards.toArray(new String[52]);
        assertDeckCardsMatch(orderedCards, new Deck(cards));
    }

    @Test
    public void constructorThrowsExceptionWithTooFewCards() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            List<String> cards = orderedCards.subList(0, 51);
            new Deck(cards);
        });
        assertEquals("A Deck must be 52 cards, 51 sent in instead.", t.getMessage());
    }

    @Test
    public void constructorThrowsExceptionWithTooManyCards() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            List<String> cards = new ArrayList<>(orderedCards);
            cards.add("Kc");
            new Deck(cards);
        });
        assertEquals("A Deck must be 52 cards, 53 sent in instead.", t.getMessage());
    }

    @Test
    public void cardAt() {
        for (int i = 0; i < 52; i++) {
            assertEquals(orderedCards.get(i), deck.cardAt(i));
        }
    }

    private char cardRank(String card) {
        return card.charAt(0);
    }

    private int cardValue(String card) {
        switch (cardRank(card)) {
            case 'A': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case 'T': return 10;
            case 'J': return 11;
            case 'Q': return 12;
            case 'K': return 13;
            default: throw new RuntimeException(card + " isn't a valid card for cardValue()");
        }
    }

    @Test
    public void cardValue() {
        Deck deck = new Deck(orderedCards);
        for (int i = 0; i < 52; i++) {
            assertEquals(cardValue(orderedCards.get(i)), deck.cardValue(i));
        }
    }

    @Test
    public void kingsFoundOnOrderedDeck() {
        Deck deck = new Deck(orderedCards);
        for (int i = 0; i < 52; i++) {
            char rank = cardRank(deck.cardAt(i));
            assertEquals(rank == 'K', deck.isKing(i));
        }
    }

    @Test
    public void cardRankMasks() {
        Deck deck = new Deck(orderedCards);
        long[] expectedMasks = {
                0b0000000000000000000000000000000000000000000000000000L,
                0b0000000000001000000000000100000000000010000000000001L,
                0b0000000000010000000000001000000000000100000000000010L,
                0b0000000000100000000000010000000000001000000000000100L,
                0b0000000001000000000000100000000000010000000000001000L,
                0b0000000010000000000001000000000000100000000000010000L,
                0b0000000100000000000010000000000001000000000000100000L,
                0b0000001000000000000100000000000010000000000001000000L,
                0b0000010000000000001000000000000100000000000010000000L,
                0b0000100000000000010000000000001000000000000100000000L,
                0b0001000000000000100000000000010000000000001000000000L,
                0b0010000000000001000000000000100000000000010000000000L,
                0b0100000000000010000000000001000000000000100000000000L,
                0b1000000000000100000000000010000000000001000000000000L
        };

        for (int i = 0; i < 13; i++) {
            long actualMask = deck.cardRankMask(i);
            assertEquals(expectedMasks[i], actualMask);
        }
    }

    private boolean cardsMatch(String card1, String card2) {
        return cardValue(card1) + cardValue(card2) == 13;
    }

    @Test
    public void cardsMatch() {
        Deck deck = new Deck(orderedCards);
        for (int i = 0; i < 52; i++) {
            String card1 = orderedCards.get(i);
            for (int j = 0; j < 52; j++) {
                String card2 = orderedCards.get(j);
                assertEquals(cardsMatch(card1, card2), deck.cardsMatch(i, j));
            }
        }
    }

    @Test
    public void getStateCache() {
        Deck deck = new Deck(orderedCards);
        for (Pyramid pyramid : Pyramid.ALL) {
            assertNotNull(deck.getStateCache(pyramid.getFlags()));
        }
    }

}
