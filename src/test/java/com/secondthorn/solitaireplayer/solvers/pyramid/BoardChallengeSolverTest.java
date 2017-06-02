package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BoardChallengeSolverTest {
    @Test
    public void simpleTest() {
        BoardChallengeSolver solver = new BoardChallengeSolver();
        String cards = "3c 7s Jd 5s 8c Th 2s 3s Ah Ad 2d 4c 5d 9d 7h 9s Qc 6h Tc 3d Ks 8h 6c Ts As 6s " +
                       "Jh 8d 3h Js 9c 7d Qs 5c 5h Qd Ac Kd 4s 9h 6d 2h Qh Kc Td 8s 4h 4d Kh 7c Jc 2c";
        Deck deck = new Deck(cards);
        List<List<Action>> solutions = solver.solve(deck);
        assertEquals(1, solutions.size());
        List<Action> solution = solutions.get(0);
        assertEquals(40, solution.size());
    }

    @Test
    public void unwinnableTest() {
        BoardChallengeSolver solver = new BoardChallengeSolver();
        String cards = "2d 9s 7c 5d 2s Qc Jd 5c Jc Td 4s 6s 8c 8s Jh 5h As Js 6d 2c Qd Qh 4c 8h Ks 7d " +
                       "Ah 4d 9h 3d 5s 4h Th Ad 3s 8d Ts Tc 9d Kc 7h Kd 6h Qs 2h Ac 7s 6c 3c 3h 9c Kh";
        Deck deck = new Deck(cards);
        List<List<Action>> solutions = solver.solve(deck);
        assertEquals(0, solutions.size());
    }
}
