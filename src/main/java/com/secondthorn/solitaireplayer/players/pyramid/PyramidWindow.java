package com.secondthorn.solitaireplayer.players.pyramid;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;
import org.sikuli.script.Image;

/**
 * Methods for a PyramidPlayer to interact with the Microsoft Solitaire Collection window.
 */
class PyramidWindow extends MSCWindow {
    /**
     * An image of the button to draw a card from the stock pile to the waste pile,
     * or recycle the deck when the stock pile is empty.
     */
    private Image drawImage;

    PyramidWindow() throws InterruptedException, PlayException {
        super("Pyramid");
        drawImage = loadImage("Pyramid/Draw.png");
    }

    /**
     * Draws a card from the stock pile to the waste pile. There are no checks in place to make sure this is possible.
     * The user must make sure that the stock pile is not empty and that it's not the end of the game.
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException if unable to click on the Draw button
     */
    void draw() throws InterruptedException, PlayException {
        clickImage(drawImage, 1.0d);
    }

    /**
     * Recycles the waste pile back into the stock pile. There are no checks in place to make sure this is possible.
     * The user must make sure that the stock pile is empty (unlike the draw() method), and that it's not the end of
     * the game.
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException if unable to click on the Draw button
     */
    void recycle() throws InterruptedException, PlayException {
        clickImage(drawImage, 1.0d);
        Thread.sleep(1000);
    }

    /**
     * Clicks on a card in the Pyramid. The user must make sure that the card is not blocked by any cards from below.
     * @param pyramidIndex a pyramid index from 0 to 27
     * @throws InterruptedException if the thread is interrupted
     */
    void clickPyramidCardIndex(int pyramidIndex) throws InterruptedException {
        clickRegion(regions.getTableau()[pyramidIndex]);
    }

    /**
     * Clicks on the card at the top of the stock pile. The user must make sure that there is a card here.
     * @throws InterruptedException if the thread is interrupted
     */
    void clickStockCard() throws InterruptedException {
        clickRegion(regions.getStock());
    }

    /**
     * Clicks on the card at the top of the waste pile. The user must make sure that there is a card here.
     * @throws InterruptedException
     */
    void clickWasteCard() throws InterruptedException {
        clickRegion(regions.getWaste());
    }

    /**
     * Returns the card at the given index of the Pyramid. The code might guess the wrong card or return "??" for
     * unknown card.
     * @param pyramidIndex a pyramid index from 0 to 27
     * @return the card at the given index or "??" if unknown
     */
    String cardAtPyramid(int pyramidIndex) {
        return cardAt(regions.getTableau()[pyramidIndex]);
    }

    /**
     * Returns the card at the top of the stock pile. The code might guess the wrong card or return "??" for unknown
     * card.
     * @return the card at the top of the stock pile or "??" if unknown
     */
    String cardAtDeck() {
        return cardAt(regions.getStock());
    }
}
