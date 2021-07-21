package home.analytics.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SysUtils {
    public static Map<String, Thread> thHolder = new ConcurrentHashMap<>();

    public static void sleep(final long ms) {
        long futurePoint = System.currentTimeMillis() + ms;
        while (true) {
            if (System.currentTimeMillis() >= futurePoint) break;
        }
    }
}
