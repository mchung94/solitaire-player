package com.secondthorn.solitaireplayer.players;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;
import org.sikuli.basics.Animator;
import org.sikuli.basics.AnimatorOutQuarticEase;
import org.sikuli.basics.AnimatorTimeBased;
import org.sikuli.basics.Settings;
import org.sikuli.script.App;
import org.sikuli.script.support.IRobot;
import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Region;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the Windows 10 Microsoft Solitaire Collection window and supports all interactions with it, in particular
 * searching for images within the window and moving the mouse and clicking on things in the window. Methods that
 * involve clicking throw InterruptedException because of sleep() calls after the click. This is so interruptions can
 * exit the program instead of being ignored.
 */
public abstract class MSCWindow {
    /**
     * An image to detect if the OK dialog popped up to confirm undoing the board.
     */
    private org.sikuli.script.Image okDialogImage;

    /**
     * The location of the Undo Board buttons.
     */
    private Image undoBoardImage = loadImage("Common/UndoBoard.png");

    /**
     * The location of the Undo Board button, used when the first one doesn't exist.
     */
    private Image undoBoardImage2 = loadImage("Common/UndoBoard2.png");
    
    /**
     * The location of the OK button to confirm undoing the board.
     */
    private Region okButton = new Region(529, 547, 41, 23);

    /**
     * A mapping between images and a rank character - multiple images may map to the same rank.
     */
    private Map<org.sikuli.script.Image, Character> rankImages;

    /**
     * A mapping between images and a suit character - multiple images may map to the same suit.
     */
    private Map<Image, Character> suitImages;

    /**
     * Regions of the screen to interact with for searching for images and clicking. These are loaded from various
     * regions.json files in the resources directory.
     */
    protected Regions regions;

    private static final int WINDOW_WIDTH = 1440;
    private static final int WINDOW_HEIGHT = 900;
    private static final String WINDOW_SIZE = "" + WINDOW_WIDTH + "x" + WINDOW_HEIGHT;

    /**
     * Common setup for subclasses - since this class is abstract and cannot be instantiated, the subclasses must call
     * super to run this initialization code.
     *
     * @param gameName the directory under src/main/resources/ for the game being played
     * @throws PlayException if there is a problem during resource loading or interaction with the game window
     */
    protected MSCWindow(String gameName) throws InterruptedException, PlayException {
        positionForPlay();
        systemReport();
        Settings.InputFontSize = 14;
        okDialogImage = loadImage("Common/OKDialog.png");
        String gameResourceDir = gameName + "/";
        validateGameBeingPlayed(gameName);
        rankImages = loadCharacterImages(gameResourceDir, "A23456789TJQK");
        suitImages = loadCharacterImages(gameResourceDir, "cdhs");
        regions = RegionDeserializer.createRegions(gameResourceDir + "regions.json");
    }

    private void validateGameBeingPlayed(String gameName) throws InterruptedException, PlayException {
        String gameResourceDir = gameName + "/";
        Image gameImage = loadImage(gameResourceDir + "Game.png");
        if (appRegion().exists(gameImage, 0.0d) == null) {
            // we don't see Game.png, try one more time with BonusGame.png
            gameImage = loadImage(gameResourceDir + "BonusGame.png");
            if (appRegion().exists(gameImage, 0.0d) == null) {
                throw new PlayException("Can't detect if we're playing a game of " + gameName + " Solitaire.");
            }
        }
    }

