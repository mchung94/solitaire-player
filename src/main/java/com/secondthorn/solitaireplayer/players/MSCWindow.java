package com.secondthorn.solitaireplayer.players;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import static com.sun.jna.platform.win32.WinUser.SW_RESTORE;

/**
 * A class of static methods for interacting with the Windows 10 Microsoft Solitaire Collection app window.
 * <p>
 * The app should be running and at the start of a game before running the player.  If any of these methods
 * fail, there's nothing that can be done programmatically to recover, so they may throw unchecked exceptions.
 * <p>
 * Currently this lets the rest of the program make assumptions about the position and size of elements in
 * the app window, which will definitely be wrong if themes, scaling, or HiDPI are used.
 */
public final class MSCWindow {
    /**
     * The Window class name for the Windows 10 Microsoft Solitaire Collection app.
     */
    private static final String WINDOW_CLASS_NAME = "ApplicationFrameWindow";
    /**
     * The Window title for the Windows 10 Microsoft Solitaire Collection app.
     */
    private static final String WINDOW_TITLE_NAME = "Microsoft Solitaire Collection";

    private MSCWindow() {
        // do nothing, this only contains static fields/methods
    }

    /**
     * Set up the Microsoft Solitaire Collection to a consistent and known state for playing.
     * This activates the window, moves it to the upper left corner, and will resize it to 1024x768.
     */
    public static void positionForPlay() {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(WINDOW_CLASS_NAME, WINDOW_TITLE_NAME);
        if ((hwnd == null) || !showWindow(hwnd) || !moveWindow(hwnd)) {
            throw new RuntimeException("Unable to find, move, or show the Microsoft Solitaire Collection window.");
        }
    }

    /**
     * Move the window to the upper left corner and resize it to 1024x768.
     * This is to make positioning as consistent as possible, although themes,
     * scaling, or HiDPI will probably cause issues.
     *
     * @param hwnd The handle of the window to move/resize.
     * @return true if successful
     */
    private static boolean moveWindow(WinDef.HWND hwnd) {
        return User32.INSTANCE.MoveWindow(hwnd, 0, 0, 1024, 768, true);
    }

    /**
     * Activate and display the window.  If minimized or maximized, it will restore it.
     *
     * @param hwnd The handle of the window to show.
     * @return true if successful
     */
    private static boolean showWindow(WinDef.HWND hwnd) {
        return User32.INSTANCE.ShowWindow(hwnd, SW_RESTORE);
    }

}
