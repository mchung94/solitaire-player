package com.secondthorn.solitaireplayer.solvers.tripeaks;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardChallengeSolverTest {
    @Test
    void gameTest() {
        Deck deck = new Deck(
                "6d 5h Ah Jd 4s Ks 6s 8c 2h 4d 9s Kd 6c Ad 8s Ac 5c 9d 7h 3h 8d 5s 4c Qc Jh Kc " +
                "Kh 3c 3s 9c As 5d Qh Ts 4h 7s Td 9h Th 7c 8h 2c 7d Tc 2d 6h 2s Js Qd 3d Qs Jc"
        );
        BoardChallengeSolver solver = new BoardChallengeSolver();
        List<Solution> solutions = solver.solve(deck, State.INITIAL_STATE);
        assertEquals(1, solutions.size());
        Solution solution = solutions.get(0);
        assertTrue(solution.isDefinitiveSolution());
        assertEquals("Clear the board in 42 steps", solution.getDescription());
        assertEquals(42, solution.getActions().size());
    }

    @Test
    void unsolvableDeckHasEmptySolution() {
        Deck deck = new Deck(
                "9s 3s Kc Qd Jc 7h Ah 5h Ad Jd 3c Ks 5c 9h 9d Qh 6d 7c 4h Qc 8d 5s Js As 4d 8s" +
                " 8c 8h 4s Td Qs 6c 2c Kh 6s 3d 2h 4c 3h Jh Kd 2s 6h 7s Tc 9c 2d 7d Ts 5d Ac Th"
        );
        BoardChallengeSolver solver = new BoardChallengeSolver();
        List<Solution> solutions = solver.solve(deck, State.INITIAL_STATE);
        assertEquals(1, solutions.size());
        Solution solution = solutions.get(0);
        assertTrue(solution.isDefinitiveSolution());
        assertEquals("Lose Quickly: Impossible to clear the board", solution.getDescription());
        assertEquals(23, solution.getActions().size());
    }
}
