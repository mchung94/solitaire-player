package com.secondthorn.solitaireplayer.players.pyramid;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Region;

import java.util.HashMap;
import java.util.Map;

/**
 * PyramidWindow represents the client area of the Microsoft Solitaire Collection window during
 * Pyramid Solitaire games.  The difference between this class and MSCWindow is that MSCWindow
 * represents the window itself and operations to perform on it such as resizing the window
 * and giving it focus, while PyramidWindow represents the game inside the window and interacts
 * with the cards and buttons in the game.
 */
class PyramidWindow {
    /**
     * The src/main/resources/pyramid/ subdirectory that contains the appropriate images and data relating
     * to the cards for the window scaling and type of game.
     */
    private String resourceDir;

    /**
     * The Sikuli region of the Microsoft Solitaire Collection window (x, y, width, height)
     */
    private Region appRegion;

    /**
     * The collection of rank images for the game being played (A23456789TJQK).
     */
    private Map<Character, Image> rankImages;

    /**
     * The collection of suit images for the game being played (cdhs).
     */
    private Map<Character, Image> suitImages;

    /**
     * The image of the Draw button to click on for drawing a card from the deck to the waste pile
     * or recycling the deck.
     */
    private Image drawImage;

    /**
     * The image of the Undo Board button for resetting the game back to the start.
     */
    private Image undoBoardImage;

    /**
     * The image of the OK button to confirm the Undo Board action.
     */
    private Image okImage;

    /**
     * The Sikuli Regions (x, y, width, height) of all the cards as well as the stock and waste piles.
     */
    private Regions regions;

    PyramidWindow() throws PlayException {
        appRegion = org.sikuli.script.App.focusedWindow();
        resourceDir = findResourceDir();
        regions = Regions.newInstance(resourceDir + "regions.json");
        loadImages();
    }

    /**
     * Draw a card from the deck to the waste pile.
     */
    void draw() {
        if (click(drawImage, 1.0d)) {
            sleep(250); // wait for the card to move to the waste pile
        }
    }

    /**
     * Recycle the waste pile back into the deck.  This and draw() both click on the Draw button
     * because that's how the game functionality was designed.
     */
    void recycle() {
        if (click(drawImage, 1.0d)) {
            sleep(1000); // wait for the cards to move from the waste pile to the stock pile
        }
    }

    /**
     * Click on a card in the pyramid at the given pyramid index.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     *
     * @param pyramidIndex The index 0-27 of one of the cards on the pyramid.
     */
    void clickPyramidCardIndex(int pyramidIndex) {
        sleep(250);
        regions.pyramid[pyramidIndex].click();
    }

    /**
     * Click on the stock pile to remove the card.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     */
    void clickStockCard() {
        sleep(250);
        regions.stock.click();
    }

    /**
     * Click on the waste pile to remove the card.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     */
    void clickWasteCard() {
        sleep(250);
        regions.waste.click();
    }

    /**
     * Undo the board if the option is available.
     */
    void undoBoard() {
        if (clickIfExists(undoBoardImage)) {
            click(okImage, 3.0d);
            sleep(2000); // wait for the cards to move back into the starting positions
        }
    }

    /**
     * Return the card at the given pyramid card index.
     *
     * @param pyramidIndex the pyramid card index to look at
     * @return The two-letter String for the card at the pyramid index, or null if no card is there.
     */
    String cardAtPyramid(int pyramidIndex) {
        return cardAtRegion(regions.pyramid[pyramidIndex]);
    }

    /**
     * Return the card at the top of the deck.
     *
     * @return The two-letter String for the card at the top of the deck, or null if no card is there.
     */
    String cardAtDeck() {
        return cardAtRegion(regions.stock);
    }

