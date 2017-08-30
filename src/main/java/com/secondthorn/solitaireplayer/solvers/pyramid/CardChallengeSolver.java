package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Pyramid Solitaire Card Challenge solver.
 * <p>
 * Card challenges take the form: "Clear N [Rank] in M deals", e.g. "Clear 12 Aces in 2 deals".
 * This solver is the most complicated because sometimes there isn't one right solution.  There may
 * be games where you can either clear the board and clear few cards of the relevant rank, or
 * remove more cards of the relevant rank but get stuck and not be able to clear the board.  The
 * only way to know the correct choice is to know what the cards in the next game is.
 * <p>
 * This solver will look for two solutions: one where you remove as many goal cards as possible but clear the board,
 * and one where you remove as many goal cards as possible but don't clear the board.
 * <p>
 * If it can reach the goal without clearing the board, it will figure out how to do so in the minimum number of steps.
 * <p>
 * Otherwise, if it's impossible to clear the board, it will just maximize removing goal cards.
 * <p>
 * If it is possible to clear the board, and it is the better solution, it will just return that solution.
 * <p>
 * When it's not clear what's a better solution, it will return both solutions for the player to decide.
 */
public class CardChallengeSolver implements PyramidSolver {
    private int numCardsToClear;
    private char cardRankToClear;
    private int cardRankValueToClear;

    private Node goalReachedNode;
    private int goalReachedNodeScore;
    private Node bestClearNode;
    private int bestClearNodeScore;
    private Node bestNonClearNode;
    private int bestNonClearNodeScore;

    public CardChallengeSolver(int goalNumCardsToClear, char cardRankToClear, int currentNumCardsCleared) {
        if (currentNumCardsCleared > goalNumCardsToClear) {
            throw new IllegalArgumentException("The current number of cards cleared must be smaller than the goal");
        }
        this.numCardsToClear = goalNumCardsToClear - currentNumCardsCleared;
        this.cardRankToClear = cardRankToClear;
        this.cardRankValueToClear = "A23456789TJQK".indexOf(cardRankToClear) + 1;
    }

    /**
     * Find the best solution(s) to solve Card Challenges, maximizing removing cards of a certain rank.
     * This uses Breadth-First Search without detecting unwinnable states, because instead of
     * searching for a specific state or type of state, we're trying to maximize something.  So we need
     * to search exhaustively.
     *
     * @param deck a standard deck of 52 cards
     * @return a solution if one best one exists, or two if it can't determine the best
     */
    public Map<String, List<Action>> solve(Deck deck) {
        Map<String, List<Action>> solutions = new HashMap<>();
        Deque<Node> fringe = new ArrayDeque<>();
        TLongSet seenStates = new TLongHashSet();
        long state = State.INITIAL_STATE;
        Node node = new Node(state, null);
        fringe.add(node);

        goalReachedNode = null;
        goalReachedNodeScore = 0;
        bestClearNode = null;
        bestClearNodeScore = 0;
        bestNonClearNode = null;
        bestNonClearNodeScore = 0;

        while (!fringe.isEmpty()) {
            node = fringe.remove();
            state = node.getState();
            StateCache stateCache = deck.getStateCache(State.getPyramidFlags(state));
            int score = numCardsOfRankRemoved(state, cardRankValueToClear, deck);
            if (score == numCardsToClear) {
                goalReachedNode = node;
                goalReachedNodeScore = score;
                break;
            }
            if (stateCache.isPyramidClear()) {
                if (score > bestClearNodeScore) {
                    bestClearNode = node;
                    bestClearNodeScore = score;
                    if (bestSolutionsFound(bestClearNodeScore, bestNonClearNodeScore)) {
                        break;
                    }
                }
            } else {
                TLongList successors = stateCache.getSuccessors(state);
                if (successors.size() == 0) {
                    if (score > bestNonClearNodeScore) {
                        bestNonClearNode = node;
                        bestNonClearNodeScore = score;
                        if (bestSolutionsFound(bestClearNodeScore, bestNonClearNodeScore)) {
                            break;
                        }
                    }
                } else {
                    for (int i = 0, len = successors.size(); i < len; i++) {
                        long nextState = successors.get(i);
                        if (!seenStates.contains(nextState)) {
                            seenStates.add(nextState);
                            Node nextNode = new Node(nextState, node);
                            fringe.add(nextNode);
                        }
                    }
                }
            }
        }

        if (goalReachedNode != null) {
            addGoalReachedNode(deck, solutions);
        } else {
            if (bestClearNode == null) {
                if (bestNonClearNode != null) {
                    addNonClearNode(deck, solutions);
                }
            } else {
                if (bestNonClearNode == null) {
                    addClearNode(deck, solutions);
                } else {
                    if (bestClearNodeScore >= bestNonClearNodeScore) {
                        addClearNode(deck, solutions);
                    } else {
                        addClearNode(deck, solutions);
                        addNonClearNode(deck, solutions);
                    }
                }
            }
        }

        return solutions;
    }

