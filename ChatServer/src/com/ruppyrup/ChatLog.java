package com.ruppyrup;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatLog {

    public static final void chatLog(String message, JTextArea area) {
        var time = new Date();
        var dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm:ss");
        var timeStamp = dateFormat.format(time);
        area.append(timeStamp + " -> " + message + "\n");

    }
}
