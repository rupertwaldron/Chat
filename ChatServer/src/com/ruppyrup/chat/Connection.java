package com.ruppyrup.chat;

import com.ruppyrup.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

import static com.ruppyrup.command.ChatLogger.chatLog;
import static com.ruppyrup.command.Command.*;

public class Connection implements Runnable {
    private final static String DEFAULT_NAME = "attempting client";

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
            while (true) {
                Optional<String> input = Optional.ofNullable(in.readLine());
                chatLog(input.orElse("No response from client: " + name), server.getLogArea());
                if (input.isEmpty()) break;

                String actionCode = input.get().substring(0, 1);
                String paramaters = input.get().substring(1);
                var command = valueOf(actionCode);
                String message = processCommand(command, paramaters);
                out.println(message);
            }
        } catch (IOException e) {
            chatLog("Error connecting/communicating to new client : " + e.getMessage(), server.getLogArea());
        } finally {
            quit();
        }
    }

    private String processCommand(Command command, String parameters) {
        return switch (command) {
            case N: {
                if (server.addConnection(parameters, socket)) {
                    name = parameters;
                    yield (A + parameters + " added to server 😁");
                } else {
                    yield (R + parameters + " may already exist 😟");
                }
            }
            default:
                yield "No message available";
        };
    }

    private void sendToClient(String message) {
        out.println(message);
        chatLog(message + " was sent to " + name, server.getLogArea());
    }

    private void quit() {
        chatLog("Connection ended for " + name, server.getLogArea());
        try {
            socket.close();
        } catch (IOException e) {

        }
    }
}
