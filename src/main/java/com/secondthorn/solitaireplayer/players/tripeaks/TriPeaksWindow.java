package com.secondthorn.solitaireplayer.players.tripeaks;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;

/**
 * Methods for a TriPeaksPlayer to interact with the Microsoft Solitaire Collection window.
 */
class TriPeaksWindow extends MSCWindow {
    TriPeaksWindow() throws InterruptedException, PlayException {
        super("TriPeaks");
    }

    /**
     * Clicks on one of the 28 tableau cards. The user must make sure it's a valid card to click on: no cards blocking
     * it from below and one rank above or below the card currently on the top of the waste pile.
     *
     * @param index a tableau index from 0 to 27
     * @throws InterruptedException if the thread is interrupted
     */
    void clickTableauCard(int index) throws InterruptedException {
        clickRegion(regions.getTableau()[index]);
    }

    /**
     * Clicks on the stock pile, drawing a card from the stock pile to the waste pile.  The user must make sure the
     * stock pile isn't empty.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    void clickStockCard() throws InterruptedException {
        clickRegion(regions.getStock());
    }

    /**
     * Draws a card from the stock pile to the waste pile. The user must make sure the stock pile is not empty.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    void draw() throws InterruptedException {
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
