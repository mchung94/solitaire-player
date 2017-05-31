package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.List;

/**
 * This is just a wrapper around a regular Node but with the depth cached.  Depth could be derived by
 * following a Node's parents but it takes time.  Why not store the depth in the regular Node?  Because
 * some algorithms like Breadth First Search don't really need it, and can save memory by not storing it.
 */
public class NodeWithDepth {
    private int depth;
    private Node node;

    /**
     * Create a new NodeWithDepth for the given state with parent node and depth passed in.
     *
     * @param state      the Node's state
     * @param parentNode the parent node
     * @param depth      the number of steps from the initial state to this state
     */
    public NodeWithDepth(long state, NodeWithDepth parentNode, int depth) {
        if (parentNode == null) {
            this.node = new Node(state, null);
        } else {
            this.node = new Node(state, parentNode.node);
        }
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public long getState() {
        return node.getState();
    }

    /**
     * Return a list of all the actions performed to get from the initial state to this Node's state.
     *
     * @param deck the Deck of cards being played in Pyramid Solitaire
     * @return a list of Actions
     */
    public List<Action> actions(Deck deck) {
        return node.actions(deck);
    }
}
