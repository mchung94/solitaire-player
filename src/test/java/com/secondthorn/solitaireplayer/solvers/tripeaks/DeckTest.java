package com.secondthorn.solitaireplayer.solvers.tripeaks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeckTest {
    private static List<String> orderedCards = "cdhs".chars()
            .mapToObj(s -> "A23456789TJQK".chars().mapToObj(r -> String.format("%c%c", r, s)))
            .flatMap(s -> s)
            .collect(Collectors.toList());
    private static Deck orderedDeck = new Deck(orderedCards);
    private static Deck startingOrderedDeck = new Deck(
            "      ??          ??          ??\n" +
            "    ??  ??      ??  ??      ??  ??\n" +
            "  ??  ??  ??  ??  ??  ??  ??  ??  ??\n" +
            "6d  7d  8d  9d  Td  Jd  Qd  Kd  Ah  2h\n" +
            "3h 4h 5h 6h 7h 8h 9h Th Jh Qh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks"
    );

    /**
     * Asserts that the deck contains the same cards in the same order as expectedCards.
     */
    private void assertDeckCardsMatch(List<String> expectedCards, Deck deck) {
        for (int i = 0; i < 52; i++) {
            assertEquals(expectedCards.get(i), deck.cardAt(i));
        }
    }

    @Test
    void constructorWithListOfCards() {
        assertDeckCardsMatch(orderedCards, new Deck(orderedCards));
    }

    @Test
    void constructorWithArrayOfCards() {
        assertDeckCardsMatch(orderedCards, new Deck(orderedCards.toArray(new String[0])));
    }

    @Test
    void constructorWithStringOfCards() {
        assertDeckCardsMatch(orderedCards, new Deck(orderedCards.stream().collect(Collectors.joining(" "))));
    }

    @Test
    void constructorWithTooFewCardsThrowsException() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            List<String> cards = orderedCards.subList(0, 51);
            new Deck(cards);
        });
        assertEquals("A Deck must be 52 cards, 51 sent in instead.", t.getMessage());
    }

    @Test
    void constructorWithTooManyCardsThrowsException() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            List<String> cards = new ArrayList<>(orderedCards);
            cards.add("Kc");
            new Deck(cards);
        });
        assertEquals("A Deck must be 52 cards, 53 sent in instead.", t.getMessage());
    }

    /**
     * Returns true if card2 is one rank below or above card1.
     */
    private boolean isOneAboveOrBelow(String card1, String card2) {
        char rank1 = card1.charAt(0);
        char rank2 = card2.charAt(0);
        switch (rank1) {
            case 'A': return rank2 == 'K' || rank2 == '2';
            case '2': return rank2 == 'A' || rank2 == '3';
            case '3': return rank2 == '2' || rank2 == '4';
            case '4': return rank2 == '3' || rank2 == '5';
            case '5': return rank2 == '4' || rank2 == '6';
            case '6': return rank2 == '5' || rank2 == '7';
            case '7': return rank2 == '6' || rank2 == '8';
            case '8': return rank2 == '7' || rank2 == '9';
            case '9': return rank2 == '8' || rank2 == 'T';
            case 'T': return rank2 == '9' || rank2 == 'J';
            case 'J': return rank2 == 'T' || rank2 == 'Q';
            case 'Q': return rank2 == 'J' || rank2 == 'K';
            case 'K': return rank2 == 'Q' || rank2 == 'A';
            default: return false;
        }
    }

    @Test
    void oneAboveOrBelow() {
        for (int i=0; i<orderedCards.size(); i++) {
            String card1 = orderedCards.get(i);
            for (int j=0; j<orderedCards.size(); j++) {
                String card2 = orderedCards.get(j);
                assertEquals(isOneAboveOrBelow(card1, card2),
                        orderedDeck.isOneAboveOrBelow(i, j),
                        String.format("%s (index %d) vs %s (index %d)", card1, i, card2, j));
            }
        }
    }

    @Test
    void unknownCardsAreNeverOneAboveOrBelow() {
        ArrayList<String> cards = new ArrayList<>(orderedCards);
        cards.set(0, "??");
        Deck deck = new Deck(cards);
        for (int i=0; i<orderedCards.size(); i++) {
            assertFalse(deck.isOneAboveOrBelow(0, i));
            assertFalse(deck.isOneAboveOrBelow(i, 0));
        }
    }

    @Test
    void unknownCardIndexes() {
        assertFalse(orderedDeck.hasUnknownCards());
        assertTrue(startingOrderedDeck.hasUnknownCards());
    }

    @Test
    void tableauRankMask() {
        assertEquals(0b0100000000000010000000000001, orderedDeck.tableauRankMask('A'));
        assertEquals(0b0010000000000001000000000000, orderedDeck.tableauRankMask('K'));
    }
}
