package com.secondthorn.solitaireplayer.players.pyramid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Region;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * PyramidWindow represents the client area of the Microsoft Solitaire Collection window during
 * Pyramid Solitaire games.  The difference between this class and MSCWindow is that MSCWindow
 * represents the entire window itself and operations to perform on it such as resizing the window
 * and giving it focus, PyramidWindow represents what's inside the window and interacts with the
 * cards and buttons in the game.
 */
public class PyramidWindow {
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
     * The cached location of the Draw button.
     */
    private Match drawImageMatch;

    /**
     * The image of the Undo Board button for resetting the game back to the start.
     */
    private Image undoBoardImage;

    /**
     * The image of the OK button to confirm the Undo Board action.
     */
    private Image okImage;

    /**
     * The regions (x, y, width, height) of all the 28 cards on the board/table.  We "hardcode" these
     * into resources/.../regions.json files instead of just searching for images throughout the window,
     * to make it more accurate, but then this means we have to have multiple sets of these depending on
     * window scaling.
     */
    private Region[] pyramidRegions;

    /**
     * The region of the deck card pile on the game window.
     */
    private Region deckRegion;

    /**
     * The region of the waste pile on the game window.
     */
    private Region wasteRegion;

    public PyramidWindow() throws PlayException {
        Settings.InputFontMono = true;
        appRegion = org.sikuli.script.App.focusedWindow();
        resourceDir = findResourceDir();
        adjustFontSettings();
        loadCardRegions();
        loadImages();
    }

    /**
     * Draw a card from the deck to the waste pile.
     *
     * @throws PlayException if there's an issue with finding the Draw button
     */
    public void draw() throws PlayException {
        clickDrawButton();
        sleep(100);
    }

    /**
     * Recycle the waste pile back into the deck.  This and draw() both click on the Draw button
     * because that's how the game functionality was designed.
     *
     * @throws PlayException if there's an issue with finding the Draw button
     */
    public void recycle() throws PlayException {
        clickDrawButton();
        sleep(1000);
    }

    /**
     * Click on a card in the table/board at the given table index.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     *
     * @param tableIndex The index 0-27 of one of the cards on the table (the "pyramid").
     */
    public void removeTableCardIndex(int tableIndex) {
        sleep(500);
        pyramidRegions[tableIndex].click();
    }

    /**
     * Click on the deck to remove the card.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     */
    public void removeDeckCard() {
        sleep(500);
        deckRegion.click();
    }

    /**
     * Click on the waste pile to remove the card.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     */
    public void removeWasteCard() {
        sleep(500);
        wasteRegion.click();
    }

    /**
     * Undo the board if the option is available.
     */
    public void undoBoard() throws PlayException {
        Match undoBoardMatch = appRegion.exists(undoBoardImage, 0);
        if (undoBoardMatch != null) {
            undoBoardMatch.click();
            Match okMatch = appRegion.exists(okImage);
            if (okMatch != null) {
                okMatch.click();
            }
            sleep(500);
        }
    }

    /**
     * Return the card at the given table card index.
     *
     * @param pyramidIndex the table index to look at
     * @return The two-letter String for the card at the table pyramid index, or null if no card is there.
     */
    public String cardAtPyramid(int pyramidIndex) {
        return cardAtRegion(pyramidRegions[pyramidIndex]);
    }

    /**
     * Return the card at the top of the deck.
     *
     * @return The two-letter String for the card at the top of the deck, or null if no card is there.
     */
    public String cardAtDeck() {
        return cardAtRegion(deckRegion);
    }

    /**
     * Return the card at the top of the waste pile.
     *
     * @return The two-letter String for the card at the top of the waste pile, or null if no card is there.
     */
    public String cardAtWaste() {
        return cardAtRegion(wasteRegion);
    }

