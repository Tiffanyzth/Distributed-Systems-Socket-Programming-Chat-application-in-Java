package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI extends JFrame implements ActionListener, WindowListener {
    
    private static final long serialVersionUID = 1L;
    
    // the stop and start buttons
    private JButton stopStart, kick, stats;
    
    // JTextArea for the chat room and the events
    private JTextArea chat, event;
    
    // The port number
    private JTextField tPortNumber, userId;
    
    // my server
    private Server server;
    
    
    // constructor that receive the port to listen
    ServerGUI(int port) {
    	// set title
        super("Chat Server");
        server = null;
        
        // in the NorthPanel
        JPanel north = new JPanel(new GridLayout(3,1));
        // the first row
        JPanel row1 = new JPanel(new GridLayout(1,2));
        row1.add(new JLabel("Port number: "));
        tPortNumber = new JTextField("  " + port);
        row1.add(tPortNumber);
        // to stop or start the server, we start with "Start"
        stopStart = new JButton("Start");
        stopStart.addActionListener(this);
        row1.add(stopStart);
        north.add(row1);
        
        // the second row
        north.add(new JPanel());
        
        // the third row
        JPanel row2 = new JPanel(new GridLayout(1,3));
        row2.add(new JLabel("User Id: "));
        userId = new JTextField("  ");
        row2.add(userId);
        // to kick someone or stats commands
        kick = new JButton("Kick");
        kick.addActionListener(this);
        row2.add(kick);
        stats = new JButton("Stats");
        stats.addActionListener(this);
        row2.add(stats);
        north.add(row2);
        
        add(north, BorderLayout.NORTH);
        
        // the event and chat room
        JPanel center = new JPanel(new GridLayout(2,1));
        chat = new JTextArea(80,80);
        chat.setEditable(false);
        appendRoom("Chat room.\n");
        center.add(new JScrollPane(chat));
        event = new JTextArea(80,80);
        event.setEditable(false);
        appendEvent("Events log.\n");
        center.add(new JScrollPane(event)); 
        add(center);
        
        // need to be informed when the user click the close button on the frame
        addWindowListener(this);
        setSize(400, 600);
        setVisible(true);
    }       

    // append message to the two JTextArea (position at the end)
    void appendRoom(String str) {
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }
    void appendEvent(String str) {
        event.append(str);
        event.setCaretPosition(chat.getText().length() - 1);
        
    }
    
    // start or stop where clicked
    public void actionPerformed(ActionEvent e) {
    	Object o = e.getSource();
    	
        // if it is the stopStart button
        if(o == stopStart) {
        	// if running we have to stop
            if(server != null) {
                server.stop();
                server = null;
                tPortNumber.setEditable(true);
                stopStart.setText("Start");
                return;
            }
            // start the server  
            int port;
            try {
                port = Integer.parseInt(tPortNumber.getText().trim());
            }
            catch(Exception er) {
                appendEvent("Invalid port number");
                return;
            }
            // create a new Server
            server = new Server(port, this);
            new ServerRunning().start();
            stopStart.setText("Stop");
            tPortNumber.setEditable(false);
        }
        
        // if it is the kick button
        if(o == kick) {
        	int kickId;
        	kickId = Integer.parseInt(userId.getText().trim());
            server.kick(kickId);
            return;
        }
        
        // if it is the stats button
        if(o == stats) {
        	int statsId;
        	statsId = Integer.parseInt(userId.getText().trim());
        	server.stats(statsId);              
        	return;
        }
        
    }
    
    // entry point to start the Server
    public static void main(String[] arg) {
        // start server default port 1500
        new ServerGUI(1500);
    }

    // If the user click the X button to close the application
    // need to close the connection with the server to free the port
    public void windowClosing(WindowEvent e) {
        // if my Server exist
        if(server != null) {
            try {
            	// ask the server to close the connection
                server.stop();          
            }
            catch(Exception eClose) {
            }
            server = null;
        }
        // dispose the frame
        dispose();
        System.exit(0);
    }
    
    // can ignore the other WindowListener methods
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    // A thread to run the Server
    class ServerRunning extends Thread {
        public void run() {
            server.start();         // should execute until if fails
            // the server failed
            stopStart.setText("Start");
            tPortNumber.setEditable(true);
            appendEvent("Server crashed\n");
            server = null;
        }
    }

}