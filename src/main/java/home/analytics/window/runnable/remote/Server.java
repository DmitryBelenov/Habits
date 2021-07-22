package home.analytics.window.runnable.remote;

import home.analytics.utils.SysUtils;
import home.analytics.window.runnable.Cli;
import home.analytics.window.runnable.SysRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable, SysRunner {

    public static final String KEY = Server.class.toString();

    private ServerSocket serverSocket;
    private Socket clSocket;
    private PrintWriter out;
    private BufferedReader in;

    public static boolean serverAlive = true;

    @Override
    public void run() {
        SysUtils.thHolder.put(KEY, Thread.currentThread());
        try {
            final InetAddress netAddress = InetAddress.getLocalHost();
            serverSocket = new ServerSocket(6070);
            while (serverAlive) {
                clSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clSocket.getInputStream()));
                out = new PrintWriter(clSocket.getOutputStream(), true);

                String income = in.readLine();
                if (income.equalsIgnoreCase(RemParams.r.name())) {
                    final String stat = Cli.Commands.getStat().replace("\n", "[/*]"); // no \n in out stream
                    out.println(netAddress.getHostName() + " response:" + stat);
                } else {
                    out.println(netAddress.getHostName() + ": no such remote command");
                }
            }
        } catch (IOException e) {
            System.out.println("Server is closing..\n" + e.getMessage());
            // dummy
        }
        stop();
    }

    private void stop() {
        try {
            clSocket.close();

            in.close();
            out.close();
            serverSocket.close();
        } catch (IOException io) {
            // dummy
        }
    }

    private enum RemParams {
        r
    }
}
