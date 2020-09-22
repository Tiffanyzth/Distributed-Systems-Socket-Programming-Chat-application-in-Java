package test;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// can be run both as a console application or a GUI
public class Server {
    private static int uniqueId;
    private int port;
    private SimpleDateFormat sdf;
    private boolean keepGoing;
        
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;
    
    // GUI of the server
    private ServerGUI sg;

    //server constructor in console
    public Server(int port) {
        this(port, null);
    }
    
    // constructor
    public Server(int port, ServerGUI sg) {
        
    	// GUI or null
        this.sg = sg;
        this.port = port;
        
        // to set the time format - hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        
        // ArrayList for the Client list
        al = new ArrayList<ClientThread>();
    }
    
    public void start() {
        keepGoing = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while(keepGoing) {
            	
                // display message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");
                // accept connection
                Socket socket = serverSocket.accept();
                // if the server was asked to stop
                if(!keepGoing) {
                	break;
                }
                ClientThread t = new ClientThread(socket);
                // save the client thread in the ArrayList
                al.add(t);
                t.start();
            }
            
            // after the server was asked to stop
            try {
                serverSocket.close();
                //close the threads and socket of all the clients
                for(int i = 0; i < al.size(); ++i) {
                    ClientThread ct = al.get(i);
                    try {
                    ct.sInput.close();
                    ct.sOutput.close();
                    ct.socket.close();
                    }
                    catch(IOException io) {
                        io.printStackTrace();
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }
    
    //For the GUI to stop the server
	@SuppressWarnings("resource")
	protected void stop() {
        keepGoing = false;
        try {
            new Socket("localhost", port);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // Display an event (not a message) to the console or the GUI
    private void display(String msg) {
        String message = sdf.format(new Date()) + " " + msg + "\n";
        if(sg == null) {
            System.out.print(message);
        }else {
            sg.appendEvent(message);
        }
    }
    
    // to broadcast a message to all Clients
    private synchronized void broadcast(String message) {
    	// display the message in room window
    	String mess = sdf.format(new Date()) + " " + message + "\n";
        if(sg == null) {
            System.out.print(mess);
        }else {
        	// Difference between display(): append in the room window
            sg.appendRoom(mess);
        }
        
        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            // try to write to the Client
            // if it fails, remove it from the list
            if(!ct.writeMsg(mess)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who log off
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);                
                return;
            }
        }
    }
    
    // for a client who log off
    synchronized void kick(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);           
                broadcast("KICK - "+ct.username+"(id: "+ct.id+")\n");
                ct.close();
                return;
            }
        }
    }
    
    // for STATS command
    synchronized void stats(int id) {
    	// scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
            	display("STATS - "+ct.username+"(id: "+ct.id+")\n");
               	// scan the message list
                for(int j = 0; j < ct.msgList.size(); ++j) {
                    // found it
                    if(ct.id == id) {
                    	display("\t"+(j+1)+") "+ct.msgList.get(j)+"\n");
                    }
                }
                return;
            }
        }
    }

    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        
        // my unique id (easier for exit connection)
        int id;
        // the User's name of the Client
        String username;
        // the only type of message a will receive
        ChatMessage cm;
        // the date I connect
        String date;
        // the command/message that a client sent
        ArrayList<String> msgList = new ArrayList<String>();

        // Constructor
        ClientThread(Socket socket) {
            // a unique id
        	++uniqueId;
            id = uniqueId;
            this.socket = socket;
            // Creating both Data Stream 
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the user's name
                username = (String) sInput.readObject();
                broadcast(username + "(id: "+id + ") just connected.");
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            catch (ClassNotFoundException e) {
            	e.printStackTrace();
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + "(id: "+id + ") Exception reading Streams: " + e);
                    break;              
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                // the message part of the origin
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch(cm.getType()) {
                case ChatMessage.BROADCAST:
                    broadcast(username + "(id: "+id + "): " + message);
                    // store the command
                    msgList.add("BROADCAST - {" + message + "}");
                    break;
                case ChatMessage.STOP:
                	broadcast(username + "(id: "+id + ") has stopped the connection.");
                	// store the command
                    msgList.add("STOP");
                    keepGoing = false;
                    break;
                case ChatMessage.LIST:
                    writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                    // scan all the users connected
                    for(int i = 0; i < al.size(); ++i) {
                        ClientThread ct = al.get(i);
                        writeMsg((i+1) + ") " + ct.username + "(id: "+ct.id + ") : " + uniqueId+"\n");
                    }
                    // store the command
                    msgList.add("LIST");
                    break;
                }
            }
            // remove myself from the arrayList containing the list of the connected Clients
            remove(id);
            close();
        }
        
        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        // Write a String to the Client output stream
        private boolean writeMsg(String msg) {
            // if Client is still connected, send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}