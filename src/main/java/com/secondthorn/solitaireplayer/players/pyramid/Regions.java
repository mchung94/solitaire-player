package com.secondthorn.solitaireplayer.players.pyramid;

import org.sikuli.script.Region;

/**
 * The regions (x, y, width, height) of all the 28 cards on the pyramid as well as the stock and
 * waste piles.  We "hardcode" these into resources/.../regions.json files instead of just searching
 * for images throughout the window, to make it more accurate.
 */
public class Regions {
    private Region[] pyramid;
    private Region stock;
    private Region waste;

    public Region[] getPyramid() {
        return pyramid;
    }

    public Region getStock() {
        return stock;
    }

    public Region getWaste() {
        return waste;
    }
}