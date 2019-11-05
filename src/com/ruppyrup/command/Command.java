package com.ruppyrup.command;

public enum Command {

    S("S"),
    A("A"),
    R("R"),
    C("C"),
    N("N"),
    B("B"),
    Q("Q");

    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand () {
        return command;
    }
}