    /**
     * Parse and return the JSON in the given file location.
     *
     * @param resourcePath The path to the file containing JSON data.
     * @return A JsonNode of the file contents.
     * @throws PlayException if there's a problem reading the file.
     */
    private JsonNode loadJsonResource(String resourcePath) throws PlayException {
        ObjectMapper mapper = new ObjectMapper();
        URL url = ClassLoader.getSystemResource(resourcePath);
        JsonNode node;
        try {
            node = mapper.readTree(url);
            return node;
        } catch (IOException ex) {
            throw new PlayException("Unable to load " + resourcePath);
        }
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
     * Using the resource directory, load the regions.json file which contains the x, y, width, height
     * rectangles for the location of the upper left corner of each of the 28 cards on the
     * table, plus the locations of the deck and waste piles.
     *
     * @throws PlayException
     */
    private void loadCardRegions() throws PlayException {
        String regionsFilename = resourceDir + "regions.json";
        JsonNode regionsNode = loadJsonResource(regionsFilename);
        JsonNode pyramidRegionsNode = regionsNode.get("pyramidRegions");
        pyramidRegions = new Region[pyramidRegionsNode.size()];
        for (int i = 0; i < pyramidRegionsNode.size(); i++) {
            pyramidRegions[i] = createRegionFromJson(pyramidRegionsNode.get(i));
        }
        deckRegion = createRegionFromJson(regionsNode.get("deckRegion"));
        wasteRegion = createRegionFromJson(regionsNode.get("wasteRegion"));
    }

    /**
     * Given a set of x/y/width/height coordinates from the regions.json file,
     * return a Sikuli Region object encapsulating these values.  The purpose of having
     * the region is that you can call click() on them to click in the center of the area.
     *
     * @param node a JSON node containing fields x, y, width, and height
     * @return a Sikuli Region containing the x, y, width, and height fields
     */
    private Region createRegionFromJson(JsonNode node) {
        int x = node.get("x").asInt();
        int y = node.get("y").asInt();
        int width = node.get("width").asInt();
        int height = node.get("height").asInt();
        return Region.create(x, y, width, height);
    }

    /**
     * Load and store all images in the resource directory - ranks, suits, Draw, Undo Board, and OK.
     */
    private void loadImages() {
        suitImages = new HashMap<>();
        for (char suit : "cdhs".toCharArray()) {
            Image image = loadResourceImage("" + suit);
            suitImages.put(suit, image);
        }
        rankImages = new HashMap<>();
        for (char rank : "A23456789TJQK".toCharArray()) {
            Image image = loadResourceImage("" + rank);
            rankImages.put(rank, image);
        }
        drawImage = loadResourceImage("Draw");
        undoBoardImage = loadResourceImage("UndoBoard");
        okImage = loadResourceImage("OK");
    }

    /**
     * Click on the Draw button to draw a card from the deck to the waste pile, or recycle the
     * waste pile.
     *
     * @throws PlayException when there's a problem clicking on the Draw button
     */
    private void clickDrawButton() throws PlayException {
        if (drawImageMatch == null) {
            drawImageMatch = findImageMatch(drawImage);
        }
        drawImageMatch.click();
    }

    /**
     * Find and return the best matching location for the image on the window region.
     *
     * @param image the image to look for on the app window
     * @return the best match for the image
     * @throws PlayException if there's a problem finding the image on the screen
     */
    private Match findImageMatch(Image image) throws PlayException {
        Match bestMatch = null;
        try {
            Iterator<Match> iterator = appRegion.findAll(image);
            while (iterator.hasNext()) {
                Match m = iterator.next();
                if ((bestMatch == null) || (m.getScore() > bestMatch.getScore())) {
                    bestMatch = m;
                }
            }
        } catch (FindFailed ex) {
            throw new PlayException("Unable to find \"" + image.getImageName() + "\" on the screen", ex);
        }
        return bestMatch;
    }

    /**
     * Given an image filename, load and return the image for use by Sikuli.
     *
     * @param filename the image filename
     * @return the Sikuli Image object
     */
    private Image loadResourceImage(String filename) {
        return Image.create(ClassLoader.getSystemResource(resourceDir + filename + ".png"));
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

    /**
     * Resize the Sikuli input text size to adjust for the scaling factor.
     */
    private void adjustFontSettings() {
        int startPos = resourceDir.indexOf("-percent-scaling") - 3;
        int scalingPercent = Integer.parseInt(resourceDir.substring(startPos, startPos + 3));
        switch (scalingPercent) {
            case 100:
                Settings.InputFontSize = 14;
                break;
            case 200:
                Settings.InputFontSize = 28;
                break;
            case 250:
                Settings.InputFontSize = 35;
                break;
        }
    }

}
