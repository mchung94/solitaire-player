package com.secondthorn.solitaireplayer.solvers.pyramid;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BoardChallengeSolverTest {
    @Test
    public void simpleTest() {
        BoardChallengeSolver solver = new BoardChallengeSolver();
        String cards = "Th Js Jh 9c Qd 5c 2d 9h Td 4h Qs 9d 3s 8d Kh 6c 3h 6d 8c Kc Ah Qh Tc 9s Kd 8s " +
                       "4s 2c 4c Jc 7c Jd 8h 6s 5d 3c 4d 3d 6h Ts 5s Ks 7d Ac 7s 2s Qc 2h 5h As 7h Ad";
        Deck deck = new Deck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        assertEquals(1, solutions.size());
        List<Action> solution = solutions.values().iterator().next();
        assertEquals(44, solution.size());
    }

    @Test
    public void unwinnableTest() {
        BoardChallengeSolver solver = new BoardChallengeSolver();
        String cards = "2d 9s 7c 5d 2s Qc Jd 5c Jc Td 4s 6s 8c 8s Jh 5h As Js 6d 2c Qd Qh 4c 8h Ks 7d " +
                       "Ah 4d 9h 3d 5s 4h Th Ad 3s 8d Ts Tc 9d Kc 7h Kd 6h Qs 2h Ac 7s 6c 3c 3h 9c Kh";
        Deck deck = new Deck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        assertEquals(0, solutions.size());
    }
}
