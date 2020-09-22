package test;

import java.io.*;

public class ChatMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    // The different types of message
    static final int LIST = 0, BROADCAST = 1, STOP = 2, KICK = 3, STATS = 4;
    private int type;
    private String message;
    
    // constructor
    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }
    
    // getters
    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
}