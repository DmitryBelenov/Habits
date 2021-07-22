package home.analytics.window.runnable;

import home.analytics.utils.SysUtils;
import home.analytics.window.BehaviourCollector;
import home.analytics.window.JNAWinState;

import java.util.Date;

public class WindowUsage implements Runnable, SysRunner {

    private final long checkInterval;
    private static final BehaviourCollector BC = BehaviourCollector.instance();
    public static final String KEY = WindowUsage.class.toString();
    public static volatile boolean work = true;

    public static final Date startDate = new Date();

    public WindowUsage(long checkInterval) {
        if (checkInterval < 100)
            throw new RuntimeException("To small check window interval");

        this.checkInterval = checkInterval;
        SysUtils.thHolder.put(KEY, Thread.currentThread());
    }

    @Override
    public void run() {
        if (isAlive()) {
            final String curWinName = JNAWinState.getForegroundName();
            BC.addToWUC(curWinName, checkInterval);
        }
    }

    private boolean isAlive() {
        if (!work) {
            SysUtils.thHolder.get(KEY).interrupt();
        }
        return work;
    }
}
