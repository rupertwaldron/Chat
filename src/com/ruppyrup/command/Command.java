package com.ruppyrup.command;

public enum Command {

    SUBMIT("SUBMIT"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    CHAT("CHAT"),
    NAME("NAME"),
    BROADCAST("BROADCAST"),
    QUIT("QUIT");

    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand () {
        return command + ":";
    }
}
