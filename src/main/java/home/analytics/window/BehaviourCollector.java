package home.analytics.window;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BehaviourCollector {
    private static volatile BehaviourCollector bc;

    private static Map<String, Long> windowUsageCollector;

    private static final int MAX_KEY_LENGTH = 120;

    private final static Lock rLock = new ReentrantReadWriteLock().readLock();
    private final static Lock wLock = new ReentrantReadWriteLock().writeLock();

    private BehaviourCollector() {
        windowUsageCollector = new ConcurrentHashMap<>();
    }

    public static BehaviourCollector instance() {
        if (bc == null) {
            synchronized (BehaviourCollector.class) {
                if (bc == null) {
                    bc = new BehaviourCollector();
                }
            }
        }
        return bc;
    }

    public Map<String, Long> getWUC() {
        rLock.lock();
        try {
            return windowUsageCollector;
        } finally {
            rLock.unlock();
        }
    }

    public Long getWUByName(final String name) {
        rLock.lock();
        try {
            return windowUsageCollector.get(name);
        } finally {
            rLock.unlock();
        }
    }

    public void addToWUC(String name, final Long timeMS) {
        wLock.lock();
        try {
            if (name.length() > MAX_KEY_LENGTH) {
                name = name.substring(0, MAX_KEY_LENGTH + 1);
            }

            windowUsageCollector.merge(name, timeMS, Long::sum);
        } finally {
            wLock.unlock();
        }
    }
}
