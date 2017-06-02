package com.secondthorn.solitaireplayer.solvers.pyramid;

import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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

    public CardChallengeSolver(int goalNumCardsToClear, char cardRankToClear, int currentNumCardsCleared) {
        if (currentNumCardsCleared > goalNumCardsToClear) {
            throw new IllegalArgumentException("The current number of cards cleared must be smaller than the goal");
        }
        this.numCardsToClear = goalNumCardsToClear - currentNumCardsCleared;
        this.cardRankToClear = cardRankToClear;
    }

    public List<List<Action>> solve(Deck deck) {
        List<List<Action>> solutions = new ArrayList<>();
        Deque<Node> fringe = new ArrayDeque<>();
        TLongSet seenStates = new TLongHashSet();
        long state = State.INITIAL_STATE;
        Node node = new Node(state, null);
        fringe.add(node);

        Node goalReachedNode = null;
        Node bestClearNode = null;
        int bestClearNodeScore = 0;
        Node bestNonClearNode = null;
        int bestNonClearNodeScore = 0;

        while (fringe.size() > 0) {
            node = fringe.remove();
            state = node.getState();
            int score = State.numCardsOfRankRemoved(state, cardRankToClear, deck);
            if (score == numCardsToClear) {
                goalReachedNode = node;
                break;
            }
            if (State.isTableClear(state)) {
                if (score > bestClearNodeScore) {
                    bestClearNode = node;
                    bestClearNodeScore = score;
                    if (bestSolutionsFound(bestClearNodeScore, bestNonClearNodeScore)) {
                        break;
                    }
                }
            } else {
                TLongList successors = State.successors(state, deck);
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
            solutions.add(goalReachedNode.actions(deck));
        } else {
            if (bestClearNode == null) {
                if (bestNonClearNode != null) {
                    solutions.add(bestNonClearNode.actions(deck));
                }
            } else {
                if (bestNonClearNode == null) {
                    solutions.add(bestClearNode.actions(deck));
                } else {
                    if (bestClearNodeScore >= bestNonClearNodeScore) {
                        solutions.add(bestClearNode.actions(deck));
                    } else {
                        solutions.add(bestClearNode.actions(deck));
                        solutions.add(bestNonClearNode.actions(deck));
                    }
                }
            }
        }

        return solutions;
    }

    /**
     * For convenience, check if you've already found the best possible solutions for both
     * clearing the board and not clearing the board (all four cards removed in the best
     * solutions).  If true, we don't need to keep looking for solutions.
     * @param bestClearNodeScore the number of goal cards removed for the best solution that clears the board
     * @param bestNonClearNodeScore the number of goal cards removed for the best solution that didn't clear the board
     * @return true if we already found the best possible solutions
     */
    private boolean bestSolutionsFound(int bestClearNodeScore, int bestNonClearNodeScore) {
        return (bestClearNodeScore == 4) && (bestNonClearNodeScore == 4);
    }

}
