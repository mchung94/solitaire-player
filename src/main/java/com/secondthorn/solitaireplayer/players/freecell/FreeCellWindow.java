package com.secondthorn.solitaireplayer.players.freecell;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;

public class FreeCellWindow extends MSCWindow {
    public FreeCellWindow() throws InterruptedException, PlayException {
        super("FreeCell");
    }

    /**
     * Returns the card at the given index of the FreeCell tableau. The code
     * might guess the wrong card or return "??" for unknown card.
     * @param deckIndex a card deck index from 0 to 51
     * @return the card at the given index or "??" if unknown
     */
    String cardAt(int deckIndex) {
        return cardAt(regions.getTableau()[deckIndex]);
    }
}
