package home.analytics.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SysUtils {

    private static final long SECOND_MS = 1000;
    private static final long MINUTE_MS = 1000 * 60;
    private static final long HOUR_MS = 1000 * 60 * 60;

    public static Map<String, Thread> thHolder = new ConcurrentHashMap<>();

    public static void sleep(final long ms) {
        long futurePoint = System.currentTimeMillis() + ms;
        while (true) {
            if (System.currentTimeMillis() >= futurePoint) break;
        }
    }

    public static String getFormatted(final Map<String, Long> wuc, final int maxLn) {
        StringBuilder sb = new StringBuilder("Windows usage:\n");
        Stream<Map.Entry<String, Long>> sorted = wuc.entrySet().stream().sorted(Map.Entry.comparingByValue());
        sorted.forEach(me -> {
            String k = me.getKey();
            if (!k.isEmpty()) {
                final int ln = k.length();
                final String cnvMs = convertMS(me.getValue());
                StringBuilder addLine = new StringBuilder(" ");
                for (int i = 0; i < (maxLn - ln) + 4; i++) {
                    addLine.append(".");
                }
                addLine.append(" ");

                sb.append(k).append(addLine.toString()).append(cnvMs).append("\n");
            }
        });
        return sb.toString();
    }

    private static String convertMS(final Long ms) {
        String cnv;
        if (ms > HOUR_MS) {
            cnv = (ms / HOUR_MS) + "h " + ((ms % HOUR_MS) / MINUTE_MS) + "min " + (((ms % HOUR_MS) % MINUTE_MS) / SECOND_MS) + "sec";
        } else if (ms > MINUTE_MS) {
            cnv = ((ms / MINUTE_MS) + "min " + ((ms % MINUTE_MS) / SECOND_MS) + "sec");
        } else {
            cnv = (ms / SECOND_MS) + "sec";
        }
        return cnv;
    }
}
