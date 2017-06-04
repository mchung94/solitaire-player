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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.sikuli.script.Sikulix.inputText;
import static org.sikuli.script.Sikulix.popAsk;
import static org.sikuli.script.Sikulix.popSelect;

/**
 * A class that uses Sikuli to automate playing a game of Pyramid Solitaire in
 * Windows 10's Microsoft Solitaire Collection.
 */
public class PyramidPlayer extends SolitairePlayer {
    /**
     * When a board can't be cleared at all, the fastest list of steps to follow is to
     * just Draw/Recycle cards until you can't anymore.  One interesting thing to note is that
     * in Microsoft Solitaire Collection on Windows 10, if you keep drawing/recycling cards until you can't
     * draw or recycle anymore, the game can end, even if there's still cards on the table you can still
     * remove (at least as of June 2017).
     */
    private static List<Action> loseQuickly;

    static {
        loseQuickly = new ArrayList<>();
        for (int cycle = 1; cycle <= 3; cycle++) {
            for (int deckCard = 0; deckCard < 24; deckCard++) {
                loseQuickly.add(Action.newDrawAction());
            }
            if (cycle < 3) {
                loseQuickly.add(Action.newRecycleAction());
            }
        }
    }

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
        MSCWindow.positionForPlay();
        List<String> cards = scanCardsOnScreen(window);
        Deck deck = buildDeck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        solver = null;
        System.gc();
        List<Action> solutionToPlay = chooseSolution(solutions);
        MSCWindow.positionForPlay();
        playSolution(solutionToPlay, window);
    }

    /**
     * Build and return the Deck of cards currently being played.
     * Ask the user to verify and fix if necessary.
     *
     * @param cards a list of cards (a rough draft to be verified and corrected by the user)
     * @return A 52 card Deck object
     * @throws PlayException if we're unable to read 52 cards, regardless of mistakes while reading
     */
    private Deck buildDeck(List<String> cards) throws PlayException {
        List<String> missing = missingCards(cards);
        List<String> duplicates = duplicateCards(cards);
        List<String> malformed = malformedCards(cards);
        do {
            String message = "Please verify the list of cards is correct and edit if necessary.";
            message += "\nClick Cancel to quit.";
            message += "\nNumber of cards (should be 52): " + cards.size();
            message += "\nMissing Cards: " + ((missing.size() > 0) ? missing : "None");
            message += "\nDuplicate Cards: " + ((duplicates.size() > 0) ? duplicates : "None");
            message += "\nMalformed Cards: " + ((malformed.size() > 0) ? malformed : "None");
            String newDeckText = inputText(message, "Deck Verification", 8, 72, pyramidString(cards));
            if (newDeckText == null) {
                throw new PlayException("User cancelled verification of deck cards and the program will quit.");
            }
            cards = new ArrayList<>(Arrays.asList(newDeckText.trim().split("\\s+")));
            missing = missingCards(cards);
            duplicates = duplicateCards(cards);
            malformed = malformedCards(cards);
        } while ((cards.size() != 52) || (missing.size() > 0) || (duplicates.size() > 0) || (malformed.size() > 0));
        return new Deck(cards);
    }

    /**
     * Look through all the cards in the game and return a list of the cards seen.
     * Basically, we scan through all the cards on the Pyramid, then flip through all the cards
     * in the deck looking at each one.
     *
     * @param window the PyramidWindow to help read cards on the screen
     * @return A List of all the cards found on the screen
     * @throws PlayException if there's a problem interacting with the window
     */
    private List<String> scanCardsOnScreen(PyramidWindow window) throws PlayException {
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
        return cards;
    }

    /**
     * Out of a list of solutions returned by the solver, figure out which one to play and return it.
     *
     * @param solutions a list of solutions generated by the solver
     * @return one of the solutions, a list of Actions
     * @throws PlayException if the number of solutions is unexpected (not one or two solutions)
     */
    private List<Action> chooseSolution(Map<String, List<Action>> solutions) throws PlayException {
        List<Action> solution = null;
        String solutionDescription = null;
        switch (solutions.size()) {
            case 0:
                solutionDescription = "No solution found, so lose quickly to get to the next deal.";
                solution = loseQuickly;
                break;
            case 1:
                solutionDescription = solutions.keySet().iterator().next();
                solution = solutions.get(solutionDescription);
                break;
            default:
                String[] keys = solutions.keySet().toArray(new String[solutions.keySet().size()]);
                Arrays.sort(keys);
                String message = "Select a solution, Cancel to exit without automatically playing.";
                String title = "Multiple Solutions Found";
                solutionDescription = popSelect(message, title, keys);
                solution = solutions.get(solutionDescription);
                break;
        }
        StringBuilder confirmMessage = new StringBuilder();
        confirmMessage.append("Press Yes to play solution, No to quit.\n");
        confirmMessage.append("Solution: ");
        confirmMessage.append(solutionDescription);
        confirmMessage.append("\n");

        if ((solution != null) && popAsk(confirmMessage.toString(), "Play the solution?")) {
            return solution;
        } else {
            throw new PlayException("User cancelled selecting and playing a solution.");
        }
    }

    /**
     * Given a list of actions by the solver, perform them on the game window.
     *
     * @param solution a list of Actions to perform
     * @param window   the Pyramid Window to perform the actions on
     * @throws PlayException if there's a problem interacting with the window
     */
    private void playSolution(List<Action> solution, PyramidWindow window) throws PlayException {
        for (Action action : solution) {
            switch (action.getCommand()) {
                case DRAW:
                    window.draw();
                    break;
                case RECYCLE:
                    window.recycle();
                    break;
                case REMOVE:
                    for (int i = 0; i < action.getPositions().size(); i++) {
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
    }

    /**
     * Check if a a list of cards is missing any of the standard 52 cards.
     *
     * @param cards a list of cards being played in Pyramid Solitaire
     * @return a list (possibly empty) of the cards missing in the Deck
     */
    private List<String> missingCards(List<String> cards) {
        List<String> missingCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>(cards);
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

    /**
     * Return a list of duplicate cards found in the list of cards
     *
     * @param cards a list of cards
     * @return a list of the duplicate cards in the given card list
     */
    private List<String> duplicateCards(List<String> cards) {
        List<String> duplicateCards = new ArrayList<>();
        Set<String> cardSet = new HashSet<>();
        for (String card : cards) {
            if (cardSet.contains(card)) {
                duplicateCards.add(card);
            } else {
                cardSet.add(card);
            }
        }
        return duplicateCards;
    }

    /**
     * Return a list of malformed cards, that aren't two letter strings with a
     * rank character (A23456789TJQK) and a suit character (cdhs).
     *
     * @param cards a list of cards
     * @return a list of the malformed cards in the given card list
     */
    private List<String> malformedCards(List<String> cards) {
        List<String> malformedCards = new ArrayList<>();
        for (String card : cards) {
            if ((card == null) ||
                    (card.length() != 2) ||
                    ("A23456789TJQK".indexOf(card.charAt(0)) == -1) ||
                    ("cdhs".indexOf(card.charAt(1)) == -1)) {
                malformedCards.add(card);
            }
        }
        return malformedCards;
    }

    /**
     * Given a list of cards, return a single String containing the cards dealt out
     * in a Pyramid Solitaire pattern.  It contains the 28 card pyramid, and a list
     * of the rest of the cards starting from the top of the deck pile to the bottom.
     * It expects a full deck of 52 or close to it.
     *
     * @param cards a list of cards
     * @return a String representation of the cards laid out in a Pyramid Solitaire game setup
     */
    String pyramidString(List<String> cards) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12 - (i * 2); j++) {
                sb.append(" ");
            }
            for (int j = 0; j < i + 1; j++) {
                sb.append(cards.get(count));
                if (j < i) {
                    sb.append("  ");
                }
                count++;
            }
            sb.append("\n");
        }
        while (count < cards.size()) {
            sb.append(cards.get(count));
            count++;
            if (count < 52) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}
