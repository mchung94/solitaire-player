package com.secondthorn.solitaireplayer.solvers.tripeaks;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardChallengeSolverTest {
    @Test
    void gameTest() {
        Deck deck = new Deck(
                "6d 5h Ah Jd 4s Ks 6s 8c 2h 4d 9s Kd 6c Ad 8s Ac 5c 9d 7h 3h 8d 5s 4c Qc Jh Kc " +
                "Kh 3c 3s 9c As 5d Qh Ts 4h 7s Td 9h Th 7c 8h 2c 7d Tc 2d 6h 2s Js Qd 3d Qs Jc"
        );
        BoardChallengeSolver solver = new BoardChallengeSolver();
        Map<String, List<String>> solutions = solver.solve(deck);
        assertEquals(1, solutions.size());
        assertEquals(42, solutions.values().iterator().next().size());
    }
}
