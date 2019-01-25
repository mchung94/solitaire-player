package com.secondthorn.solitaireplayer.players.pyramid;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;
import com.secondthorn.solitaireplayer.players.RegionDeserializer;
import org.sikuli.basics.Settings;
import org.sikuli.script.IRobot;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Region;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
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
     * The SikuliX region of the Microsoft Solitaire Collection window (x, y, width, height)
     */
    private Region appRegion;

    /**
     * The collection of rank images for the game being played (A23456789TJQK).
     */
    private Map<Image, Character> rankImages;

    /**
     * The collection of suit images for the game being played (cdhs).
     */
    private Map<Image, Character> suitImages;

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
     * The SikuliX Regions (x, y, width, height) of all the cards as well as the stock and waste piles.
     */
    private Regions regions;

    PyramidWindow() throws PlayException {
        appRegion = org.sikuli.script.App.focusedWindow();
        resourceDir = findResourceDir();
        regions = RegionDeserializer.createRegions(resourceDir + "regions.json", Regions.class);
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
        clickRegion(regions.getPyramid()[pyramidIndex]);
    }

    /**
     * Click on the stock pile to remove the card.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     */
    void clickStockCard() {
        sleep(250);
        clickRegion(regions.getStock());
    }

    /**
     * Click on the waste pile to remove the card.  You have to click on a King or a
     * pair of cards to actually remove them but this clicks on one of the cards you want to remove.
     */
    void clickWasteCard() {
        sleep(250);
        clickRegion(regions.getWaste());
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
        return cardAt(regions.getPyramid()[pyramidIndex]);
    }

    /**
     * Return the card at the top of the deck.
     *
     * @return The two-letter String for the card at the top of the deck, or null if no card is there.
     */
    String cardAtDeck() {
        return cardAt(regions.getStock());
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
        String scaleDir = "Pyramid/" + MSCWindow.getSizeString() + "/";
        if (pyramidImageExists(scaleDir + "Game.png")) {
            return scaleDir;
        } else {
            throw new PlayException("Unable to detect if we're playing a game of Pyramid Solitaire");
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
        return appRegion.exists(image, 0.0d) != null;
    }

    /**
     * Load and store all images in the resource directory - ranks, suits, Draw, Undo Board, and OK.
     */
    private void loadImages() throws PlayException {
        suitImages = loadCharImages("cdhs");
        rankImages = loadCharImages("A23456789TJQK");
        drawImage = loadResourceImage("Draw.png");
        undoBoardImage = loadResourceImage("UndoBoard.png");
        okImage = loadResourceImage("OK.png");
    }

    /**
     * Given an image filename, load and return the image for use by SikuliX.
     *
     * @param filename the image filename
     * @return the SikuliX Image object
     */
    private Image loadResourceImage(String filename) {
        return Image.create(ClassLoader.getSystemResource(resourceDir + filename));
    }

    /**
     * Click on an image if it exists on the screen, but return immediately without doing anything if not found.
     *
     * @param image the image to search for and click on
     * @return true if the image was found and clicked on, false otherwise (no click occurred)
     */
    private boolean clickIfExists(Image image) {
        return click(image, 0.0d);
    }

    /**
     * Search for the image and click on it if it is found on screen.  If not found, wait up to timeout seconds
     * for it the image.
     *
     * @param image   the image to search for and click on
     * @param timeout the number of seconds to wait for the image to appear if not found immediately
     * @return true if the image was found and clicked on, false otherwise (no click occurred)
     */
    private boolean click(Image image, double timeout) {
        Match match = appRegion.exists(image, timeout);
        if (match != null) {
            return clickRegion(match);
        }
        return false;
    }

    /**
     * This is a temporary workaround for issues moving the mouse in Windows 10.  Normally this would just be
     * region.click() but we move the mouse using an alternative method and then perform the mouse click.
     *
     * @param region Move the mouse to the center of this region and click.
     * @return true if the click was successful
     */
    private boolean clickRegion(Region region) {
        MSCWindow.moveMouseSmoothly(region.getCenter().x, region.getCenter().y);
        IRobot r = region.getCenter().getScreen().getRobot();
        r.mouseDown(16);
        r.delay(Settings.ClickDelay > 1.0D ? 1 : (int) (Settings.ClickDelay * 1000.0D));
        r.mouseUp(16);
        r.waitForIdle();
        return true;
    }

    private ArrayList<String> characterImageFilenames(char c) throws PlayException {
        ArrayList<String> filenames = new ArrayList<>();
        String imageDirectory = resourceDir + c;
        try {
            java.net.URI uri = ClassLoader.getSystemResource(imageDirectory).toURI();
            if (uri.getScheme().equals("jar")) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, java.util.Collections.emptyMap(), null)) {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath(imageDirectory))) {
                        for (Path path : ds) {
                            int nameCount = path.getNameCount();
                            filenames.add(path.subpath(nameCount - 2, nameCount).toString());
                        }
                    }
                }
            } else {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(uri))) {
                    for (Path path : ds) {
                        int nameCount = path.getNameCount();
                        filenames.add(path.subpath(nameCount - 2, nameCount).toString());
                    }
                }

            }
        } catch (URISyntaxException ex) {
            throw new PlayException("Unable to find resource directory for card-related character " + c);
        } catch (IOException ex) {
            throw new PlayException("Unable to find image files for the card-related character " + c);
        }
        return filenames;
    }

    /**
     * Given a set of characters, load the images for each character's .png files and return
     * a mapping between them.  This is for loading card suit and rank images.
     *
     * @param chars A sequence of chars, each being the name of directory containing .png files
     * @return a mapping between the image and the char it represents
     */
    private Map<Image, Character> loadCharImages(CharSequence chars) throws PlayException {
        Map<Image, Character> charImages = new HashMap<>();
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            for (String filename : characterImageFilenames(c)) {
                Image image = loadResourceImage(filename);
                charImages.put(image, c);
            }
        }
        return charImages;
    }

    /**
     * Determine the most likely card to be at the region, or null if there isn't a card there or
     * if it can't figure out what card is there.
     *
     * @param region a region to check for a card (upper left corner containing rank and suit)
     * @return A two-letter card representation (rank and suit) or null if no card found
     */
    private String cardAt(Region region) {
        String card = null;
        Character rank = findBestCardCharacter(region, rankImages);
        Character suit = findBestCardCharacter(region, suitImages);
        if ((rank != null) && (suit != null)) {
            card = rank.toString() + suit.toString();
        }
        return card;
    }

    /**
     * Given a region and a map between image to suit/rank Characters, find the best matching
     * suit or rank in that region.
     *
     * @param region             a region to check for a suit or rank image
     * @param imagesToCharacters a mapping between Images and Characters for suit/rank
     * @return the Character value of the best matching Image in the mapping
     */
    private Character findBestCardCharacter(Region region, Map<Image, Character> imagesToCharacters) {
        Character rank = null;
        try {
            Match m = region.findBest(imagesToCharacters.keySet().toArray());
            if (m != null) {
                rank = imagesToCharacters.get(m.getImage());
            }
        } catch (IndexOutOfBoundsException ex) {
            // if findBest doesn't find any matches it throws IndexOutOfBoundsException
        }
        return rank;
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
