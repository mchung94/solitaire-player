package com.secondthorn.solitaireplayer.solvers.tripeaks;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.secondthorn.solitaireplayer.solvers.tripeaks.Action.Command.DRAW;
import static com.secondthorn.solitaireplayer.solvers.tripeaks.Action.Command.REMOVE;
import static com.secondthorn.solitaireplayer.solvers.tripeaks.Action.Command.UNDO_BOARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardRevealingSolverTest {
    @Test
    void solutionRevealsUnknownCard() {
        Deck deck = new Deck(
                "      ??          ??          ??\n" +
                "    ??  ??      ??  ??      ??  ??\n" +
                "  ??  ??  ??  ??  ??  ??  ??  ??  ??\n" +
                "7h  3d  7d  Ah  9h  5c  Td  Ac  2h  7c\n" +
                "8h\n" +
                "As 9d Ad 4s Jc 2d 7s Jh 4d Kd 9c 8d 5s 2c 4h Qd Ts 5d 4c Ks 6c Tc 3s"
        );
        CardRevealingSolver solver = new CardRevealingSolver();
        Solution solution = solver.solve(deck, State.INITIAL_STATE);
        assertEquals("Reveal the tableau cards at index(es): [16]", solution.getDescription());
        assertTrue(solution.isDefinitiveSolution());
        assertEquals(3, solution.getActions().size());
        assertTrue(new PlayTester(deck).areActionsPlayable(solution));
    }

    @Test
    void solutionIsEmptyWhenAllCardsAreKnown() {
        Deck deck = new Deck(
                "      Ac          2c          3c\n" +
                "    4c  5c      6c  7c      8c  9c\n" +
                "  Tc  Jc  Qc  Kc  Ad  2d  3d  4d  5d\n" +
                "6d  7d  8d  9d  Td  Jd  Qd  Kd  Ah  2h\n" +
                "3h\n" +
                "4h 5h 6h 7h 8h 9h Th Jh Qh Kh As 2s 3s 4s 5s 6s 7s 8s 9s Ts Js Qs Ks"
        );
        CardRevealingSolver solver = new CardRevealingSolver();
        Solution solution = solver.solve(deck, State.INITIAL_STATE);
        assertEquals("All cards are known, can't turn over any face down cards", solution.getDescription());
        assertTrue(solution.isDefinitiveSolution());
        assertEquals(0, solution.getActions().size());
        assertTrue(new PlayTester(deck).areActionsPlayable(solution));
    }

    @Test
    void solutionRestartsFromBeginningWhenStuck() {
        Deck deck = new Deck(
                "      ??          ??          ??\n" +
                "    ??  ??      ??  ??      ??  ??\n" +
                "  ??  ??  ??  ??  ??  ??  ??  ??  ??\n" +
                "7h  3d  7d  Ah  9h  5c  Td  Ac  2h  7c\n" +
                "8h\n" +
                "As 9d Ad 4s Jc 2d 7s Jh 4d Kd 9c 8d 5s 2c 4h Qd Ts 5d 4c Ks 6c Tc 3s"
        );
        CardRevealingSolver solver = new CardRevealingSolver();
        Solution solution = solver.solve(deck, State.create(20313, 51, 52));
        assertEquals("Reveal the tableau cards at index(es): [16]", solution.getDescription());
        assertTrue(solution.isDefinitiveSolution());
        assertEquals(State.create(19333, 25, 30), solution.getEndingState());
        List<Action> actions = solution.getActions();
        assertEquals(4, actions.size());
        assertTrue(new PlayTester(deck).areActionsPlayable(solution));
    }
}
