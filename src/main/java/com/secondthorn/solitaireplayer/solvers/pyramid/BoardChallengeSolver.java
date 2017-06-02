package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * A Pyramid Solitaire Board Challenge solver.
 * <p>
 * Board challenges take the form: "Clear N board(s) in M deal(s)", e.g. "Clear 3 board(s) in 2 deal(s)".
 * This solver figures out how to clear boards or skip them in the fewest number of Actions possible.
 * Boards are cleared by removing all 28 cards on the table, the deck and waste piles don't matter.
 * <p>
 * If it's possible to clear the board, it will determine how to do so in the minimum number of steps.
 * <p>
 * If it's impossible to clear the board, it will determine how to reach a dead end with no more possible
 * moves in the minimum number of steps.  Most likely, it will involve drawing and recycling cards over and over
 * until the player can't do it anymore.  There's no attempt to maximize score.
 */
public class BoardChallengeSolver implements PyramidSolver {

    public List<List<Action>> solve(Deck deck) {
        List<List<Action>> solutions = new ArrayList<>();
        BucketQueue<NodeWithDepth> fringe = new BucketQueue<>(100);
        TLongIntMap seenStates = new TLongIntHashMap();
        long state = State.INITIAL_STATE;
        NodeWithDepth node = new NodeWithDepth(state, null, 0);
        if (!State.isUnwinnable(state, deck)) {
            fringe.add(node, State.hCost(state, deck));
        }
        while (fringe.size() != 0) {
            node = fringe.remove();
            state = node.getState();
            if (State.isTableClear(state)) {
                solutions.add(node.actions(deck));
                return solutions;
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
