package com.secondthorn.solitaireplayer.solvers.tripeaks;

public class Action {
    /**
     * All possible actions to play during a game of TriPeaks Solitaire.
     */
    public enum Command {
        /**
         * Move a tableau card to the waste pile.
         */
        REMOVE,
        /**
         * Move a card from the stock pile to the waste pile.
         */
        DRAW,
        /**
         * Undo the board (restart the game from the beginning)
         */
        UNDO_BOARD
    }

    private Command command;
    private String card;
    private int deckIndex;

    /**
     * Creates a new Action to specify to the player what to do in a game of TriPeaks Solitaire.
     *
     * @param action Either a card or "Undo Board"
     * @param deck   A TriPeaks Solitaire deck of cards
     */
    public Action(String action, Deck deck) {
        if (action.equals("Undo Board")) {
            command = Command.UNDO_BOARD;
        } else {
            card = action;
            deckIndex = deck.indexOf(card);
            if (deckIndex < 28) {
                command = Command.REMOVE;
            } else {
                command = Command.DRAW;
            }
        }
    }

    /**
     * Returns an instruction for the player: UNDO_BOARD, DRAW, or REMOVE.
     *
     * @return the command for this action
     */
    public Command getCommand() {
        return command;
    }

    /**
     * For Drawing or Removing a card, which card to move to the waste pile.
     * This is meaningless for an UNDO_BOARD command.
     *
     * @return the card the action applies to
     */
    public String getCard() {
        return card;
    }

    /**
     * For Drawing or Removing a card, the deck index of the card to act on.
     * This is meaningless for an UNDO_BOARD command.
     *
     * @return the deck index of the card the action applies to
     */
    public int getDeckIndex() {
        return deckIndex;
    }
}
