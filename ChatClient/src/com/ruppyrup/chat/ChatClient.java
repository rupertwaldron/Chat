package com.ruppyrup.chat;

import com.ruppyrup.command.Command;
import com.ruppyrup.mycomponents.TitleLabel;
import com.ruppyrup.networking.LogInDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

import static com.ruppyrup.command.ChatLogger.chatLog;
import static com.ruppyrup.command.Command.*;

public class ChatClient extends JFrame implements Runnable {
    private static final long serialVersionUID = -6247572210030262635L;
    private static final int PORT_NUMBER = 63458;

    private String name = "Anonymous";
    private String host = "localhost";
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JTextArea chatArea = new JTextArea(20, 30);
    private JTextArea inputArea = new JTextArea(3, 30);
    private LogInDialog logInDiaglog = new LogInDialog("Chat");


    public ChatClient() {
        initGUI();
        setTitle("Chat");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        login();
        new Thread(this).start();
    }

    private void login() {
        logInDiaglog.setVisible(true);
        if (!logInDiaglog.isCancelled()) {
            host = logInDiaglog.getIpAddressField();
            name = logInDiaglog.getUserNameField();
            System.out.println("Host :" + host + "\nName : " + name);
        } else {
            close();
        }
    }

    private void initGUI() {
        var titleLable = new TitleLabel("Chat");
        add(titleLable, BorderLayout.PAGE_START);

        //listeners
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
                System.exit(0);
            }
        });

        // main panel
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);

        // chat area
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        var marginInsets = new Insets(3, 3, 3, 3);
        chatArea.setMargin(marginInsets);
        var chatScrollPane = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(chatScrollPane);

        // input area
        var messageLabel = new JLabel("Type your message here: ");
        mainPanel.add(messageLabel);
        inputArea.setLineWrap(true);
        inputArea.setMargin(marginInsets);
        var inputScrollPane = new JScrollPane(inputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(inputScrollPane);
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    send();
                }
            }
        });

        // send button
        var buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.PAGE_END);
        var sendButton = new JButton("Send");
        sendButton.addActionListener((event) -> send());
        buttonPanel.add(sendButton);

    }

    private void send() {
        String message = inputArea.getText().trim();
        if (message.length() > 0) {
            processCommand(BROADCAST, message);
        }
    }


    @Override
    public void run() {
        try {
            socket = new Socket(host, PORT_NUMBER);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                Optional<String> input = Optional.ofNullable(in.readLine());

                if (input.isEmpty()) break;
                String serverMessage = input.get();
                String actionCode;
                String parameters = "";
                if (serverMessage.endsWith(":")) {
                    actionCode = serverMessage.substring(0, serverMessage.indexOf(":"));
                } else {
                    String[] message = serverMessage.split(":");
                    actionCode = message[0];
                    parameters = message[1];
                }

                var command = valueOf(actionCode);
                processCommand(command, parameters);
            }

        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(this, "The server is not running");
        } catch (IOException ee) {
            JOptionPane.showMessageDialog(this, "Lost connection to the server");
        } finally {
            close();
        }
    }

    private void processCommand(Command command, String parameters) {
        switch (command) {
            case SUBMIT -> {
                String messageToSever = NAME.getCommand() + name;
                out.println(messageToSever);
                chatLog(messageToSever, chatArea);
            }
            case CHAT -> chatLog(parameters, chatArea);
            case ACCEPTED -> chatLog("server accepted name: " + name + "-> (server)" + parameters, chatArea);
            case REJECTED -> {
                chatLog("server rejected name: " + name + "-> (server)" + parameters, chatArea);
                close();
            }
            case BROADCAST -> {
                String messageToSever = name + "-> " + parameters;
                out.println(BROADCAST.getCommand() + messageToSever);
                inputArea.setText("");
            }
            default -> chatLog("No command from server", chatArea);
        }
    }

    private void close() {
        try {
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
        } catch (IOException e) {
            System.exit(0);
        } finally {
            try {
                chatLog("Closing down.....", chatArea);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
            System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        try {
            String className = UIManager.getCrossPlatformLookAndFeelClassName();
            UIManager.setLookAndFeel(className);
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(ChatClient::new);
    }
}
