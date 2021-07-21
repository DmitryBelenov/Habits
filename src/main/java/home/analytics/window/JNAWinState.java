package home.analytics.window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.util.*;

public class JNAWinState {
    public static List<WinDef.HWND> getAllVisible() {
        final List<WinDef.HWND> windows = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                windows.add(hwnd);
            }
            return true;
        }, null);

        return windows;
    }

    public static Collection<String> getAllVisibleNames() {
        List<WinDef.HWND> visible = getAllVisible();
        if (visible.size() > 0) {
            Collection<String> res = new ArrayList<>(visible.size());
            for (WinDef.HWND v : visible) {
                final String name = getWindowTitle(v);
                if (!name.isEmpty()) {
                    res.add(name);
                }
            }
            return res;
        }
        return Collections.emptyList();
    }

    public static String getWindowTitle(final WinDef.HWND hwnd) {
        final char[] title = new char[User32.INSTANCE.GetWindowTextLength(hwnd) + 1];
        final int length = User32.INSTANCE.GetWindowText(hwnd, title, title.length);
        return Native.toString(Arrays.copyOfRange(title, 0, length));
    }

//    public static String getWindowFullURI(final WinDef.HWND hwnd) {
//        final char[] buffer = new char[1024];
//        final int length = User32.INSTANCE.GetWindowModuleFileName(hwnd, buffer, buffer.length);
//        return Native.toString(Arrays.copyOfRange(buffer, 0, length));
//    }

    public static int getAllVisibleCount() {
        return getAllVisible().size();
    }

    public static WinDef.HWND getForeground() {
        return User32.INSTANCE.GetForegroundWindow();
    }

    public static String getForegroundName() {
        return getWindowTitle(User32.INSTANCE.GetForegroundWindow());
    }
}
