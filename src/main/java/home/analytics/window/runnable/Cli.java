package home.analytics.window.runnable;

import home.analytics.Habits;
import home.analytics.utils.SysUtils;
import home.analytics.window.BehaviourCollector;
import home.analytics.window.runnable.remote.Client;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Cli implements Runnable {

    public static final String KEY = Cli.class.toString();
    private boolean workCli = true;

    private static final BehaviourCollector BC = BehaviourCollector.instance();

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
                return "wrong number of args. Example: remote 192.168.0.1 r";

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
            return SysUtils.getFormatted(wuc, maxLn[0]);
        }

        private static String saveStatToFile() {
            String saveRes;
            final String filePath = System.getProperty("user.home") + "/Desktop/Habits_Stat_" + DATE_FORMAT.format(WindowUsage.startDate) + "—" + DATE_FORMAT.format(new Date()) + ".txt";
            try {
                FileUtils.writeStringToFile(new File(filePath), getStat(), StandardCharsets.UTF_8);
                saveRes = "Statistic saved to desktop";
            } catch (IOException io) {
                saveRes = io.getMessage();
            }
            return saveRes;
        }
    }
}
