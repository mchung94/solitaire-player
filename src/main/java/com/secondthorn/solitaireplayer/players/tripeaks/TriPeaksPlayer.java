package com.secondthorn.solitaireplayer.players.tripeaks;

import com.secondthorn.solitaireplayer.players.PlayException;
import com.secondthorn.solitaireplayer.players.SolitairePlayer;
import com.secondthorn.solitaireplayer.solvers.tripeaks.Action;
import com.secondthorn.solitaireplayer.solvers.tripeaks.BoardChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.tripeaks.CardChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.tripeaks.CardRevealingSolver;
import com.secondthorn.solitaireplayer.solvers.tripeaks.Deck;
import com.secondthorn.solitaireplayer.solvers.tripeaks.ScoreChallengeSolver;
import com.secondthorn.solitaireplayer.solvers.tripeaks.Solution;
import com.secondthorn.solitaireplayer.solvers.tripeaks.State;
import com.secondthorn.solitaireplayer.solvers.tripeaks.TriPeaksSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.sikuli.script.Sikulix.popAsk;
import static org.sikuli.script.Sikulix.popSelect;

/**
 * Guides the user through a game of TriPeaks Solitaire. It interacts with the Microsoft Solitaire Collection window as
 * well as the user, from the start of the game until a solution is played or reported.
 */
public class TriPeaksPlayer extends SolitairePlayer {
    private TriPeaksSolver solver;

