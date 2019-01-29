package com.secondthorn.solitaireplayer.players.tripeaks;

import com.secondthorn.solitaireplayer.players.PlayException;
import com.secondthorn.solitaireplayer.players.SolitairePlayer;
import org.sikuli.basics.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Guides the user through a game of TriPeaks Solitaire. It interacts with the Microsoft Solitaire Collection window as
 * well as the user, from the start of the game until a solution is played or reported.
 */
public class TriPeaksPlayer extends SolitairePlayer {
    public TriPeaksPlayer(String[] args) {
        String challenge = args[0];
        switch (challenge) {
            case "Board":
                break;
            case "Score":
                break;
            case "Card":
                break;
            default:
                throw new IllegalArgumentException("Unknown challenge type for TriPeaks Solitaire: " + challenge);
        }
    }

    /**
     * Plays the currently displayed Microsoft Solitaire Collection Pyramid Solitaire game, using SikuliX to automate
     * the actions and scan the cards on the screen.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException        if there's a problem while playing the game
     */
    @Override
    public void autoplay() throws InterruptedException, PlayException {
        TriPeaksWindow window = new TriPeaksWindow();
        window.moveMouse(0, 0);
        List<String> cards = scanCardsOnScreen(window);
    }

    /**
     * Prints out the solution(s) but doesn't do any SikuliX-based automation to play the game.
     *
     * @throws PlayException if the user cancels while the program is verifying the deck of cards
     */
    @Override
    public void preview(String filename) throws PlayException {

    }

    /**
     * Looks through all the cards in the game and returns a list of the cards seen. The cards may be wrong and must
     * be verified and corrected by the user. Initially the game starts with the first 18 cards face down and these
     * are represented by "??" for unknown card.
     *
     * @param window the TriPeaksWindow to help read cards on the screen
     * @return a list of all the cards found on the screen
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException if there's a problem looking for cards in the Microsoft Solitaire Collection window
     */
    private List<String> scanCardsOnScreen(TriPeaksWindow window) throws InterruptedException, PlayException {
        window.undoBoard();
        List<String> cards = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            cards.add(window.cardAtTableau(i));
        }
        cards.add(window.cardAtWaste());
        for (int i = 0; i < 23; i++) {
            window.draw();
            cards.add(window.cardAtWaste());
        }
        window.undoBoard();
        return cards;
    }

    /**
     * Returns a String containing the cards in a TriPeaks Solitaire pattern. It contains the 28 card tableau,
     * followed by the top card of the waste pile on the next line, then a list of the rest of the cards starting from
     * the top of the stock pile to the bottom.
     * The argument must be a list of 52 cards (some may be unknown, represented by ??)
     *
     * @param cards a list of cards
     * @return a String representation of the cards laid out in a TriPeaks Solitaire game setup
     */
    private String cardsToString(List<String> cards) {
        return String.format(
                "      %s          %s          %s\n" +
                        "    %s  %s      %s  %s      %s  %s\n" +
                        "  %s  %s  %s  %s  %s  %s  %s  %s  %s\n" +
                        "%s  %s  %s  %s  %s  %s  %s  %s  %s  %s\n" +
                        "%s\n" +
                        "%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
                cards.toArray());
    }
}
