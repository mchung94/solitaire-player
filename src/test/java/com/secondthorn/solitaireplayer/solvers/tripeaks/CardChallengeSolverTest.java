package com.secondthorn.solitaireplayer.solvers.tripeaks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardChallengeSolverTest {
    @Test
    void testWithUnknownCards() {
        Deck deck = new Deck(
                "      ??          ??          ??\n" +
                "    ??  ??      ??  ??      ??  ??\n" +
                "  ??  ??  ??  ??  ??  ??  ??  ??  ??\n" +
                "9d  9h  2c  7d  Qd  Ks  Kd  5s  2d  Kh\n" +
                "3d\n" +
                "Ac Js 6c Td Kc Th Ad 7c Tc As Ts 6h 2h Qc 8c 6d 3s 9s Jc Qh 4h 5h 3h"
        );
        CardChallengeSolver solver = new CardChallengeSolver(10, 'K', 0);
        Solution solution = solver.solve(deck, State.INITIAL_STATE);
        assertEquals("Remove 3 cards of rank K in 27 steps", solution.getDescription());
        assertFalse(solution.isDefinitiveSolution());
    }

    @Test
    void testWithKnownDeck() {
        Deck deck = new Deck(
                "      Qs          4c          2s\n" +
                "    4d  Jh      9c  Ah      5d  8d\n" +
                "  8h  7s  4s  7h  3c  Jd  8s  5c  6s\n" +
                "9d  9h  2c  7d  Qd  Ks  Kd  5s  2d  Kh\n" +
                "3d\n" +
                "Ac Js 6c Td Kc Th Ad 7c Tc As Ts 6h 2h Qc 8c 6d 3s 9s Jc Qh 4h 5h 3h"
        );
        CardChallengeSolver solver = new CardChallengeSolver(10, 'K', 0);
        Solution solution = solver.solve(deck, State.INITIAL_STATE);
        assertEquals("Remove 3 cards of rank K in 45 steps", solution.getDescription());
        assertTrue(solution.isDefinitiveSolution());
    }

    @Test
    void badArguments() {
        assertThrows(IllegalArgumentException.class, () -> new CardChallengeSolver(10, 'K', 12));;
    }
}
