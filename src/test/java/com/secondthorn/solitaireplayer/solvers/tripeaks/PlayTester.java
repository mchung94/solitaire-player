package com.secondthorn.solitaireplayer.solvers.tripeaks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class checks TriPeaks Solitaire solutions to see if the solution is playable, meaning the solution steps are
 * all valid moves.
 */
public class PlayTester {
    private Deck deck;
    private List<String> cards;
    private List<String> tableau;
    private Deque<String> wastePile;
    private Deque<String> stockPile;

    public PlayTester(Deck deck) {
        this.deck = deck;
        resetToStartOfGame();
    }

    /**
     * Returns true if the actions in the solution are all valid steps that are allowed in the game.
     * @param solution a solution with a list of actions to perform
     * @return true if the actions don't break any rules
     */
    boolean areActionsPlayable(Solution solution) {
        for (Action action : solution.getActions()) {
            switch (action.getCommand()) {
                case DRAW:
                    if (!stockPile.isEmpty()) {
                        wastePile.push(stockPile.pop());
                    } else {
                        return false;
                    }
                    break;
                case REMOVE:
                    int tableauIndex = action.getDeckIndex();
                    String tableauCard = action.getCard();
                    if (!cards.get(tableauIndex).equals(tableauCard)) {
                        return false;
                    }
                    if (isRemovable(tableauIndex)) {
                        cards.set(tableauIndex, null);
                        wastePile.push(tableauCard);
                    }
                    break;
                case UNDO_BOARD:
                    resetToStartOfGame();
                    break;
            }
        }
        return true;
    }

    /**
     * Initialize the playtester to the beginning of the game.
     */
    private void resetToStartOfGame() {
        cards = new ArrayList<>(deck.getCards());
        tableau = cards.subList(0, 28);
        wastePile = new ArrayDeque<>();
        wastePile.push(cards.get(28));
        stockPile = new ArrayDeque<>(cards.subList(29, 52));
    }

    /**
     * Return true if the tableau card at the given index can be moved to the top of the waste pile.
     */
    private boolean isRemovable(int tableauIndex) {
        String tableauCard = tableau.get(tableauIndex);
        String topCardOfWastePile = wastePile.peek();
        return (tableauCard != null) &&
                (!isBlocked(tableauIndex)) &&
                (isOneAboveOrBelow(tableauCard, topCardOfWastePile));
    }

    /**
     * Return true if the tableau card at the given index has cards blocking it from below.
     */
    private boolean isBlocked(int tableauIndex) {
        return IntStream.of(childIndexes(tableauIndex)).anyMatch(i -> tableau.get(i) != null);
    }

    /**
     * Returns true if card1 and card2 are one rank apart, and can be moved on top of each other in the waste pile.
     * This wraps around so that Ace and King are one rank apart.
     */
    private boolean isOneAboveOrBelow(String card1, String card2) {
        char rank1 = card1.charAt(0);
        char rank2 = card2.charAt(0);
        switch (rank1) {
            case 'A': return (rank2 == 'K') || (rank2 == '2');
            case '2': return (rank2 == 'A') || (rank2 == '3');
            case '3': return (rank2 == '2') || (rank2 == '4');
            case '4': return (rank2 == '3') || (rank2 == '5');
            case '5': return (rank2 == '4') || (rank2 == '6');
            case '6': return (rank2 == '5') || (rank2 == '7');
            case '7': return (rank2 == '6') || (rank2 == '8');
            case '8': return (rank2 == '7') || (rank2 == '9');
            case '9': return (rank2 == '8') || (rank2 == 'T');
            case 'T': return (rank2 == '9') || (rank2 == 'J');
            case 'J': return (rank2 == 'T') || (rank2 == 'Q');
            case 'Q': return (rank2 == 'J') || (rank2 == 'K');
            case 'K': return (rank2 == 'Q') || (rank2 == 'A');
            default:
                throw new IllegalArgumentException(card1 + " is not a real card");
        }
    }

    /**
     * Returns an array of tableau card indexes for the cards blocking the card from below.
     *       _0          _1          _2
     *     _3  _4      _5  _6      _7  _8
     *   _9  10  11  12  13  14  15  16  17
     * 18  19  20  21  22  23  24  25  26  27
     */
    private int[] childIndexes(int tableauIndex) {
        int[][] indexes = {
                {3, 4, 9, 10, 11, 18, 19, 20, 21},
                {5, 6, 12, 13, 14, 21, 22, 23, 24},
                {7, 8, 15, 16, 17, 24, 25, 26, 27},
                {9, 10, 18, 19, 20},
                {10, 11, 19, 20, 21},
                {12, 13, 21, 22, 23},
                {13, 14, 22, 23, 24},
                {15, 16, 24, 25, 26},
                {16, 17, 25, 26, 27},
                {18, 19},
                {19, 20},
                {20, 21},
                {21, 22},
                {22, 23},
                {23, 24},
                {24, 25},
                {25, 26},
                {26, 27},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
        };
        return indexes[tableauIndex];
    }
}