    /**
     * Print the primary screen size according to the given DPI Awareness value.
     * The DPI_AWARENESS_CONTEXT determines how the size is reported: if
     * UNAWARE, then the sizes are scaled by their DPI, and if it's
     * PER_MONITOR_AWARE_V2, then it will return the monitor's actual size.
     * @param context a DPI_AWARENESS_CONTEXT
     * @return a String containing the width/height of the primary monitor in pixels
     */
    private String primaryScreenSize(DPI_AWARENESS_CONTEXT context) {
        DPI_AWARENESS_CONTEXT oldContext = DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(context);
        int width = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CXSCREEN);
        int height = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CYSCREEN);
        DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(oldContext);
        return width + "x" + height;
    }

    /**
     * Print out a system report that users can report to developers to
     * investigate issues with the program not working.
     */
    private void systemReport() {
        String sizeString = getSizeString();
        WinDef.UINT dpi = DpiUser32.INSTANCE.GetDpiForWindow(getHWND());
        System.out.println("System Report:");
        System.out.println("    Window Size (after adjustment) = " + sizeString);
        System.out.println("    Expected Window Size = " + WINDOW_SIZE);
        System.out.println("    DPI = " + dpi);
        System.out.println("    Expected DPI = 96");
        System.out.println("    Scaling Factor = " + (100 * dpi.longValue() / 96.0) + "%");
        String unaware = primaryScreenSize(DPI_AWARENESS_CONTEXT.UNAWARE);
        String actual = primaryScreenSize(DPI_AWARENESS_CONTEXT.PER_MONITOR_AWARE_V2);
        System.out.println("    Primary Screen Size (Adjusted for Scaling Factor) = " + unaware);
        System.out.println("    Expected Primary Screen Size = " + actual);
        System.out.println("If the scaling factor isn't 100% or the values don't match the expected,");
        System.out.println("then this program may not work properly.");
    }

    /**
     * Resets the game to the beginning if the option is available, otherwise do nothing.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException        if there's a problem clicking on the Undo Board / OK buttons
     */
    public void undoBoard() throws InterruptedException, PlayException {
        if (!clickImage(undoBoardImage, 1.0d)) {
            clickImage(undoBoardImage2, 1.0d);
        }
        if (appRegion().exists(okDialogImage, 3.0d) != null) {
            clickRegion(okButton);
        }
        Thread.sleep(3000);
    }

    /**
     * Instantly move the mouse cursor to the given (X, Y) virtual screen coordinates. The virtual screen is a
     * bounding box around all the monitors, where (0, 0) is the top left corner of the primary monitor, not the
     * virtual screen.  So virtual screen coordinates can be negative.
     * <p>
     * Cursor movement already exists in the java.awt.Robot class but with JDK 8 it has issues with multi-monitor and
     * HiDPI setups.  This is a workaround until we can require users to have JDK 11.
     *
     * @param x the virtual screen X coordinate to move the mouse cursor to
     * @param y the virtual screen Y coordinate to move the mouse cursor to
     */
    public void moveMouse(int x, int y) {
        final int MOUSEEVENTF_MOVE = 0x0001;
        final int MOUSEEVENTF_ABSOLUTE = 0x8000;
        final int MOUSEEVENTF_VIRTUALDESK = 0x4000;

        DPI_AWARENESS_CONTEXT oldContext = DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT.PER_MONITOR_AWARE_V2);
        int left = User32.INSTANCE.GetSystemMetrics(WinUser.SM_XVIRTUALSCREEN);
        int top = User32.INSTANCE.GetSystemMetrics(WinUser.SM_YVIRTUALSCREEN);
        int width = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CXVIRTUALSCREEN);
        int height = User32.INSTANCE.GetSystemMetrics(WinUser.SM_CYVIRTUALSCREEN);

        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE);
        input.input.setType("mi");
        input.input.mi.dx = new WinDef.LONG((((x - left) * 65536L) / width) + 1);
        input.input.mi.dy = new WinDef.LONG((((y - top) * 65536L) / height) + 1);
        input.input.mi.mouseData = new WinDef.DWORD(0);
        input.input.mi.dwFlags = new WinDef.DWORD(MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_VIRTUALDESK);
        input.input.mi.time = new WinDef.DWORD(0);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

        DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(oldContext);
    }

    /**
     * Moves the Microsoft Solitaire Collection window to the foreground, undo minimize/maximize, and resize it.
     */
    public void positionForPlay() throws InterruptedException, PlayException {
        WinDef.HWND hwnd = getHWND();
        if ((hwnd == null) || !showWindow(hwnd) || !moveWindow(hwnd) || !setForegroundWindow(hwnd)) {
            throw new PlayException("Unable to find, move, or show the Microsoft Solitaire Collection window.");
        }
        Thread.sleep(250);
    }

    /**
     * Guesses the card at the given region.  It may be wrong and it may return "??" to represent an unknown card.
     *
     * @param region the region containing the card rank and suit images (upper left corner)
     * @return a guess at what the card is at the region of the screen
     */
    protected String cardAt(Region region) {
        Character rank = bestCardCharacter(region, rankImages);
        Character suit = bestCardCharacter(region, suitImages);
        if (rank != null && suit != null) {
            String card = rank.toString() + suit.toString();
            // convenient place to hardcode card recognition mistakes
            return card;
        }
        return "??";
    }

    /**
     * Returns an image from inside the src/main/resources directory.  This will work for both loading the file from
     * the filesystem or from a jar file.
     *
     * @param filename the path within the src/main/resources directory for the image file to load
     * @return an Image from the file
     */
    protected Image loadImage(String filename) {
        return Image.create(ClassLoader.getSystemResource(filename));
    }

    /**
     * Clicks the primary mouse button on the center of the SikuliX region.
     * <p>
     * SikuliX already has a region click but due to mouse movement issues with JDK 8 and multi-monitor / HiDPI setups,
     * it is currently re-implemented here using a custom mouse movement method.  This may be revisited after we begin
     * to require JDK 11.
     *
     * @param region the region to click on
     * @throws InterruptedException if the thread is interrupted
     */
    protected void clickRegion(Region region) throws InterruptedException, PlayException {
        Location center = region.getCenter();
        moveMouseSmoothly(center.x, center.y);
        IRobot robot = center.getScreen().getRobot();
        robot.mouseDown(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseUp(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
        Thread.sleep(400);
    }

    /**
     * If the image exists on the Microsoft Solitaire Collection window, click on it.  The timeout a time limit on how
     * many seconds to wait for the image to appear.  Using zero timeout for example will do nothing unless the image
     * already exists on the screen.
     *
     * @param image   an image to search for and click on within the game window
     * @param timeout a time limit to wait for the image to appear if it isn't already there
     * @return true or false to indicate if it successfully found and clicked on the image
     * @throws InterruptedException if the thread is interrupted
     * @throws PlayException        if there was a problem searching for the image clicking on it
     */
    protected boolean clickImage(Image image, double timeout) throws InterruptedException, PlayException {
        Match match = appRegion().exists(image, timeout);
        if (match != null) {
            clickRegion(match);
            return true;
        }
        return false;
    }

    /**
     * Returns a mapping between images and the rank / suit characters they represent.  Multiple images may map to
     * the same character.
     */
    private Map<Image, Character> loadCharacterImages(String resourceDir, String characters) throws PlayException {
        Map<Image, Character> images = new HashMap<>();
        for (char c : characters.toCharArray()) {
            for (String path : characterImageFilenames(resourceDir, c)) {
                images.put(loadImage(path), c);
            }
        }
        return images;
    }

    /**
     * Return the SikuliX region representing the Microsoft Solitaire Collection window location.
     */
    protected Region appRegion() throws InterruptedException, PlayException {
        positionForPlay();
        return App.focusedWindow();
    }

    /**
     * Returns the best card rank / suit character for the card in the given region.
     */
    private Character bestCardCharacter(Region region, Map<Image, Character> imageToCharacter) {
        Match match = region.findBest(imageToCharacter.keySet().toArray());
        if (match == null) {
            return null;
        }
        return imageToCharacter.get(match.getImage());
    }

    /**
     * Returns a list of all the files in the src/main/resources/[game]/[resolution]/[rank or suit] directory.
     * These are image files of the card rank or suit, used for guessing what cards are face up on the game window.
     */
    private java.util.List<String> characterImageFilenames(String resourceDir, char c) throws PlayException {
        String imageDirectory = resourceDir + c + "/";
        try {
            URI uri = ClassLoader.getSystemResource(imageDirectory).toURI();
            if (uri.getScheme().equals("jar")) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                     DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath(imageDirectory))) {
                    return filesFromDirectoryStream(ds);
                }
            } else {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(uri))) {
                    return filesFromDirectoryStream(ds);
                }
            }
        } catch (URISyntaxException ex) {
            throw new PlayException("Unable to find resource directory for card-related character " + c, ex);
        } catch (IOException ex) {
            throw new PlayException("Unable to find image files for the card-related character " + c, ex);
        }
    }

    /**
     * Returns a list of files from a jar or file system directory.
     */
    private java.util.List<String> filesFromDirectoryStream(DirectoryStream<Path> ds) {
        List<String> filenames = new ArrayList<>();
        for (Path path : ds) {
            int nameCount = path.getNameCount();
            filenames.add(path.subpath(nameCount - 3, nameCount).toString());
        }
        return filenames;
    }

    /**
     * Returns the size in [width]x[height] format for the Microsoft Solitaire Collection window.
     */
    private String getSizeString() {
        DPI_AWARENESS_CONTEXT oldContext = DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT.PER_MONITOR_AWARE_V2);
        WinDef.HWND hwnd = getHWND();
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(oldContext);
        return (rect.right - rect.left) + "x" + (rect.bottom - rect.top);
    }

    /**
     * Represents Windows DPI awareness contexts - here it is used to see the true resolution of the display.
     */
    public static class DPI_AWARENESS_CONTEXT extends WinNT.HANDLE {
        static final DPI_AWARENESS_CONTEXT UNAWARE = new DPI_AWARENESS_CONTEXT(-1);
        static final DPI_AWARENESS_CONTEXT SYSTEM_AWARE = new DPI_AWARENESS_CONTEXT(-2);
        static final DPI_AWARENESS_CONTEXT PER_MONITOR_AWARE = new DPI_AWARENESS_CONTEXT(-3);
        static final DPI_AWARENESS_CONTEXT PER_MONITOR_AWARE_V2 = new DPI_AWARENESS_CONTEXT(-4);
        static final DPI_AWARENESS_CONTEXT UNAWARE_GDISCALED = new DPI_AWARENESS_CONTEXT(-5);

        public DPI_AWARENESS_CONTEXT() {
            // do nothing; a public no-arg constructor is required
        }

        DPI_AWARENESS_CONTEXT(long value) {
            super(Pointer.createConstant(value));
        }
    }

    /**
     * Additional functions from user32.dll used by this program.
     */
    private interface DpiUser32 extends User32 {
        DpiUser32 INSTANCE = Native.load("user32", DpiUser32.class, W32APIOptions.DEFAULT_OPTIONS);

        /**
         * Sets the thread's DPI awareness to a new setting and returns the previous setting.
         * This is to see the display's true resolution instead of a scaled resolution.
         */
        DPI_AWARENESS_CONTEXT SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT context);
        WinDef.UINT GetDpiForWindow(WinDef.HWND hwnd);
    }

    /**
     * Returns the handle to the Microsoft Solitaire Collection window, so other actions can be performed on it.
     */
    private WinDef.HWND getHWND() {
        String[] windowNames = {
                "Solitaire & Casual Games",
                "Solitaire Collection"
        };
        for (String windowName : windowNames) {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow("ApplicationFrameWindow", windowName);
            if (hwnd != null) {
                return hwnd;
            }
        }
        return null;
    }

    /**
     * Moves the Microsoft Solitaire Collection window to the (0, 0) virtual screen coordinates (the top-left corner
     * of the primary display) and resizes it.  Depending on the display scaling factor, the actual resolution
     * afterwards may be different.
     */
    private boolean moveWindow(WinDef.HWND hwnd) {
        return User32.INSTANCE.MoveWindow(hwnd, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, true);
    }

    /**
     * Activates and displays the Microsoft Solitaire Collection window.  Undoes any minimize/maximize setting
     * currently on the window.
     */
    private boolean showWindow(WinDef.HWND hwnd) {
        return User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
    }

    /**
     * Activates the window and directs keyboard input to it.
     */
    private boolean setForegroundWindow(WinDef.HWND hwnd) {
        return User32.INSTANCE.SetForegroundWindow(hwnd);
    }

    /**
     * Returns the location of the mouse cursor in virtual screen coordinates.
     */
    private Point getMousePosition() {
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi != null) {
            return MouseInfo.getPointerInfo().getLocation();
        }
        return null;
    }

    /**
     * Smoothly move the mouse to the given (X, Y) virtual screen coordinates over 500 milliseconds.
     * This program never intentionally moves the mouse to (0, 0), so if the mouse is moved there
     * by the user, use this as a signal to abort the program.
     *
     * @param x the X coordinate to move to
     * @param y the Y coordinate to move to
     */
    private void moveMouseSmoothly(int x, int y) throws PlayException {
        Point p = getMousePosition();
        if (p != null) {
            if ((p.x == 0) && (p.y == 0)) {
                throw new PlayException("Mouse moved to (0, 0) to abort the program.");
            }
            Animator aniX = new AnimatorTimeBased(new AnimatorOutQuarticEase((float) p.x, (float) x, 500));
            Animator aniY = new AnimatorTimeBased(new AnimatorOutQuarticEase((float) p.y, (float) y, 500));
            while (aniX.running()) {
                moveMouse((int) aniX.step(), (int) aniY.step());
            }
        }
    }
}
