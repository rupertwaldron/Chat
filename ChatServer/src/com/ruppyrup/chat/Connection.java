package com.ruppyrup.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.ruppyrup.command.Command.S;

public class Connection implements Runnable {
    private final static String DEFAULT_NAME = "New Client";

    private ChatServer server;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String name = DEFAULT_NAME;

    public Connection(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        new Thread(this).start();
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            sendToClient(S.getCommand());
            boolean validName = false;
            boolean keepRunning = true;
            while (keepRunning) {
                String input = in.readLine();
                server.log(input);
                if (input.isEmpty()) {
                    keepRunning = false;
                }
            }
        } catch (IOException e) {
            server.log("Error connecting/communicating to new client : " + e.getMessage());
        } finally {
            quit();
        }
    }

    private void sendToClient(String message) {
        out.println(message);
        server.log(message + " was sent to " + name);
    }

    private void quit() {
        server.log("Connection ended for " + name);
        try {
            socket.close();
        } catch (IOException e) {

        }
    }
}
