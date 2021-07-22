package home.analytics.window.runnable;

import home.analytics.Habits;
import home.analytics.utils.SysUtils;
import home.analytics.window.BehaviourCollector;
import home.analytics.window.runnable.remote.Client;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Cli implements Runnable {

    public static final String KEY = Cli.class.toString();
    private boolean workCli = true;

    private static final BehaviourCollector BC = BehaviourCollector.instance();

    private static final long SECOND_MS = 1000;
    private static final long MINUTE_MS = 60000;
    private static final long HOUR_MS = 3600000;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH-mm_dd.MM.yyyy");
    private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";

    public Cli() {
        SysUtils.thHolder.put(KEY, Thread.currentThread());
    }

    @Override
    public void run() {
        System.out.println("Habits started");
        Scanner sc = new Scanner(System.in);
        while (workCli) {
            System.out.print(">_ ");
            final String inp = sc.nextLine();

            if (isExit(inp)) {
                workCli = false;
                continue;
            }

            System.out.println(cmdFactory(inp));
        }

        System.out.println("Habits closing..");
        SysUtils.sleep(3000);
        System.exit(0);
    }

    private boolean isExit(final String inp) {
        if (!inp.equalsIgnoreCase(Commands.exit.name())) {
            return false;
        }

        Habits.stop();
        return true;
    }

    private String cmdFactory(String inp) {
        inp = inp.toLowerCase(Locale.ROOT);
        if (inp.trim().startsWith("remote")) {
            String[] cmdArr = inp.split(" ");
            if (cmdArr.length < 3)
                return "wrong number of args. Example: remote 128.0.0.1 r";

            final String ip = cmdArr[1].trim();
            if (!ip.matches(IPV4_REGEX))
                return "wrong IP: " + ip;

            final Thread rTh = new Thread(new Client(ip, cmdArr[2].trim()));
            rTh.start();
            return "please wait..";
        } else if (Commands.exists(inp)) {
            return Commands.valueOf(inp).getCmdExtractor().get();
        } else
            return "no such command";
    }

    public enum Commands {
        exit(()-> ""),
        stat(Commands::getStat),
        txt(Commands::saveStatToFile);

        Supplier<String> cmdExtractor;

        Commands(final Supplier<String> cmdExtractor) {
            this.cmdExtractor = cmdExtractor;
        }

        private static boolean exists(final String inp) {
            for (Commands c : Commands.values()) {
                if (c.name().equals(inp)) {
                    return true;
                }
            }
            return false;
        }

        public Supplier<String> getCmdExtractor() {
            return cmdExtractor;
        }

        public static synchronized String getStat() {
            final Map<String, Long> wuc = new HashMap<>(BC.getWUC());
            final int[] maxLn = {0};
            wuc.keySet().forEach(s-> {
                int sLn = s.length();
                if (sLn > maxLn[0]) {
                    maxLn[0] = sLn;
                }
            });
            return getFormatted(wuc, maxLn[0]);
        }

        private static String getFormatted(final Map<String, Long> wuc, final int maxLn) {
            StringBuilder sb = new StringBuilder("Windows usage:\n");
            Stream<Map.Entry<String,Long>> sorted = wuc.entrySet().stream().sorted(Map.Entry.comparingByValue());
            sorted.forEach(me -> {
                final int ln = me.getKey().length();
                final String k = me.getKey();
                final String cnvMs = convertMS(me.getValue());
                StringBuilder addLine = new StringBuilder(" ");
                for (int i = 0; i < (maxLn - ln) + 4; i++) {
                    addLine.append(".");
                }
                addLine.append(" ");
                sb.append(k).append(addLine.toString()).append(cnvMs).append("\n");
            });
            return sb.toString();
        }

        private static String saveStatToFile() {
            String saveRes;
            final String filePath = System.getProperty("user.home") + "/Desktop/Habits_Stat_" + DATE_FORMAT.format(WindowUsage.startDate) + "—" + DATE_FORMAT.format(new Date()) + ".txt";
            try {
                FileUtils.writeStringToFile(new File(filePath), getStat());
                saveRes = "Statistic saved to desktop";
            } catch (IOException io) {
                saveRes = io.getMessage();
            }
            return saveRes;
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
}
