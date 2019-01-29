package com.secondthorn.solitaireplayer.players;

import org.sikuli.script.Region;

/**
 * The regions (x, y, width, height) of all the 28 cards on the tableau as well as the waste pile.
 * We "hardcode" these into resources/.../regions.json files instead of just searching for images throughout the
 * window, to make it more accurate.
 */
public class Regions {
    private Region[] tableau;
    private Region waste;
    private Region stock;

    /**
     * Return the regions for the 28 tableau cards (also known as the board or TriPeaks).
     */
    public Region[] getTableau() {
        return tableau;
    }

    /**
     * Return the region for the waste pile.
     */
    public Region getWaste() {
        return waste;
    }

    /**
     * Return the region for the stock pile.
     */
    public Region getStock() {
        return stock;
    }
}