package test;

import java.net.*;
import java.io.*;

// The Client that can be run both as a console or a GUI
public class Client  {

	private Socket socket;
	
    // for I/O
    private ObjectInputStream sInput;       // to read from the socket
    private ObjectOutputStream sOutput;     // to write on the socket
        
    // the server address, the port and the username
    private String server;
    private int port;
    private String username;
    
    private ClientGUI cg;

    // Constructor called by console mode
    Client(String server, int port, String username) {
        // which calls the common constructor with the GUI set to null
        this(server, port, username, null);
    }

    // Constructor call when used from a GUI
    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        // save as a GUI or null
        this.cg = cg;
    }
    
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }
        String msg = "Connection accepted " + socket.getInetAddress() + ": " + socket.getPort();
        display(msg);
    
        // Creating both Data Stream 
        try {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // creates the Thread to listen from the server 
        new ListenFromServer().start();
        try {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }

    // To send a message to the console or the GUI
    private void display(String msg) {
        if(cg == null)
            System.out.println(msg);      //in console mode
        else
            cg.append(msg + "\n");      //append to the ClientGUI TextArea
    }
    
    // To send a message to the server
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    // Close the Input/Output streams, socket and connection
    private void disconnect() {
        try { 
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do
        
        // inform the GUI
        if(cg != null)
            cg.connectionFailed();
            
    }

    // a class that waits for the message from the server and append them to the JTextArea
    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    // if console mode print the message and add back the prompt
                    if(cg == null) {
                        System.out.println(msg);
                        System.out.print("> ");
                    }else {
                        cg.append(msg);
                    }
                }
                catch(IOException e) {
                    display("Server has close the connection: " + e);
                    if(cg != null) 
                        cg.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}