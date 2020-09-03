package com.secondthorn.solitaireplayer.solvers.tripeaks;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

public class ScoreChallengeSolver implements TriPeaksSolver {
    /**
     * The maximum possible score removing all 28 tableau cards in a single streak.
     */
    public static final int MAX_POSSIBLE_SCORE = 84900;

    /**
     * The number of points needed to reach the goal.
     */
    private int pointsNeeded;

    /**
     * Creates a Score Challenge solver to find a way to reach the goal score starting from the current score
     * @param goalScore the final score needed to win the challenge
     * @param currentScore the current score at the start of the game
     */
    public ScoreChallengeSolver(int goalScore, int currentScore) {
        pointsNeeded = goalScore - currentScore;
        if (pointsNeeded <= 0) {
            throw new IllegalArgumentException("The current score must be smaller than the goal score");
        }
    }

    /**
     * Returns the best series of steps to play for a Score Challenge. Some cards in the tableau may be unknown.
     * If the goal score can be reached, that will be the return value. Otherwise it will return the steps to reach
     * the highest possible score. If all cards are known, this will be a definitive solution, otherwise it won't be.
     * @param deck          a deck of cards for TriPeaks Solitaire
     * @param startingState the starting state for the solver to begin at
     * @return a Solution to either reach the goal, or the highest possible score otherwise
     */
    @Override
    public Solution solve(Deck deck, int startingState) {
        IntFIFOQueue fringe = new IntFIFOQueue();
        TIntIntMap seenStates = new TIntIntHashMap();
        TIntObjectMap<ScoreCache> scoreCache = new TIntObjectHashMap<>();
        scoreCache.put(startingState, new ScoreCache(0, 0));
        int bestScore = 0;
        int bestState = startingState;
        fringe.enqueue(startingState);
        while (!fringe.isEmpty()) {
            int state = fringe.dequeue();
            int score = 0;
            if (seenStates.containsKey(state)) {
                score = score(state, seenStates.get(state), scoreCache);
            }
            if (score >= pointsNeeded) {
                List<Action> actions = TriPeaksSolver.actions(state, seenStates, deck);
                String description = String.format("Gain %d points in %d steps", score, actions.size());
                return new Solution(description, true, actions, state, seenStates.get(state));
            }
            if (score > bestScore) {
                bestScore = score;
                bestState = state;
            }
            for (int nextState : State.successors(state, deck)) {
                int scoreFromPrevious = score(nextState, state, scoreCache);
                if (!seenStates.containsKey(nextState) ||
                        (scoreFromPrevious > score(nextState, seenStates.get(nextState), scoreCache))) {
                    seenStates.put(nextState, state);
                    fringe.enqueue(nextState);
                }
            }
        }
        List<Action> actions = TriPeaksSolver.actions(bestState, seenStates, deck);
        String description = String.format("Gain %d points in %d steps", bestScore, actions.size());
        return new Solution(description, !deck.hasUnknownCards(), actions, bestState, seenStates.get(bestState));
    }

    /**
     * Calculates bonus points from removing the top three tableau cards: 500 for the first removed, 1000 for the
     * second, and 5000 for the third.
     */
    private int bonusPoints(int state) {
        int tableauFlags = State.TABLEAU_FLAGS[State.getTableauIndex(state)];
        switch (tableauFlags & 0b111) {
            case 0b000:
                return 6500; // all 3 top cards removed, 500 + 1000 + 5000 bonus
            case 0b001:
            case 0b010:
            case 0b100:
                return 1500; // 2 top cards removed, 500 + 1000 bonus
            case 0b011:
            case 0b101:
            case 0b110:
                return 500; // 1 top card removed, 500 bonus
            default:
                return 0; // all top tableau cards remain, no bonus
        }
    }

    /**
     * Calculates the score of the game from the beginning until state, going through prevState just before state.
     * There may be multiple ways to get to the given state, so prevState is a necessary parameter.
     */
    private int score(int state, int prevState, TIntObjectMap<ScoreCache> scoreCache) {
        ScoreCache sc = scoreCache.get(prevState);
        int total = sc.getScore();
        int streak = sc.getStreak();
        int wasteCardIndex = State.getWasteIndex(state);
        if (wasteCardIndex < 28) {
            total += 100 + streak * 200;
            streak++;
        } else {
            streak = 0;
        }
        scoreCache.put(state, new ScoreCache(total, streak));
        return total + bonusPoints(state);
    }

    /**
     * A score cache associated with each state.  This is an optimization for score calculations so we can calculate
     * the score based on the previous state instead of calculating from the beginning of the game.
     */
    static class ScoreCache {
        private int score;
        private int streak;

        ScoreCache(int score, int streak) {
            this.score = score;
            this.streak = streak;
        }

        public int getScore() {
            return score;
        }

        public int getStreak() {
            return streak;
        }
    }
}
