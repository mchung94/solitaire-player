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
import java.util.List;
import java.util.Map;

import static org.sikuli.script.Sikulix.inputText;
import static org.sikuli.script.Sikulix.popAsk;
import static org.sikuli.script.Sikulix.popSelect;

/**
 * A class that uses SikuliX to automate playing a game of Pyramid Solitaire in
 * Windows 10's Microsoft Solitaire Collection.  It can also just print out the solution
 * without playing, when given a filename containing cards to solve.
 */
public class PyramidPlayer extends SolitairePlayer {
    /**
     * When a board can't be cleared at all, the fastest list of steps to follow is to
     * just Draw/Recycle cards until you can't anymore.  One interesting thing to note is that
     * in Microsoft Solitaire Collection on Windows 10, if you keep drawing/recycling cards until you can't
     * draw or recycle anymore, the game can end, even if there's still cards on the pyramid you can still
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

    /**
     * A Pyramid Solitaire solver - figures out the solution given a deck of cards.
     */
    private PyramidSolver solver;

    /**
     * Create a new PyramidPlayer instance based on command line args.
     * It will throw IllegalArgumentException if the args don't make sense.
     * The player can specialize in either clearing the pyramid/board, maximizing score, or
     * clearing cards of a certain rank.
     *
     * @param args command line args
     */
    public PyramidPlayer(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing goal for Pyramid Solitaire");
        }
        setDeckFilename(getDeckFilenameFromArgs(args));
        if (getDeckFilename() != null) {
            args = removeDeckFilenameFromArgs(args);
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
    @Override
    public void play() throws PlayException {
        Settings.InputFontMono = true;
        if (isPreview()) {
            preview();
        } else {
            autoPlay();
        }
    }

    /**
     * Given a deck of cards in a file, just print out the solution(s) but don't do
     * any SikuliX-based automation to play the game for you.
     *
     * @throws PlayException if the user cancels while the program is verifying the deck of cards
     */
    private void preview() throws PlayException {
        List<String> cards = readCardsFromFile(getDeckFilename());
        Deck deck = buildDeck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        printSolutions(solutions);
    }

    /**
     * Automatically solve the currently displayed Microsoft Solitaire Collection
     * Pyramid Solitaire game, using SikuliX to automate the actions and scan the screen.
     *
     * @throws PlayException if there's a problem while playing the game
     */
    private void autoPlay() throws PlayException {
        // Each time we call positionForPlay(), we're making sure the MSC window is
        // in a known state and in the foreground so no weird stuff happens when SikuliX
        // is doing its work.
        MSCWindow.positionForPlay();
        Settings.InputFontSize = (int) (14 * (MSCWindow.getPercentScaling() / 100.0));
        PyramidWindow window = new PyramidWindow();
        window.undoBoard();
        MSCWindow.positionForPlay();
        List<String> cards = scanCardsOnScreen(window);
        Deck deck = buildDeck(cards);
        Map<String, List<Action>> solutions = solver.solve(deck);
        printSolutions(solutions);
        List<Action> solutionToPlay = chooseSolution(solutions);
        MSCWindow.positionForPlay();
        playSolution(solutionToPlay, window);
    }

    /**
     * Print all solutions to the given deck, according to the solver.
     * There may be more than one solution when playing a Card Challenge, so the interface is
     * always a mapping from solution description string to a list of Actions.
     *
     * @param solutions the results from the Pyramid Solitaire solver
     */
    private void printSolutions(Map<String, List<Action>> solutions) {
        for (Map.Entry<String, List<Action>> entry : solutions.entrySet()) {
            System.out.println("Solution: " + entry.getKey());
            for (Action action : entry.getValue()) {
                System.out.println("    " + action);
            }
        }
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
     */
    private List<String> scanCardsOnScreen(PyramidWindow window) {
        List<String> cards = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            cards.add(window.cardAtPyramid(i));
        }
        for (int i = 0; i < 24; i++) {
            String card = window.cardAtDeck();
            cards.add(card);
            window.draw();
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
        List<Action> solution;
        String solutionDescription;
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
                String[] keys = solutions.keySet().toArray(new String[0]);
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
     */
    private void playSolution(List<Action> solution, PyramidWindow window) {
        window.undoBoard();
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
                            case "Stock":
                                window.clickStockCard();
                                break;
                            case "Waste":
                                window.clickWasteCard();
                                break;
                            default:
                                window.clickPyramidCardIndex(Integer.parseInt(position));
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Given a list of cards, return a single String containing the cards dealt out
     * in a Pyramid Solitaire pattern.  It contains the 28 card pyramid, and a list
     * of the rest of the cards starting from the top of the stock pile to the bottom.
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
