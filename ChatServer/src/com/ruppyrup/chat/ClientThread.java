package com.ruppyrup.chat;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

import static com.ruppyrup.ChatLog.chatLog;

public class ClientThread implements Runnable {
//    private final static List<String> COLOURS = Arrays.asList("\033[31m", "\033[32m", "\033[33m", "\033[34m", "\033[35m", "\033[36m", "\033[37m");
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static Map<Integer, String> names = new HashMap<>();
//    private static Map<Integer, String> portColourMap = new HashMap<>();
//    private static int colorIndex;
    private Socket socket;
    private JTextArea textArea;

    public ClientThread(Socket socket, JTextArea textArea) {
        this.socket = socket;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        chatLog("New connection to " + socket.getInetAddress() + " : " + socket.getPort(), textArea);
//        colorIndex = colorIndex < COLOURS.size() ? colorIndex : 0;
//        portColourMap.put(socket.getPort(), COLOURS.get(colorIndex++));

        try (var in = new Scanner(socket.getInputStream());
             var out = new PrintWriter(socket.getOutputStream(), true)){

            clientWriters.add(out);

            out.println("Enter client name: ");
            String name = in.nextLine();
            out.println(name + " has joined");
            names.put(socket.getPort(), name);

            while (in.hasNextLine()) {
                String input = in.nextLine();

                chatLog(names.get(socket.getPort()) + " : " + input, textArea);
                Consumer<PrintWriter> broadcast = writer -> writer.println(names.get(socket.getPort()) + " : " + input);
                clientWriters.forEach(broadcast);
            }
        } catch (Exception e) {
            System.out.println("Error:" + socket);
        } finally {
            try { socket.close(); } catch (IOException e) {e.getMessage();}
            System.out.println("Closed: " + socket);
        }
    }
}
