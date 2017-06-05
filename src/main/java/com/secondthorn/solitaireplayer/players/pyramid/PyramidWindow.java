package com.secondthorn.solitaireplayer.players.pyramid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        adjustFontSettings();
        loadCardRegions();
        loadImages();
    }

    public void draw() throws PlayException {
        clickDrawButton();
        sleep(100);
    }

    public void recycle() throws PlayException {
        clickDrawButton();
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

    public String cardAtPyramid(int pyramidIndex) {
        return cardAtRegion(pyramidRegions[pyramidIndex]);
    }

    public String cardAtDeck() {
        return cardAtRegion(deckRegion);
    }

    public String cardAtWaste() {
        return cardAtRegion(wasteRegion);
    }

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

    private String findResourceDir() throws PlayException {
        String scaleKey = (appRegion.w - appRegion.x) + "x" + (appRegion.h - appRegion.y);
        JsonNode scaleNode = loadJsonResource("pyramid/scales.json");
        scaleNode = scaleNode.get(scaleKey);
        if (scaleNode == null) {
            throw new PlayException("Unable to determine Windows Display Settings Scaling percentage");
        }
        String scaleDir = scaleNode.asText();
        if (pyramidImageExists(scaleDir + "goal/Pyramid.png")) {
            return scaleDir + "goal/";
        } else if (pyramidImageExists(scaleDir + "regular/Pyramid.png")) {
            return scaleDir + "regular/";
        } else {
            throw new PlayException("Unable to detect if we're playing a Regular or Goal game of Pyramid Solitaire");
        }
    }

    private boolean pyramidImageExists(String imageFilename) {
        Image image = Image.create(ClassLoader.getSystemResource(imageFilename));
        return appRegion.exists(image, 0) != null;
    }

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

    private void clickDrawButton() throws PlayException {
        if (drawImageMatch == null) {
            drawImageMatch = findImageMatch(drawImage);
        }
        drawImageMatch.click();
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
            // do nothing
        }
    }

    private void adjustFontSettings() {
        int startPos = resourceDir.indexOf("-percent-scaling") - 3;
        int scalingPercent = Integer.parseInt(resourceDir.substring(startPos, startPos+3));
        Settings.InputFontMono = true;
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