    public TriPeaksPlayer(String[] args) {
        String challenge = args[0];
        switch (challenge) {
            case "Board":
                solver = new BoardChallengeSolver();
                System.out.println("Starting a TriPeaks Solitaire Board Challenge...");
                break;
            case "Score":
                int goalScore = (args.length > 1) ? parseInt(args[1]) : ScoreChallengeSolver.MAX_POSSIBLE_SCORE;
                int currentScore = (args.length > 2) ? parseInt(args[2]) : 0;
                solver = new ScoreChallengeSolver(goalScore, currentScore);
                System.out.print("Starting a TriPeaks Solitaire Score Challenge: ");
                if (args.length == 1) {
                    System.out.println("find the maximum possible score...");
                } else {
                    System.out.println("go from " + currentScore + " to " + goalScore + " points...");
                }
                break;
            case "Card":
                if (args.length != 4) {
                    throw new IllegalArgumentException("Wrong number of args for TriPeaks Solitaire Card Challenge");
                }
                int goalNumCardsToClear = parseInt(args[1]);
                char cardRankToClear = parseCardRank(args[2]);
                int currentNumCardsCleared = parseInt(args[3]);
                solver = new CardChallengeSolver(goalNumCardsToClear, cardRankToClear, currentNumCardsCleared);
                System.out.print("Starting a TriPeaks Solitaire Card Challenge: ");
                System.out.print("clear " + goalNumCardsToClear + " cards of rank " + cardRankToClear);
                System.out.println(", with " + currentNumCardsCleared + " cleared so far");
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
        CardRevealingSolver cardRevealingSolver = new CardRevealingSolver();
        System.out.println("Looking up which cards are on the board and in the stock pile.");
        System.out.println("Afterwards, please check and verify the cards it detected.");
        Deck deck = fillLastCard(verifyCards(scanCardsOnScreen(window)));
        window.undoBoard();
        int state = State.INITIAL_STATE;
        System.out.println("Now searching for a solution...");
        Solution solution = solver.solve(deck, State.INITIAL_STATE);
        while (!solution.isDefinitiveSolution()) {
            System.out.println("No definite solution found yet - searching for a way to reveal face down cards...");
            Solution cardRevealingSolution = cardRevealingSolver.solve(deck, state);
            if (cardRevealingSolution.getActions().size() > 0) {
                Thread.sleep(1000);
                playSolution(cardRevealingSolution, window);
                deck = updateDeck(deck, window);
                state = cardRevealingSolution.getEndingState();
                solution = solver.solve(deck, State.INITIAL_STATE);
            } else {
                System.out.println("No way to turn over face down cards, try a non-definitive solution...");
                break;
            }
        }
        printSolution(solution);
        String confirmMessage = String.format("Press Yes to play or No to quit.\nSolution: %s\n",
                solution.getDescription());
        if (!popAsk(confirmMessage, "Play the solution?")) {
            throw new PlayException("User cancelled selecting and playing a solution.");
        }
        window.undoBoard();
        playSolution(solution, window);
    }

    /**
     * Prints out the solution(s) but doesn't do any SikuliX-based automation to play the game.
     *
     * @throws PlayException if the user cancels while the program is verifying the deck of cards
     */
    @Override
    public void preview(String filename) throws PlayException {
        List<String> cards = readCardsFromFile(filename);
        Deck deck = new Deck(verifyCards(cards));
    }

    /**
     * Returns true if the list of cards works for playing TriPeaks Solitaire.  It must be a standard 52 card deck,
     * but the first 18 tableau cards may be unknown.  The stock pile should be known before the game starts.
     *
     * @param cards           a list of cards to check
     * @param missing         the cards in the list that are missing from the standard 52 card deck
     * @param duplicates      the cards in the list that are duplicates, not including unknown cards
     * @param malformed       the cards in the list that are not cards and not unknown cards (garbage input)
     * @param numUnknownCards the number of unknown cards, represented by "??""
     * @return true if the list of cards is suitable for playing TriPeaks Solitaire.
     */
    @Override
    protected boolean isValidCards(List<String> cards,
                                   List<String> missing,
                                   List<String> duplicates,
                                   List<String> malformed,
                                   long numUnknownCards) {
        return ((cards.size() == 52) &&
                (0 <= missing.size()) &&
                (missing.size() <= 18) &&
                (duplicates.size() == 0) &&
                (malformed.size() == 0) &&
                (0 <= numUnknownCards) &&
                (numUnknownCards <= 18));
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
    @Override
    protected String cardsToString(List<String> cards) {
        return String.format(
                "      %s          %s          %s\n" +
                        "    %s  %s      %s  %s      %s  %s\n" +
                        "  %s  %s  %s  %s  %s  %s  %s  %s  %s\n" +
                        "%s  %s  %s  %s  %s  %s  %s  %s  %s  %s\n" +
                        "%s\n" +
                        "%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
                cards.toArray()
        );
    }

    /**
     * Looks through all the cards in the game and returns a list of the cards seen. The cards may be wrong and must
     * be verified and corrected by the user. Initially the game starts with the first 18 cards face down and these
     * are represented by "??" for unknown card.
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
     * Rescan any unknown tableau cards which may now be face up, and ask the user to verify the cards.
     */
    private Deck updateDeck(Deck deck, TriPeaksWindow window) throws PlayException {
        List<String> cards = new ArrayList<>(deck.getCards());
        for (int i = 0; i < 18; i++) {
            if (deck.isUnknownCard(i)) {
                cards.set(i, window.cardAtTableau(i));
            }
        }
        return fillLastCard(verifyCards(cards));
    }

    /**
     * If there's only one unknown card, replace it with the last missing card if possible.
     */
    private Deck fillLastCard(List<String> cards) {
        List<String> missing = missingCards(cards);
        int[] unknownCardIndexes = unknownCardIndexes(cards);
        if ((missing.size() == 1) && (unknownCardIndexes.length == 1)) {
            cards.set(unknownCardIndexes[0], missing.get(0));
        }
        return new Deck(cards);
    }

    /**
     * Returns the deck indexes of the unknown cards in the list of cards.
     */
    private int[] unknownCardIndexes(List<String> cards) {
        return IntStream.range(0, cards.size()).filter(i -> cards.get(i).equals(Deck.UNKNOWN_CARD)).toArray();
    }

    /**
     * Given a Solution, control the cursor and play them in the Microsoft Solitaire Collection window.
     */
    private void playSolution(Solution solution, TriPeaksWindow window) throws InterruptedException, PlayException {
        window.positionForPlay();
        for (Action action : solution.getActions()) {
            switch (action.getCommand()) {
                case DRAW:
                    window.clickStockCard();
                    break;
                case REMOVE:
                    window.clickTableauCard(action.getDeckIndex());
                    break;
                case UNDO_BOARD:
                    window.undoBoard();
                    break;
            }
        }
    }

    private void printSolution(Solution solution) {
        System.out.println(solution.getDescription());
        for (Action action : solution.getActions()) {
            switch (action.getCommand()) {
                case REMOVE:
                    System.out.println(String.format("Move the tableau card %s (index=%d) to the waste pile",
                            action.getCard(), action.getDeckIndex()));
                    break;
                case DRAW:
                    System.out.println("Draw a card from the stock pile");
                    break;
                case UNDO_BOARD:
                    System.out.println("Undo the Board");
                    break;
            }
        }
    }
}
