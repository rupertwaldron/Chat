package com.ruppyrup.chat;

import javax.swing.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.ruppyrup.ChatLog.chatLog;

public class ClientThread implements Runnable {
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private Socket socket;
    private JTextArea textArea;

    public ClientThread(Socket socket, JTextArea textArea) {
        this.socket = socket;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        chatLog("New connection to " + socket.getInetAddress() + " : " + socket.getPort(), textArea);
    }
}
