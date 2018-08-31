package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardChallengeSolverTest {

    @Test
    public void constructorAlreadyPastGoal() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            new CardChallengeSolver(3, 'A', 4);
        });
        assertEquals("The current number of cards cleared must be smaller than the goal", t.getMessage());
    }

    @Test
    public void twoSolutionTest() {
        CardChallengeSolver solver = new CardChallengeSolver(4, '4', 0);
        String cards = "Tc Ac Js 5d 2h 3h As Th Qd 7h 3c Td 8s Kh 6d Ks 5c 6h 9h 3d 5h Jh Kc 8d Jd 8c " +
                       "7d 7c 2d Qs 9s 2c 3s 7s Ah Ad 4h 6s 6c 4c 2s 4d Qh 9d Jc 4s Qc Ts Kd 5s 9c 8h";
        Deck deck = new Deck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        assertEquals(2, solutions.size());
    }

    @Test
    public void goalReachedEarly() {
        CardChallengeSolver solver = new CardChallengeSolver(3, 'A', 2);
        String cards = "Kd Kc Qh Ah 7d 6d 8d 5d 9d 4d Td 3d Jd 2d Qd Ad 7c 6c 8c 5c 9c 4c Tc 3c Jc 2c " +
                       "Qc Ac 6h 7h 5h 8h 4h 9h 3h Th 2h Jh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks";
        Deck deck = new Deck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        assertEquals(1, solutions.size());
        assertEquals(1, solutions.values().iterator().next().size());
        assertTrue(solutions.containsKey("Reach goal, remove 1 card of rank A in 1 step."));
    }

    @Test
    public void deadEndWithoutClearingBoard() {
        CardChallengeSolver solver = new CardChallengeSolver(4, 'J', 0);
        String cards = "As 5d 4s 7h 7s Kh 7d Tc 5c Qh 2d Kc 9c 6h Th 6s 5h Ks Jc 6c 2c 4h 8h 8s 7c Ac " +
                       "Jh Js Kd Td 2s 9d 8c 6d Qc 9h 8d 3d 9s 2h 4d 3s 3c 3h Qd Ad Jd 5s 4c Ts Ah Qs";
        Deck deck = new Deck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        assertEquals(1, solutions.size());
        assertTrue(solutions.containsKey("Without clearing the board, remove 3 cards of rank J in 46 steps."));
    }

}
