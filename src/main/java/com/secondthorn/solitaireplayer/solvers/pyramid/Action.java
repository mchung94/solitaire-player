package com.secondthorn.solitaireplayer.solvers.pyramid;

import java.util.List;

/**
 * This class represents an action to perform in the solution to Pyramid Solitaire.
 * The commands consist of either removing one or a pair of cards, drawing a card from the deck
 * to the waste pile, or recycling the waste pile back into the deck.
 */
public class Action {
    private Command command;
    private List<String> cards;
    private List<String> positions;

    /**
     * A private constructor for Actions, use the static factory methods instead.
     * @param command the type of action for the player to perform
     * @param cards optional list of cards when the command is to remove cards
     * @param positions optional list of card positions when the command is to remove cards
     */
    private Action(Command command, List<String> cards, List<String> positions) {
        this.command = command;
        this.cards = cards;
        this.positions = positions;
    }

    /**
     * Create an Action to represent removing a King or a pair of cards whose ranks add up to 13.
     * @param cards the cards to remove
     * @param positions the positions of the cards to remove
     * @return a new Action instance for removing cards
     */
    public static Action newRemoveAction(List<String> cards, List<String> positions) {
        return new Action(Command.REMOVE, cards, positions);
    }

    /**
     * Create an Action to represent drawing a card from the deck to the waste pile.
     * This can only be done if the deck is not empty.
     * @return a new Action instance for drawing a card from deck to waste
     */
    public static Action newDrawAction() {
        return new Action(Command.DRAW, null, null);
    }

    /**
     * Create an Action to represent recycling the waste pile back into the deck.
     * This only can be done if the deck is empty.
     * @return a new Action instance for recycling the waste pile
     */
    public static Action newRecycleAction() {
        return new Action(Command.RECYCLE, null, null);
    }

    /**
     * For Actions involving removing cards, return the cards to remove.
     * Cards are represented as two-letter strings containing rank and suit.
     * @return a list of cards to remove
     */
    public List<String> getCards() {
        return cards;
    }

    /**
     * For Actions involving removing cards, return the card positions to remove.
     * If the card is in the deck, the value is "Deck".
     * If the card is in the waste pile, the value is "Waste".
     * Otherwise, the card is in the table/pyramid and is the integer table index as a String.
     * @return a list of positions to remove
     */
    public List<String> getPositions() {
        return positions;
    }

    /**
     * Return the command (REMOVE/DRAW/RECYCLE) this Action represents.
     * @return the command for the player to perform
     */
    public Command getCommand() {
        return command;
    }

    /**
     * An enumeration of the possible actions the player can take in Pyramid Solitaire.
     * Either remove a card or a pair of cards, draw a new card from the deck to the waste pile,
     * or recycle the waste pile back into the deck.
     */
    public enum Command {
        REMOVE,
        DRAW,
        RECYCLE
    }

    public String toString() {
        switch (command) {
            case DRAW:
                return "Draw";
            case RECYCLE:
                return "Recycle";
            default:
                return "Remove " + cards + " (" + positions + ")";
        }
    }
}
