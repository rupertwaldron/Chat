package com.ruppyrup.chat;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

import static com.ruppyrup.ChatLog.chatLog;
import static com.ruppyrup.chat.ChatServer.*;

public class Connection implements Runnable {
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static List<Socket> clientSockets = new ArrayList<>();
    private static Map<Integer, String> names = new HashMap<>();
    private Socket socket;
    private JTextArea textArea;

    public Connection(Socket socket, JTextArea textArea) {
        this.socket = socket;
        this.textArea = textArea;
        clientSockets.add(socket);
    }

    @Override
    public void run() {
        chatLog("New connection to " + socket.getInetAddress() + " : " + socket.getPort(), textArea);

        try (var in = new Scanner(socket.getInputStream());
             var out = new PrintWriter(socket.getOutputStream(), true)){

            clientWriters.add(out);

            String name = in.nextLine();
            out.println(name + " has joined");
            names.put(socket.getPort(), name);

            while (isServerRunning() && in.hasNextLine()) {
                String input = in.nextLine();

                chatLog(names.get(socket.getPort()) + " : " + input, textArea);
                Consumer<PrintWriter> broadcast = writer -> writer.println(names.get(socket.getPort()) + " : " + input);
                clientWriters.forEach(broadcast);
            }
            clientWriters.forEach(writer -> writer.println("Server Shutting down... Goodbye"));
        } catch (Exception e) {
            System.out.println("Error:" + socket);
        } finally {
            try {
                socket.close();
                chatLog("Client socket Closed : " + socket, textArea);
            } catch (IOException e) {
                chatLog("Error closing client socket: " + e.getMessage(), textArea);
            }
            chatLog("Closed: " + socket, textArea);
        }
    }
}
