package com.ruppyrup.chat;

import com.ruppyrup.mycomponents.TitleLabel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.ruppyrup.command.ChatLogger.chatLog;

public class ChatServer extends JFrame implements Runnable {
    private static final long serialVersionUID = 4756118355993389721L;
    private static final int PORT_NUMBER = 63458;
    private JTextArea logArea = new JTextArea(20, 50);
    private JButton startButton = new JButton("Start");
    private ServerSocket serverSocket;
    private Map<PrintWriter, String> connections = new ConcurrentHashMap<>();

    public ChatServer() {
        initGUI();
        setTitle("Chat Server");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void initGUI() {
        var titleLable = new TitleLabel("Chat Server");
        add(titleLable, BorderLayout.PAGE_START);

        // main panel
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);

        // log area
        logArea.setEditable(false);
        var scrollPane = new JScrollPane(logArea);
        mainPanel.add(scrollPane);
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // button panel
        var buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.PAGE_END);
        startButton.addActionListener((a) -> startServer());
        buttonPanel.add(startButton);
        getRootPane().setDefaultButton(startButton);

        //listeners
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stop();
                System.exit(0);
            }
        });
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    private void startServer() {
        //startButton.setEnabled(false);
        new Thread(this).start();
        startButton.setText("Stop");
    }

    private void stop() {
        chatLog("Closing socket", logArea);
        if (serverSocket != null && serverSocket.isBound()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                chatLog("Failed to close server socket on port : " + PORT_NUMBER, logArea);
                chatLog(e.getMessage(), logArea);
            }
        }
    }

    @Override
    public void run() {
        chatLog("Server is running...", logArea);
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            while (true) {
                var socket = serverSocket.accept();
                chatLog("New connection to " + socket.getInetAddress() + " : " + socket.getPort(), logArea);
                new Connection(this, socket);
            }
        } catch (IOException e) {
            chatLog("Exception whilst trying to listen on port " + PORT_NUMBER, logArea);
            chatLog(e.getMessage(), logArea);
        } finally {
            stop();
        }
    }

    public boolean addConnection(PrintWriter printWriter, String name) {
        if (connections.containsKey(printWriter)) {
            return false;
        }
        connections.put(printWriter, name);
        return true;
    }

    public boolean removeConnection(PrintWriter printWriter) {
        if (!connections.containsKey(printWriter)) {
            return false;
        }
        connections.remove(printWriter);
        return true;
    }

    public Optional<String> getName(PrintWriter printWriter) {
        return Optional.ofNullable(connections.get(printWriter));
    }

    public void broadCast(String message) {
        connections.keySet().forEach(printWriter -> {
            chatLog("(sent)" + message + " to " + connections.get(printWriter), logArea);
            printWriter.println(message);
        });
    }

    public static void main(String[] args) {
        try {
            String className = UIManager.getCrossPlatformLookAndFeelClassName();
            UIManager.setLookAndFeel(className);
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(ChatServer::new);
    }
}
