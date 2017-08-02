package com.secondthorn.solitaireplayer.solvers.pyramid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Pyramid represents precalculated data around the 28 pyramid cards that doesn't depend on the deck of cards being
 * played.  For example, it contains all possible arrangements for the 28 cards and which cards are covering/covered
 * by each pyramid card.
 */
public class Pyramid {
    /**
     * For each pyramid card index, a mask of all cards covering it from below.  COVER_MASKS is for checking if a
     * pyramid card is uncovered and therefore available for removal.  The bottom row of the pyramid has a mask of just
     * zero because they aren't covered by any other cards.
     */
    static final long[] COVER_MASKS = {
            0b1111111111111111111111111110L,
            0b0111111011111011110111011000L,
            0b1111110111110111101110110000L,
            0b0011111001111001110011000000L,
            0b0111110011110011100110000000L,
            0b1111100111100111001100000000L,
            0b0001111000111000110000000000L,
            0b0011110001110001100000000000L,
            0b0111100011100011000000000000L,
            0b1111000111000110000000000000L,
            0b0000111000011000000000000000L,
            0b0001110000110000000000000000L,
            0b0011100001100000000000000000L,
            0b0111000011000000000000000000L,
            0b1110000110000000000000000000L,
            0b0000011000000000000000000000L,
            0b0000110000000000000000000000L,
            0b0001100000000000000000000000L,
            0b0011000000000000000000000000L,
            0b0110000000000000000000000000L,
            0b1100000000000000000000000000L,
            0b0000000000000000000000000000L,
            0b0000000000000000000000000000L,
            0b0000000000000000000000000000L,
            0b0000000000000000000000000000L,
            0b0000000000000000000000000000L,
            0b0000000000000000000000000000L,
            0b0000000000000000000000000000L
    };

    /**
     * For each pyramid card index, a mask that excludes the cards that are covering or covered by it.
     * The nth card in the pyramid can't be removed by a card that is masked off by the nth mask.
     * Each value is the full 52 bits covering the entire deck.  The stock/waste cards (28th-51st) bits in the deck
     * are always included because they're never blocked by anything.
     */
    static final long[] UNRELATED_CARD_MASKS = {
            0b1111111111111111111111110000000000000000000000000000L,
            0b1111111111111111111111111000000100000100001000100100L,
            0b1111111111111111111111110000001000001000010001001010L,
            0b1111111111111111111111111100000110000110001100110100L,
            0b1111111111111111111111111000001100001100011001101000L,
            0b1111111111111111111111110000011000011000110011011010L,
            0b1111111111111111111111111110000111000111001110110100L,
            0b1111111111111111111111111100001110001110011101100000L,
            0b1111111111111111111111111000011100011100111011001000L,
            0b1111111111111111111111110000111000111001110111011010L,
            0b1111111111111111111111111111000111100111101110110100L,
            0b1111111111111111111111111110001111001111011100100000L,
            0b1111111111111111111111111100011110011110111001000000L,
            0b1111111111111111111111111000111100111101110011001000L,
            0b1111111111111111111111110001111001111011110111011010L,
            0b1111111111111111111111111111100111110111101110110100L,
            0b1111111111111111111111111111001111101111001100100000L,
            0b1111111111111111111111111110011111011110011000000000L,
            0b1111111111111111111111111100111110111100110001000000L,
            0b1111111111111111111111111001111101111001110011001000L,
            0b1111111111111111111111110011111011111011110111011010L,
            0b1111111111111111111111111111110111110111101110110100L,
            0b1111111111111111111111111111101111100111001100100000L,
            0b1111111111111111111111111111011111001110001000000000L,
            0b1111111111111111111111111110111110011100010000000000L,
            0b1111111111111111111111111101111100111000110001000000L,
            0b1111111111111111111111111011111001111001110011001000L,
            0b1111111111111111111111110111111011111011110111011010L
    };

    /**
     * Data for all possible combinations of cards in the pyramid, loaded from Pyramid.json.
     * This is too much data to be hardcoded into a static initializer, and I don't want to pay the cost of
     * calculating this every time we start the program, so it's loaded from a file.
     */
    static Pyramid[] ALL;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ALL = mapper.readValue(ClassLoader.getSystemResource("pyramid/Pyramid.json"), Pyramid[].class);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Bit flags for the existence of the 28 cards in the pyramid.  The indexes are arranged like this:
     *             00
     *           01  02
     *         03  04  05
     *       06  07  08  09
     *     10  11  12  13  14
     *   15  16  17  18  19  20
     * 21  22  23  24  25  26  27
     */
    private long flags;

    /**
     * The card indexes for all the uncovered cards remaining in the pyramid, according to flags.
     */
    private int[] uncoveredIndexes;

    /**
     * The card indexes for all the cards remaining in the pyramid, according to flags.
     */
    private int[] allIndexes;

    @JsonCreator
    private Pyramid(@JsonProperty("flags") long flags,
                    @JsonProperty("uncoveredIndexes") int[] uncoveredIndexes,
                    @JsonProperty("allIndexes") int[] allIndexes) {
        this.flags = flags;
        this.uncoveredIndexes = uncoveredIndexes;
        this.allIndexes = allIndexes;
    }

    long getFlags() {
        return flags;
    }

    int[] getUncoveredIndexes() {
        return uncoveredIndexes;
    }

    int[] getAllIndexes() {
        return allIndexes;
    }
}

