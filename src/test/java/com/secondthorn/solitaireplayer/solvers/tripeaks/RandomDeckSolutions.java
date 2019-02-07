package com.secondthorn.solitaireplayer.solvers.tripeaks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Prints out solutions for the random decks.
 */
public class RandomDeckSolutions {
    public static void main(String[] args) {
        List<String> lines;
        try {
            lines = readLinesFromFile("random-decks.txt");
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
            return;
        }
        System.out.println("Board Challenge Solver");
        printSolutions(lines, new BoardChallengeSolver());
        System.out.println("Score Challenge Solver");
        printSolutions(lines, new ScoreChallengeSolver(ScoreChallengeSolver.MAX_POSSIBLE_SCORE, 0));
        System.out.println("Card Challenge Solver");
        printSolutions(lines, new CardChallengeSolver(12, 'A', 0));
    }

    private static void printSolutions(List<String> lines, TriPeaksSolver solver) {
        for (int i=0; i<lines.size(); i++) {
            Solution solution = solver.solve(new Deck(lines.get(i)), State.INITIAL_STATE);
            System.out.println(String.format("(%d \"%s\" %s)",
                    i+1, solution.getDescription(), actionsToCardList(solution.getActions())));
        }
    }

    private static List<String> readLinesFromFile(String filename) throws IOException, URISyntaxException {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(filename).toURI()));
    }

    private static String actionsToCardList(List<Action> actions) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Action action : actions) {
            switch (action.getCommand()) {
                case DRAW:
                case REMOVE:
                    sb.append("\"");
                    sb.append(action.getCard());
                    sb.append("\" ");
                    break;
                default:
                    throw new RuntimeException("Only DRAW or REMOVE should be in the solution for fully known decks");
            }
        }
        int lastIndex = sb.length() - 1;
        if (sb.charAt(lastIndex) == ' ') {
            sb.setCharAt(lastIndex, ')');
        } else {
            sb.append(")");
        }
        return sb.toString();
    }
}
