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

import java.awt.*;

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
     * Windows GetSystemMetrics() constant to retrieve the coordinate of the left side of the virtual screen.
     */
    private static final int SM_XVIRTUALSCREEN = 76;
    /**
     * Windows GetSystemMetrics() constant to retrieve the coordinate of the top of the virtual screen.
     */
    private static final int SM_YVIRTUALSCREEN = 77;
    /**
     * Windows GetSystemMetrics() constant to retrieve the width of the virtual screen in pixels.
     */
    private static final int SM_CXVIRTUALSCREEN = 78;
    /**
     * Windows GetSystemMetrics() constant to retrieve the height of the virtual screen in pixels.
     */
    private static final int SM_CYVIRTUALSCREEN = 79;

    /**
     * Windows MOUSEINPUT flag to indicate the mouse moved.
     */
    private static final int MOUSEEVENTF_MOVE = 0x0001;
    /**
     * Windows MOUSEINPUT flag to indicate the coordinates passed in are normalized absolute coordinates (0 to 65535).
     */
    private static final int MOUSEEVENTF_ABSOLUTE = 0x8000;
    /**
     * Windows MOUSEINPUT flag to indicate the absolute coordinates passed in map to the entire virtual desktop.
     */
    private static final int MOUSEEVENTF_VIRTUALDESK = 0x4000;

    /**
     * Windows DPI_AWARENESS_CONTEXT describes the if/how aware the program is of the DPI.
     */
    public static class DPI_AWARENESS_CONTEXT extends WinNT.HANDLE {
        public static final DPI_AWARENESS_CONTEXT UNAWARE = new DPI_AWARENESS_CONTEXT(-1);
        public static final DPI_AWARENESS_CONTEXT SYSTEM_AWARE = new DPI_AWARENESS_CONTEXT(-2);
        public static final DPI_AWARENESS_CONTEXT PER_MONITOR_AWARE = new DPI_AWARENESS_CONTEXT(-3);
        public static final DPI_AWARENESS_CONTEXT PER_MONITOR_AWARE_V2 = new DPI_AWARENESS_CONTEXT(-4);

        public DPI_AWARENESS_CONTEXT() {
            // do nothing
        }

        public DPI_AWARENESS_CONTEXT(long value) {
            super(Pointer.createConstant(value));
        }
    }

    /**
     * Declares the Windows API SetThreadDpiAwarenessContext(), which changes the program's DPI awareness.
     */
    private interface DpiUser32 extends User32 {
        DpiUser32 INSTANCE = (DpiUser32) Native.loadLibrary("user32", DpiUser32.class, W32APIOptions.DEFAULT_OPTIONS);

        DPI_AWARENESS_CONTEXT SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT context);
    }

    /**
     * Return the window handle for the Microsoft Solitaire Collection window.  We call this each time
     * instead of caching it because the user may close/restart the window and we want the right handle.
     *
     * @return the Microsoft Solitaire Collection window handle
     */
    private static WinDef.HWND getHWND() {
        return User32.INSTANCE.FindWindow(WINDOW_CLASS_NAME, WINDOW_TITLE_NAME);
    }

    /**
     * Set up the Microsoft Solitaire Collection to a consistent and known state for playing.
     * This activates the window, moves it to the upper left corner, and will resize it to 1024x768.
     */
    public static void positionForPlay() throws PlayException {
        WinDef.HWND hwnd = getHWND();
        if ((hwnd == null) || !showWindow(hwnd) || !moveWindow(hwnd) || !setForegroundWindow(hwnd)) {
            throw new PlayException("Unable to find, move, or show the Microsoft Solitaire Collection window.");
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

    /**
     * Bring the window to the foreground.
     *
     * @param hwnd The handle of the window to bring to the foreground.
     * @return true if successful
     */
    private static boolean setForegroundWindow(WinDef.HWND hwnd) {
        return User32.INSTANCE.SetForegroundWindow(hwnd);
    }

    /**
     * Based on the actual window size retrieved after trying to resize it to 1024x768,
     * determine the Windows 10 scaling size (Display Settings -> Scale and Layout).
     * This works because even though we ask the window size to be set to 1024x768, the actual window size
     * afterwards is different depending on the user's scaling size setting.
     *
     * @return The percentage scaling size in Windows 10 display settings
     * @throws PlayException if there's an issue determining the scaling size
     */
    public static int getPercentScaling() throws PlayException {
        positionForPlay();
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(getHWND(), rect);
        String size = (rect.right - rect.left) + "x" + (rect.bottom - rect.top);
        int percentScaling;
        switch (size) {
            case "1024x768":
                percentScaling = 100;
                break;
            case "1026x977":
                percentScaling = 200;
                break;
            case "1282x1221":
                percentScaling = 250;
                break;
            default:
                throw new PlayException("Unsupported Windows Display Settings scaling size: it's not 100%/200%/250%");
        }
        return percentScaling;
    }

    /**
     * Move the mouse to the (x, y) pixel coordinates of the entire virtual desktop.  This is a temporary workaround
     * for mouse movement, a recent Windows update caused problems with Java Robot's mouseMove() functionality.
     * It could be related to DPI scaling or multiple monitors (or both).
     * <p>
     * Windows has a notion of a primary monitor, where (0, 0) is the upper left corner of the primary monitor's
     * screen.  The virtual screen covers all the monitors, and the upper left of it isn't necessarily (0, 0).  It
     * could have negative coordinates if the primary monitor isn't on the upper left corner of the virtual screen's
     * bounding rectangle.
     * <p>
     * In the absolute coordinate system, the upper left of the virtual screen is (0, 0) and the bottom right is
     * (65535, 65535).  So we use GetSystemMetrics() to find the bounding rectangle of the virtual screen and use that
     * to convert the virtual screen pixel coordinates into absolute coordinates which are needed to move the mouse.
     *
     * @param x the x coordinate of the location to move the mouse to
     * @param y the y coordinate of the location to move the mouse to
     */
    public static void moveMouse(int x, int y) {
        DPI_AWARENESS_CONTEXT oldContext = DpiUser32.INSTANCE.SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT.PER_MONITOR_AWARE);

        int left = User32.INSTANCE.GetSystemMetrics(SM_XVIRTUALSCREEN);
        int top = User32.INSTANCE.GetSystemMetrics(SM_YVIRTUALSCREEN);
        int width = User32.INSTANCE.GetSystemMetrics(SM_CXVIRTUALSCREEN);
        int height = User32.INSTANCE.GetSystemMetrics(SM_CYVIRTUALSCREEN);

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
     * Get the pixel coordinates the mouse's current position, or null of not found.
     *
     * @return the current position of the mouse
     */
    public static Point getMousePosition() {
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi != null) {
            return MouseInfo.getPointerInfo().getLocation();
        }
        return null;
    }

    /**
     * Move the mouse from it's current location to (x, y) in virtual screen pixel coordinates in 500ms.
     * This basically comes from SikuliX's RobotDesktop.
     *
     * @param x the x coordinate of the location to move the mouse to
     * @param y the y coordinate of the location to move the mouse to
     */
    public static void moveMouseSmoothly(int x, int y) {
        moveMouseSmoothly(x, y, 500);
    }

    private static void moveMouseSmoothly(int x, int y, long ms) {
        Point p = getMousePosition();
        if (p != null) {
            Animator aniX = new AnimatorTimeBased(new AnimatorOutQuarticEase((float) p.x, (float) x, ms));
            Animator aniY = new AnimatorTimeBased(new AnimatorOutQuarticEase((float) p.y, (float) y, ms));
            while (aniX.running()) {
                moveMouse((int) aniX.step(), (int) aniY.step());
            }
        }
    }
}
