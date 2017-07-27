package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Pyramid Solitaire Board Challenge solver.
 * <p>
 * Board challenges take the form: "Clear N board(s) in M deal(s)", e.g. "Clear 3 board(s) in 2 deal(s)".
 * This solver figures out how to clear boards in the fewest number of actions possible.
 * Boards are cleared by removing all 28 cards on the table, the deck and waste piles don't matter.
 * <p>
 * If it's possible to clear the board, it will determine how to do so in the minimum number of steps.
 * If it's impossible to clear the board, it will return no solution.  There is no attempt to maximize score.
 */
public class BoardChallengeSolver implements PyramidSolver {

    /**
     * Find the way to clear the 28 table cards in the fewest number of steps possible.
     * <p>
     * This uses the A* algorithm and a simple unwinnable state detection process.  Compared to
     * Breadth-First Search, this is faster at finding a solution if one exists, but slower to
     * return no solution when it's impossible to clear.
     *
     * @param deck a standard deck of 52 cards
     * @return a solution if one exists
     */
    public Map<String, List<Action>> solve(Deck deck) {
        Map<String, List<Action>> solutions = new HashMap<>();
        BucketQueue<NodeWithDepth> fringe = new BucketQueue<>(100);
        TLongIntMap seenStates = new TLongIntHashMap();
        long state = State.INITIAL_STATE;
        NodeWithDepth node = new NodeWithDepth(state, null, 0);
        if (!State.isUnwinnable(state, deck)) {
            fringe.add(node, State.hCost(state, deck));
        }
        while (!fringe.isEmpty()) {
            node = fringe.remove();
            state = node.getState();
            if (State.isTableClear(state)) {
                List<Action> solution = node.actions(deck);
                solutions.put("Clear the board in " + solution.size() + " steps.", solution);
                break;
            }
            int nextDepth = node.getDepth() + 1;
            TLongList successors = State.successors(state, deck);
            for (int i = 0, len = successors.size(); i < len; i++) {
                long nextState = successors.get(i);
                int seenDepth = seenStates.get(nextState);
                if ((seenDepth == seenStates.getNoEntryValue()) || (nextDepth < seenDepth)) {
                    seenStates.put(nextState, nextDepth);
                    if (!State.isUnwinnable(nextState, deck)) {
                        NodeWithDepth newNode = new NodeWithDepth(nextState, node, nextDepth);
                        fringe.add(newNode, nextDepth + State.hCost(nextState, deck));
                    }
                }
            }
        }
        return solutions;
    }

}
