package com.secondthorn.solitaireplayer.players.freecell;

import com.secondthorn.solitaireplayer.players.PlayException;
import com.secondthorn.solitaireplayer.players.SolitairePlayer;
import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;
import java.util.List;

import static org.sikuli.script.Sikulix.inputText;

/**
 * This is an experimental version based on 20LeeBrian1's ideas on how this can
 * work. Scan the deck of cards in the game, convert it to a text format that
 * the user can input into the online FreeCell solver
 */
public class FreeCellPlayer extends SolitairePlayer {

    public FreeCellPlayer(String[] args) {
        String goalType = args.length > 0 ? args[0] : "Board";
        switch (goalType) {
            case "Board":
                break;
            case "Score":
            case "Card":
                throw new IllegalArgumentException("Score and Card challenges aren't implemented yet.");
            default:
                throw new IllegalArgumentException("Unknown goal for FreeCell Solitaire: " + goalType);
        }
    }

    /**
     * Automatically play a Microsoft Solitaire Collection FreeCell game.
     * This method guides the player from beginning to end, asking for input
     * when it needs information.
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException if there's a problem while playing the game
     */
    @Override
    public void autoplay() throws InterruptedException, PlayException {
        FreeCellWindow window = new FreeCellWindow();
        window.moveMouse(1, 1);
        List<String> cards = scanCardsOnScreen(window);
        cards = verifyCards(cards);
        String message = "Copy the deck into https://fc-solve.shlomifish.org/js-fc-solve/text/ for the solution";
        String solutionText = inputText(message, "Deck in fc-solve format", 8, 120, cardsToFCSolveString(cards));
    }

    /**
     * Print out a solution but don't try to play the game.
     * @param filename the game input filename
     * @throws PlayException if the user cancels while the program is verifying the deck of cards
     */
    @Override
    public void preview(String filename) throws PlayException {
        System.out.println("Preview mode is unsupported for FreeCell Solitaire.");
    }

    @Override
    protected boolean isValidCards(List<String> cards,
                                   List<String> missing,
                                   List<String> duplicates,
                                   List<String> malformed,
                                   long numUnknownCards) {
        return ((cards.size() == 52) &&
                (missing.size() == 0) &&
                (duplicates.size() == 0) &&
                (malformed.size() == 0) &&
                (numUnknownCards == 0));
    }

    /**
     * A deck of cards is represented by a List and in my view, the cards in
     * FreeCell games are row-by-row, so visually if the game looks like:
     * 9d 9h Ah Td 4h Kd 7c 3d
     * 5s 8s Ac 2c Jh 4s 6c As
     * 6s 8d 8h Tc Qh 7s Kc 3h
     * Ts 3c Ks 2h Qd 5c Jc Kh
     * 7d 4c 9c 2s Th Qc Js 5h
     * 7h Jd 6h 2d 6d 4d 3s Ad
     * 8c 5d 9s Qs
     *
     * The list of cards will be:
     * 9d, 9h, Ah, Td, 4h, Kd, 7c, 3d, 5s, 8s, .... Ad, 8c, 5d, 9s, Qs
     *
     * @param cards a list of cards
     * @return The string of the layout of the cards at the start of the game.
     */
    @Override
    protected String cardsToString(List<String> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<52; i++) {
            sb.append(cards.get(i));
            sb.append(((i % 8) == 7) ? "\n" : " ");
        }
        return sb.toString().trim();
    }


    /**
     * In fc-solve format, the cards must be capitalized and each row must
     * represent one column of cards from left to right, like:
     * 9D 5S 6S TS 7D 7H 8C
     * 9H 8S 8D 3C 4C JD 5D
     * AH AC 8H KS 9C 6H 9S
     * TD 2C TC 2H 2S 2D QS
     * 4H JH QH QD TH 6D
     * KD 4S 7S 5C QC 4D
     * 7C 6C KC JC JS 3S
     * 3D AS 3H KH 5H AD
     * @param cards A list of cards representing a full 52-card deck
     * @return A string representing the cards in fc-solve format.
     */
    protected String cardsToFCSolveString(List<String> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<8; i++) {
            int index = i;
            while (index < 52) {
                sb.append(cards.get(index).toUpperCase());
                index += 8;
                sb.append((index < 52) ? " " : "\n");
            }
        }
        return sb.toString().trim();
    }

    private List<String> scanCardsOnScreen(FreeCellWindow window) {
        List<String> cards = new ArrayList<>();
        for (int i=0; i<52; i++) {
            cards.add(window.cardAt(i));
        }
        return cards;
    }
}