    /**
     * Figure out which resource directory for Pyramid Solitaire to use based on the Windows scaling factor
     * as well as the type of game being played (a regular game or a game with a set goal), which affects the
     * size and location of the cards).
     * <p>
     * Basically the window is resized to 1024x768, but at 200% and 250% window scaling (in Windows 10
     * Display Settings), the actual window resolution is different, so you can derive what the scaling
     * percentage is.  Then the top of the Pyramid Solitaire window client area either says "Pyramid"
     * in a goal-based game, or a pyramid symbol when it's a regular game played for points.
     * <p>
     * When playing a goal-based Pyramid Solitaire game, the extra bar at the bottom of the client area
     * that displays the goal, takes up space and means the card images are shifted and squashed a bit
     * compared to the regular Pyramid Solitaire games.
     *
     * @return The resource path directory to use for loading images and data
     * @throws PlayException if there's a problem determining the correct resource directory
     */
    private String findResourceDir() throws PlayException {
        String scaleDir = "pyramid/" + MSCWindow.getPercentScaling() + "-percent-scaling/";
        if (pyramidImageExists(scaleDir + "goal/Pyramid.png")) {
            return scaleDir + "goal/";
        } else if (pyramidImageExists(scaleDir + "regular/Pyramid.png")) {
            return scaleDir + "regular/";
        } else {
            throw new PlayException("Unable to detect if we're playing a Regular or Goal game of Pyramid Solitaire");
        }
    }

    /**
     * Check if the pyramid image exists somewhere on the app window region.
     * This is meant for checking which type of pyramid symbol/word is on the top of the client area
     * when finding out if this is a regular or goal-based Pyramid Solitaire game.
     *
     * @param imageFilename the image to look for in the window client area
     * @return true if the image exists in the Microsoft Solitaire Collection window
     */
    private boolean pyramidImageExists(String imageFilename) {
        Image image = Image.create(ClassLoader.getSystemResource(imageFilename));
        return appRegion.exists(image, 0) != null;
    }

    /**
     * Load and store all images in the resource directory - ranks, suits, Draw, Undo Board, and OK.
     */
    private void loadImages() {
        suitImages = new HashMap<>();
        for (char suit : "cdhs".toCharArray()) {
            Image image = loadResourceImage(suit + ".png");
            suitImages.put(suit, image);
        }
        rankImages = new HashMap<>();
        for (char rank : "A23456789TJQK".toCharArray()) {
            Image image = loadResourceImage(rank + ".png");
            rankImages.put(rank, image);
        }
        drawImage = loadResourceImage("Draw.png");
        undoBoardImage = loadResourceImage("UndoBoard.png");
        okImage = loadResourceImage("OK.png");
    }

    /**
     * Given an image filename, load and return the image for use by Sikuli.
     *
     * @param filename the image filename
     * @return the Sikuli Image object
     */
    private Image loadResourceImage(String filename) {
        return Image.create(ClassLoader.getSystemResource(resourceDir + filename));
    }

    /**
     * Click on an image if it exists on the screen, but return immediately without doing anything if not found.
     * @param image the image to search for and click on
     * @return true if the image was found and clicked on, false otherwise (no click occurred)
     */
    private boolean clickIfExists(Image image) {
        return click(image, 0.0d);
    }

    /**
     * Search for the image and click on it if it is found on screen.  If not found, wait up to timeout seconds
     * for it the image.
     * @param image   the image to search for and click on
     * @param timeout the number of seconds to wait for the image to appear if not found immediately
     * @return true if the image was found and clicked on, false otherwise (no click occurred)
     */
    private boolean click(Image image, double timeout) {
        Match match = appRegion.exists(image, timeout);
        return (match != null) && (match.click() > 0);
    }

    /**
     * Determine the most likely card to be at the region, or null if there isn't a card there or
     * if it can't figure out what card is there.
     *
     * @param region a region to check for a card (upper left corner containing rank and suit)
     * @return A two-letter card representation (rank and suit) or null if no card found
     */
    private String cardAtRegion(Region region) {
        try {
            Match m = region.findBest(rankImages.values().toArray());
            String card = "";
            if (m != null) {
                card += m.getImageFilename().charAt(m.getImageFilename().length() - 5);
                m = region.findBest(suitImages.values().toArray());
                if (m != null) {
                    card += m.getImageFilename().charAt(m.getImageFilename().length() - 5);
                    return card;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            // if findBest doesn't find any matches it throws IndexOutOfBoundsException
            return null;
        }
        return null;
    }

    /**
     * Utility function to sleep for the given number of milliseconds, catching and
     * ignoring the InterruptedException if it happens.
     *
     * @param milliseconds the number of milliseconds to sleep
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            // do nothing
        }
    }

}
