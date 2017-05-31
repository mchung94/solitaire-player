package com.secondthorn.solitaireplayer.players.pyramid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondthorn.solitaireplayer.players.PlayException;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Region;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PyramidWindow {
    private String resourceDir;
    private Region appRegion;
    private Map<Character, Image> rankImages;
    private Map<Character, Image> suitImages;
    private Image drawImage;
    private Match drawImageMatch;
    private Image undoBoardImage;
    private Image okImage;
    private Region[] pyramidRegions;
    private Region deckRegion;
    private Region wasteRegion;

    public PyramidWindow() throws PlayException {
        appRegion = org.sikuli.script.App.focusedWindow();
        resourceDir = findResourceDir();
        loadCardRegions();
        loadImages();
        drawImageMatch = findImageMatch(drawImage);
    }

    public void draw() throws PlayException {
        drawImageMatch.click();
        sleep(100);
    }

    public void recycle() throws PlayException {
        drawImageMatch.click();
        sleep(1000);
    }

    public void removeTableCardIndex(int tableIndex) {
        sleep(500);
        pyramidRegions[tableIndex].click();
    }

    public void removeDeckCard() {
        sleep(500);
        deckRegion.click();
    }

    public void removeWasteCard() {
        sleep(500);
        wasteRegion.click();
    }

    public void undoBoard() throws PlayException {
        Match undoBoardMatch = appRegion.exists(undoBoardImage);
        if (undoBoardMatch != null) {
            undoBoardMatch.click();
            Match okMatch = appRegion.exists(okImage);
            if (okMatch != null) {
                okMatch.click();
            }
        }
    }

    public String cardAtPyramid(int pyramidIndex) {
        return cardAtRegion(pyramidRegions[pyramidIndex]);
    }

    public String cardAtDeck() {
        return cardAtRegion(deckRegion);
    }

    public String cardAtWaste() {
        return cardAtRegion(wasteRegion);
    }

    private String findResourceDir() throws PlayException {
        if (pyramidImageExists("pyramid/regular/Pyramid.png")) {
            return "pyramid/regular/";
        } else if (pyramidImageExists("pyramid/goal/Pyramid.png")) {
            return "pyramid/goal/";
        } else {
            throw new PlayException("Unable to detect if we're playing a Regular or Goal game of Pyramid Solitaire");
        }
    }

    private boolean pyramidImageExists(String imageFilename) {
        Image image = Image.create(ClassLoader.getSystemResource(imageFilename));
        return appRegion.exists(image) != null;
    }

    private void loadCardRegions() throws PlayException {
        ObjectMapper mapper = new ObjectMapper();
        String regionsFilename = resourceDir + "regions.json";
        URL url = ClassLoader.getSystemResource(regionsFilename);
        JsonNode regionsNode;
        try {
            regionsNode = mapper.readTree(url);
        } catch (IOException ex) {
            throw new PlayException("Unable to load " + regionsFilename);
        }
        JsonNode pyramidRegionsNode = regionsNode.get("pyramidRegions");
        pyramidRegions = new Region[pyramidRegionsNode.size()];
        for (int i = 0; i < pyramidRegionsNode.size(); i++) {
            pyramidRegions[i] = createRegionFromJson(pyramidRegionsNode.get(i));
        }
        deckRegion = createRegionFromJson(regionsNode.get("deckRegion"));
        wasteRegion = createRegionFromJson(regionsNode.get("wasteRegion"));
    }

    private Region createRegionFromJson(JsonNode node) {
        int x = node.get("x").asInt();
        int y = node.get("y").asInt();
        int width = node.get("width").asInt();
        int height = node.get("height").asInt();
        return Region.create(x, y, width, height);
    }

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

    private Image loadResourceImage(String filename) {
        return Image.create(ClassLoader.getSystemResource(resourceDir + filename + ".png"));
    }

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

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
        }
    }

}
