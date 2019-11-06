package com.ruppyrup.command;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatLogger {

    public static void chatLog(String message, JTextArea textArea) {
        var time = new Date();
        var dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm:ss");
        var timeStamp = dateFormat.format(time);
        textArea.append(timeStamp + " -> " + message + "\n");
    }
}
