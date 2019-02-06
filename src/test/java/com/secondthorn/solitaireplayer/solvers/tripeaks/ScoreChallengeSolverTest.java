package com.secondthorn.solitaireplayer.solvers.tripeaks;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScoreChallengeSolverTest {
    private Deck orderedDeck = new Deck(
            "      Ac          2c          3c\n" +
            "    4c  5c      6c  7c      8c  9c\n" +
            "  Tc  Jc  Qc  Kc  Ad  2d  3d  4d  5d\n" +
            "6d  7d  8d  9d  Td  Jd  Qd  Kd  Ah  2h\n" +
            "3h\n" +
            "4h 5h 6h 7h 8h 9h Th Jh Qh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks\n"
    );

    private Deck deckWithUnknownCards = new Deck(
            "      ??          ??          ??\n" +
            "    ??  ??      ??  ??      ??  ??\n" +
            "  ??  ??  ??  ??  ??  ??  ??  ??  ??\n" +
            "6d  7d  8d  9d  Td  Jd  Qd  Kd  Ah  2h\n" +
            "3h\n" +
            "4h 5h 6h 7h 8h 9h Th Jh Qh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks\n"
    );

    @Test
    void maxScoreIsReached() {
        ScoreChallengeSolver solver = new ScoreChallengeSolver(ScoreChallengeSolver.MAX_POSSIBLE_SCORE, 0);
        List<Solution> solutions = solver.solve(orderedDeck, State.INITIAL_STATE);
        assertEquals(1, solutions.size());
        Solution solution = solutions.get(0);
        assertTrue(solution.isDefinitiveSolution());
        assertEquals(28, solution.getActions().size());
        assertEquals("Gain 84900 points in 28 steps", solution.getDescription());
    }

    @Test
    void smallScoreSolutionIsCorrect() {
        ScoreChallengeSolver solver = new ScoreChallengeSolver(1400, 0);
        List<Solution> solutions = solver.solve(orderedDeck, State.INITIAL_STATE);
        assertEquals(1, solutions.size());
        Solution solution = solutions.get(0);
        assertTrue(solution.isDefinitiveSolution());
        assertEquals(4, solution.getActions().size());
        assertEquals("Gain 1600 points in 4 steps", solution.getDescription());
    }

    @Test
    void deckWithUnknownCards() {
        ScoreChallengeSolver solver = new ScoreChallengeSolver(25000, 0);
        List<Solution> solutions = solver.solve(deckWithUnknownCards, State.INITIAL_STATE);
        assertEquals(1, solutions.size());
        Solution solution = solutions.get(0);
        assertFalse(solution.isDefinitiveSolution());
        assertEquals(10, solution.getActions().size());
        assertEquals("Gain 10000 points in 10 steps", solution.getDescription());
    }

    @Test
    void maxScoreWithoutClearingBoard() {
        Deck deck = new Deck(
                "      6d          5h          Ah\n" +
                "    Jd  4s      Ks  6s      8c  2h\n" +
                "  4d  9s  Kd  6c  Ad  8s  Ac  5c  9d\n" +
                "7h  3h  8d  5s  4c  Qc  Jh  Kc  Kh  3c\n" +
                "3s\n" +
                "9c As 5d Qh Ts 4h 7s Td 9h Th 7c 8h 2c 7d Tc 2d 6h 2s Js Qd 3d Qs Jc"
        );
        ScoreChallengeSolver solver = new ScoreChallengeSolver(ScoreChallengeSolver.MAX_POSSIBLE_SCORE, 0);
        List<Solution> solutions = solver.solve(deck, State.INITIAL_STATE);
        assertEquals(1, solutions.size());
        Solution solution = solutions.get(0);
        assertTrue(solution.isDefinitiveSolution());
        assertEquals(49, solution.getActions().size());
        assertEquals("Gain 22300 points in 49 steps", solution.getDescription());
    }

    @Test
    void badGoalThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new ScoreChallengeSolver(50000, 60000));
    }
}
