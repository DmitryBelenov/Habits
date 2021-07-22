package home.analytics;

import home.analytics.utils.SysUtils;
import home.analytics.window.runnable.Cli;
import home.analytics.window.runnable.remote.Server;
import home.analytics.window.runnable.WindowUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Habits {

    private static final List<ScheduledExecutorService> execs = new ArrayList<>();
    private static final Map<String, Long> runList = new HashMap<>();
    static {
        runList.put(WindowUsage.KEY, 1000L);
    }

    public static void start() {
        loadRunners();
        loadServerListener();
        loadCliTask();
    }

    public static void stop() {
        execs.forEach(ExecutorService::shutdown);
        SysUtils.thHolder.values().forEach(t -> {
            if (!t.isInterrupted())
                t.interrupt();
        });
    }

    private static void loadRunners() {
        runList.forEach((key, interval) -> {
            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            if (key.equals(WindowUsage.KEY)) {
                executorService.scheduleWithFixedDelay(
                        new WindowUsage(interval),
                        0,
                        interval,
                        TimeUnit.MILLISECONDS);
            }
            execs.add(executorService);
        });
    }

    private static void loadCliTask() {
        final Thread th = new Thread(new Cli());
        th.start();
    }

    private static void loadServerListener() {
        final Thread thS = new Thread(new Server());
        thS.start();
    }
}
