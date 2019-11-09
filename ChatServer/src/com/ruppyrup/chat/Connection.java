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
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            sendToClient(SUBMIT.getCommand());
            boolean validName = false;
            while (true) {
                Optional<String> input = Optional.ofNullable(in.readLine());
                chatLog(input.orElse("No response from client: " + name), server.getLogArea());
                if (input.isEmpty()) break;
                String clientMessage = input.get();
                String actionCode;
                String parameters = "";
                if (clientMessage.endsWith(":")) {
                    actionCode = clientMessage.substring(0, clientMessage.indexOf(":"));
                } else {
                    String[] message = clientMessage.split(":");
                    actionCode = message[0];
                    parameters = message[1];
                }
                var command = valueOf(actionCode);
                processCommand(command, parameters);
            }
        } catch (IOException e) {
            chatLog("Error connecting/communicating to new client : " + e.getMessage(), server.getLogArea());
        } finally {
            quit();
        }
    }

    private void processCommand(Command command, String parameters) {
        switch (command) {
            case NAME -> {
                chatLog("(received)" + NAME.getCommand() + parameters + " from " + name, server.getLogArea());
                if (server.addConnection(out, parameters)) {
                    name = parameters;
                    String messageToClient = ACCEPTED.getCommand() + parameters + " added to server";
                    out.println(messageToClient);
                    server.broadCast(CHAT.getCommand() + name + " has joined the chat room");
                    chatLog("(sent)" + messageToClient, server.getLogArea());
                } else {
                    String messageToClient = REJECTED.getCommand() + parameters + " may already exist";
                    out.println(messageToClient);
                    chatLog("(sent)" + messageToClient, server.getLogArea());
                    chatLog("Disconnecting client", server.getLogArea());
                    quit();
                }
            }
            case BROADCAST -> {
                chatLog("(received)" + BROADCAST.getCommand() + parameters + " from " + name, server.getLogArea());
                server.broadCast(CHAT.getCommand() + parameters);
            }
        }
    }

    private void sendToClient(String message) {
        out.println(message);
        chatLog(message + " was sent to " + name, server.getLogArea());
    }

    private void quit() {
        chatLog("Connection ended for " + name, server.getLogArea());
        server.removeConnection(out);
        server.broadCast(CHAT.getCommand() + name + " has left the chat room");
        try {
            socket.close();
        } catch (IOException e) {

        }
    }
}
