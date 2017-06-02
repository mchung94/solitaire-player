package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.List;
import java.util.Map;

/**
 * Pyramid Solitaire solver interface to be implemented by all solving strategies.
 */
public interface PyramidSolver {
    /**
     * Given a standard 52-card deck, find out a solution or solutions using a specific strategy.
     *
     * @param deck a standard deck of 52 cards
     * @return a list of solutions, each of which are a list of Actions
     */
    Map<String, List<Action>> solve(Deck deck);
}
