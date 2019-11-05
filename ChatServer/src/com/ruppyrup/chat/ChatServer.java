package com.ruppyrup.chat;

import com.ruppyrup.mycomponents.TitleLabel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends JFrame implements Runnable {
    private static final long serialVersionUID = 4756118355993389721L;
    private static final int PORT_NUMBER = 63458;
    private JTextArea logArea = new JTextArea(10, 30);
    private JButton startButton = new JButton("Start");
    private ServerSocket serverSocket;
    private Map<String, Connection> connections = new ConcurrentHashMap<>();

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

    private void startServer() {
        //startButton.setEnabled(false);
        new Thread(this).start();
        startButton.setText("Stop");
    }

    private void stop() {
        log("Closing socket");
        if (serverSocket != null && serverSocket.isBound()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log("Failed to close server socket on port : " + PORT_NUMBER);
                log(e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        log("Server is running...");
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            while (true) {
                var socket = serverSocket.accept();
                log("New connection to " + socket.getInetAddress() + " : " + socket.getPort());
                new Connection(this, socket);
            }
        } catch (IOException e) {
            log("Exception whilst trying to listen on port " + PORT_NUMBER);
            log(e.getMessage());
        } finally {
            stop();
        }
    }

    public void log(String message) {
        var time = new Date();
        var dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm:ss");
        var timeStamp = dateFormat.format(time);
        logArea.append(timeStamp + " -> " + message + "\n");
    }

    public boolean addConnection(Connection newConnection, String newName) {
        if (connections.containsKey(newName)) {
            return false;
        }
        connections.put(newName, newConnection);
        return true;
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
