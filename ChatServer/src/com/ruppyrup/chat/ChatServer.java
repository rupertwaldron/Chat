package com.ruppyrup.chat;

import com.ruppyrup.mycomponents.TitleLabel;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

import static com.ruppyrup.ChatLog.chatLog;

public class ChatServer extends JFrame implements Runnable {
    private static final long serialVersionUID = 4756118355993389721L;
    private static final int PORT_NUMBER = 63458;
    private JTextArea logArea = new JTextArea(10, 30);
    private JButton startButton = new JButton("Start");
    private ServerSocket serverSocket;

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
        logArea.setForeground(Color.RED);
        var scrollPane = new JScrollPane(logArea);
        mainPanel.add(scrollPane);
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // button panel
        var buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.PAGE_END);
        startButton.addActionListener(e -> {
            if (startButton.getText().equals("Start")) {
                startServer();
            } else {
                stop();
            }
        });
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
        chatLog("Closing socket", logArea);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                chatLog("Failed to close server socket on port : " + PORT_NUMBER, logArea);
                chatLog(e.getMessage(), logArea);
            }
        }
        startButton.setText("Start");
    }

    @Override
    public void run() {
        chatLog("Server is running...", logArea);
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new ClientThread(serverSocket.accept(), logArea));
            }
        } catch (IOException e) {
            //chatLog("Exception whilst trying to listen on port " + PORT_NUMBER, logArea);
            chatLog(e.getMessage(), logArea);
        } finally {
            chatLog("Thread finishing", logArea);
        }
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
