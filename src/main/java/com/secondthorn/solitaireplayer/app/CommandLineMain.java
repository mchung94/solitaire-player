package com.secondthorn.solitaireplayer.app;

import com.secondthorn.solitaireplayer.players.SolitairePlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A command line app to automatically solve and play Microsoft Solitaire
 * Collection games on Windows 10.
 */
public class CommandLineMain {
    /**
     * The main function for the command line app.
     *
     * @param args command line arguments (described in help)
     */
    public static void main(String[] args) {
        if ((args.length > 0) && args[0].equalsIgnoreCase("help")) {
            printHelp();
            System.exit(0);
        }
        SolitairePlayer player = null;
        try {
            player = SolitairePlayer.newInstance(args);
        } catch (IllegalArgumentException ex) {
            System.err.println("Command Line Argument Problem: " + ex.getMessage());
            printUsage();
            System.exit(2);
        }
        try {
            player.play();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Print a short and simple usage message.  The command line args are complicated,
     * so it emphasizes how to print out more detailed help.
     */
    private static void printUsage() {
        System.err.println("Usage: solitaire-player.bat <Game> <Goal> [additional args for goal]");
        System.err.println("For help, run: solitaire-player.bat help");
    }

    /**
     * Print a help file (more detailed than a usage message) onto stderr.
     */
    private static void printHelp() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("help.txt")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println(line);
                }
            }
        } catch (IOException ex) {
            printUsage();
        }
    }
}