    /**
     * For a given state and card rank, count how many cards of that rank have been removed.
     *
     * @param state     a long value for the Pyramid Solitaire state
     * @param rankValue a char card rank value (1 - 13)
     * @param deck      the Deck of cards being played
     * @return the number of cards of that rank that have been removed in the state
     */
    private int numCardsOfRankRemoved(long state, int rankValue, Deck deck) {
        // using Kernighan's method in The C Programming Language 2nd Ed. Exercise 2-9 to count set bits
        int numCardsRemoved = 4;
        long remainingCardFlags = state & deck.cardRankMask(rankValue);
        while (remainingCardFlags != 0) {
            remainingCardFlags &= remainingCardFlags - 1;
            numCardsRemoved--;
        }
        return numCardsRemoved;
    }

    /**
     * Generate a solution description message fragment with proper pluralization.
     *
     * @param numCardsCleared number of cards of goal rank cleared by a solution
     * @param numSteps        number of steps in the solution
     * @return a description of how many goal cards removed in how many steps
     */
    private String removeMessage(int numCardsCleared, int numSteps) {
        String cards = (numCardsCleared == 1) ? "card" : "cards";
        String steps = (numSteps == 1) ? "step" : "steps";
        return "remove " + numCardsCleared + " " + cards + " of rank " + cardRankToClear + " in " + numSteps +
                " " + steps + ".";
    }

    private void addGoalReachedNode(Deck deck, Map<String, List<Action>> solutions) {
        List<Action> solution = goalReachedNode.actions(deck);
        String description = "Reach goal, " + removeMessage(goalReachedNodeScore, solution.size());
        solutions.put(description, solution);
    }

    private void addNonClearNode(Deck deck, Map<String, List<Action>> solutions) {
        List<Action> solution = bestNonClearNode.actions(deck);
        String description = "Without clearing the board, " + removeMessage(bestNonClearNodeScore, solution.size());
        solutions.put(description, solution);
    }

    private void addClearNode(Deck deck, Map<String, List<Action>> solutions) {
        List<Action> solution = bestClearNode.actions(deck);
        String description = "Clear the board, " + removeMessage(bestClearNodeScore, solution.size());
        solutions.put(description, solution);
    }

    /**
     * For convenience, check if you've already found the best possible solutions for both
     * clearing the board and not clearing the board (all four cards removed in the best
     * solutions).  If true, we don't need to keep looking for solutions.
     *
     * @param bestClearNodeScore    the number of goal cards removed for the best solution that clears the board
     * @param bestNonClearNodeScore the number of goal cards removed for the best solution that didn't clear the board
     * @return true if we already found the best possible solutions
     */
    private boolean bestSolutionsFound(int bestClearNodeScore, int bestNonClearNodeScore) {
        return (bestClearNodeScore == 4) && (bestNonClearNodeScore == 4);
    }

}
