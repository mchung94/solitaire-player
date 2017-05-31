package com.secondthorn.solitaireplayer.players.pyramid;

import com.secondthorn.solitaireplayer.players.MSCWindow;
import com.secondthorn.solitaireplayer.players.PlayException;
import com.secondthorn.solitaireplayer.players.SolitairePlayer;
import com.secondthorn.solitaireplayer.solvers.pyramid.Action;
import com.secondthorn.solitaireplayer.solvers.pyramid.BoardChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.pyramid.CardChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.pyramid.Deck;
import com.secondthorn.solitaireplayer.solvers.pyramid.PyramidSolver;
import com.secondthorn.solitaireplayer.solvers.pyramid.ScoreChallengeSolver;
import org.sikuli.basics.Settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.sikuli.script.Sikulix.inputText;

/**
 * A class that uses Sikuli to automate playing a game of Pyramid Solitaire in
 * Windows 10's Microsoft Solitaire Collection.
 */
public class PyramidPlayer extends SolitairePlayer {
    private PyramidSolver solver;

    /**
     * Create a new PyramidPlayer instance based on command line args.
     * It will throw IllegalArgumentException if the args don't make sense.
     * The player can specialize in either clearing the board/table, maximizing score, or
     * clearing cards of a certain rank.
     *
     * @param args command line args
     */
    public PyramidPlayer(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing goal for Pyramid Solitaire");
        }
        String goalType = args[0];
        switch (goalType) {
            case "Board":
                if (args.length != 1) {
                    throw new IllegalArgumentException("Wrong number of args for Pyramid Solitaire Board Challenge");
                }
                solver = new BoardChallengeSolver();
                break;
            case "Score":
                switch (args.length) {
                    case 1:
                        solver = new ScoreChallengeSolver();
                        break;
                    case 3:
                        int goalScore = parseInt(args[1]);
                        int currentScore = parseInt(args[2]);
                        solver = new ScoreChallengeSolver(goalScore, currentScore);
                        break;
                    default:
                        throw new IllegalArgumentException("Wrong number of args for Pyramid Solitaire Score Challenge");
                }
                break;
            case "Card":
                if (args.length != 4) {
                    throw new IllegalArgumentException("Wrong number of args for Pyramid Solitaire Card Challenge");
                }
                int goalNumCardsToClear = parseInt(args[1]);
                char cardRankToClear = parseCardRank(args[2]);
                int currentNumCardsCleared = parseInt(args[3]);
                solver = new CardChallengeSolver(goalNumCardsToClear, cardRankToClear, currentNumCardsCleared);
                break;
            default:
                throw new IllegalArgumentException("Unknown goal for Pyramid Solitaire: " + goalType);
        }
    }

    /**
     * Play a Pyramid Solitaire game.
     */
    public void play() throws PlayException {
        Settings.InputFontMono = true;
        MSCWindow.positionForPlay();
        PyramidWindow window = new PyramidWindow();
        window.undoBoard();
        Deck deck = buildDeck(window);
        List<String> missingCards = missingCards(deck);
        List<String> duplicateCards = duplicateCards(deck);
        boolean ok = false;  // let user verify even if everything looks OK at first
        while (!ok) {
            String message = "Please verify the list of cards is correct.  Click Cancel to quit.";
            if (missingCards.size() > 0) {
                message += "\nMissing Cards: " + missingCards;
            } else {
                message += "\nMissing Cards: None";
            }
            if (duplicateCards.size() > 0) {
                message += "\nDuplicate Cards: " + duplicateCards;
            } else {
                message += "\nDuplicate Cards: None";
            }
            String newDeckText = inputText(message, "Deck Verification", 8, 72, deck.toString());
            if (newDeckText == null) {
                throw new PlayException("User cancelled verification of deck cards and the program will quit.");
            }
            deck = new Deck(newDeckText);
            missingCards = missingCards(deck);
            duplicateCards = duplicateCards(deck);
            if ((missingCards.size() == 0) && (duplicateCards.size() == 0)) {
                ok = true;
            }
        }
        List<List<Action>> solutions = solver.solve(deck);
        List<Action> solutionToPlay;
        switch (solutions.size()) {
            case 0:
                throw new PlayException("No solution was found for this Pyramid Solitaire game.");
            case 1:
                solutionToPlay = solutions.get(0);
                break;
            case 2:
                throw new PlayException("2 solutions found, should have implemented a way to choose one");
            default:
                throw new PlayException("An unexpected number of solutions was found: " + solutions.size());
        }

        for (Action action : solutionToPlay) {
            switch (action.getCommand()) {
                case DRAW:
                    window.draw();
                    break;
                case RECYCLE:
                    window.recycle();
                    break;
                case REMOVE:
                    for (int i=0; i<action.getPositions().size(); i++) {
                        String position = action.getPositions().get(i);
                        switch (position) {
                            case "Deck":
                                window.removeDeckCard();
                                break;
                            case "Waste":
                                window.removeWasteCard();
                                break;
                            default:
                                window.removeTableCardIndex(Integer.parseInt(position));
                        }
                    }
                    break;
            }
        }
        MSCWindow.positionForPlay();
    }

    /**
     * Look through all the cards in the game and create a Deck once you've found the 52 cards.
     * Basically, we scan through all the cards on the Pyramid, then flip through all the cards
     * in the deck looking at each one.
     * @param window the PyramidWindow to help read cards on the screen
     * @return A 52 card Deck object
     * @throws PlayException if we're unable to read 52 cards, regardless of mistakes while reading
     */
    private Deck buildDeck(PyramidWindow window) throws PlayException {
        List<String> cards = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            cards.add(window.cardAtPyramid(i));
        }
        for (int i = 0; i < 24; i++) {
            String card = window.cardAtDeck();
            cards.add(card);
            window.draw();
            while (!card.equals(window.cardAtWaste())) {
                sleep(50);
            }
        }
        window.undoBoard();
        if (cards.size() != 52) {
            throw new PlayException("Unable to read 52 cards, read " + cards.size() + " instead.");
        }
        return new Deck(cards);
    }

    /**
     * Check if a Deck is missing any of the standard 52 cards.
     * @param deck a Deck of cards being played in Pyramid Solitaire
     * @return a list (possibly empty) of the cards missing in the Deck
     */
    private List<String> missingCards(Deck deck) {
        List<String> missingCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            cardSet.add(deck.cardAt(i));
        }
        for (char suit : "cdhs".toCharArray()) {
            for (char rank : "A23456789TJQK".toCharArray()) {
                String card = "" + rank + suit;
                if (!cardSet.contains(card)) {
                    missingCards.add(card);
                }
            }
        }
        return missingCards;
    }

    private List<String> duplicateCards(Deck deck) {
        List<String> duplicateCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            String card = deck.cardAt(i);
            if (cardSet.contains(card)) {
                duplicateCards.add(card);
            } else {
                cardSet.add(card);
            }
        }
        return duplicateCards;
    }

}
