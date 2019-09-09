package com.secondthorn.solitaireplayer.players.tripeaks;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;
import org.sikuli.script.Image;
import org.sikuli.script.Region;

/**
 * Methods for a TriPeaksPlayer to interact with the Microsoft Solitaire Collection window.
 */
class TriPeaksWindow extends MSCWindow {
    /**
     * An image to detect if the "No more moves" dialog popped up.
     */
    private Image undoLastMoveDialogImage;

    /**
     * The location of the "Undo last move" button that appears when the player runs out of moves.
     */
    private Region undoLastMove = new Region(781, 542, 214, 23);

    TriPeaksWindow() throws InterruptedException, PlayException {
        super("TriPeaks");
        undoLastMoveDialogImage = loadImage("TriPeaks/UndoLastMoveDialog.png");
    }

    /**
     * Undo the last move if the "No more moves!" message appears.
     * @return true if the the program undid the last move
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException if unable to click on the button
     */
    boolean undoWhenNoMoreMoves() throws InterruptedException, PlayException {
        if (appRegion().exists(undoLastMoveDialogImage, 1.0d) != null) {
            clickRegion(undoLastMove);
            return true;
        }
        return false;
    }

    /**
     * Clicks on one of the 28 tableau cards. The user must make sure it's a valid card to click on: no cards blocking
     * it from below and one rank above or below the card currently on the top of the waste pile.
     *
     * @param index a tableau index from 0 to 27
     * @throws InterruptedException if the thread is interrupted
     */
    void clickTableauCard(int index) throws InterruptedException, PlayException {
        clickRegion(regions.getTableau()[index]);
    }

    /**
     * Clicks on the stock pile, drawing a card from the stock pile to the waste pile.  The user must make sure the
     * stock pile isn't empty.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    void clickStockCard() throws InterruptedException, PlayException {
        clickRegion(regions.getStock());
    }

    /**
     * Draws a card from the stock pile to the waste pile. The user must make sure the stock pile is not empty.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    void draw() throws InterruptedException, PlayException {
        clickRegion(regions.getStock());
    }

    /**
     * Returns the card at the given part of the tableau. The code might guess the wrong card or return "??" for
     * unknown cards.
     *
     * @param index a tableau index from 0 to 27
     * @return the card at that index of the tableau or "??" if unknown
     */
    String cardAtTableau(int index) {
        return cardAt(regions.getTableau()[index]);
    }

    /**
     * Returns the card at the top of the waste pile. The code might guess the wrong card or return "??" for unknown
     * cards.
     *
     * @return the card at the top of the waste pile or "??" if unknown.
     */
    String cardAtWaste() {
        return cardAt(regions.getWaste());
    }
}
