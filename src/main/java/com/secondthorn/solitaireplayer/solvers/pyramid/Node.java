package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.ArrayList;
import java.util.List;

/**
 * A Search Node for general search algorithms (Breadth first search, A*, etc).
 */
class Node {
    private long state;
    private Node parent;

    /**
     * Create a new Node, holding the given Pyramid Solitaire state plus a
     * reference to the parent state.  The initial state's parent should be passed in as null.
     *
     * @param state  the Node's state
     * @param parent the Node's parent
     */
    Node(long state, Node parent) {
        this.state = state;
        this.parent = parent;
    }

    long getState() {
        return state;
    }

    /**
     * Return the action performed from this Node's parent state in order to get to this Node's state.
     *
     * @param deck the Deck of cards being played in Pyramid Solitaire
     * @return the action performed to reach this state
     */
    private Action action(Deck deck) {
        Action action = null;
        if (parent != null) {
            long diff = state ^ parent.state;
            long existFlagsDiff = State.getDeckFlags(diff);
            long cycleDiff = State.getCycle(diff);
            if (cycleDiff != 0) {
                action = Action.newRecycleAction();
            } else if (existFlagsDiff != 0) {
                int stockIndex = State.getStockIndex(parent.state);
                List<String> cardsToRemove = new ArrayList<>();
                List<String> positionsToRemove = new ArrayList<>();
                long flag = 1L;
                for (int i = 0; i < 52; i++) {
                    if ((flag & existFlagsDiff) != 0) {
                        cardsToRemove.add(deck.cardAt(i));
                        if (i < 28) {
                            positionsToRemove.add(String.valueOf(i));
                        } else if (i == stockIndex) {
                            positionsToRemove.add("Stock");
                        } else {
                            positionsToRemove.add("Waste");
                        }
                    }
                    flag <<= 1;
                }
                action = Action.newRemoveAction(cardsToRemove, positionsToRemove);
            } else {
                action = Action.newDrawAction();
            }
        }
        return action;
    }


    /**
     * Return a list of all the actions performed to get from the initial state to this Node's state.
     *
     * @param deck the Deck of cards being played in Pyramid Solitaire
     * @return a list of Actions
     */
    List<Action> actions(Deck deck) {
        List<Action> actions;
        if (parent == null) {
            actions = new ArrayList<>();
        } else {
            actions = parent.actions(deck);
            actions.add(action(deck));
        }
        return actions;
    }

}
