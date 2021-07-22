package home.analytics.window.runnable.remote;

import home.analytics.window.runnable.SysRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable, SysRunner {
    private final String ipAddress;
    private final String command;

    public Client(final String ip, final String command) {
        this.ipAddress = ip;
        this.command = command;
    }

    @Override
    public void run() {
        try {
            final Socket socket = new Socket(ipAddress, 6070);
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            final BufferedReader in= new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(command);
            String resp = in.readLine();

            System.out.println(resp.replace( "[/*]", "\n"));

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Remote control error: " + e);
        }
        System.out.print(">_ ");
    }
}
