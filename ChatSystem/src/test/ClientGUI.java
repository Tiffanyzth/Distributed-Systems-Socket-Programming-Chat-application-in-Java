package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    
    private JLabel label;
    // to hold the Username and later on the messages
    private JTextField tf;
    // to hold the server address an the port number
    private JTextField tfServer, tfPort;
    private JButton login, logout, userlist;
    private JTextArea room;
    
    // if it is for connection
    private boolean connected;
    
    // the Client object
    private Client client;
    
    // the default port number
    private int defaultPort;
    private String defaultHost;

    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {
    	// set title
        super("Chat Client");
        defaultHost = host;
        defaultPort = port;
        
        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3,1));
        
        // the server name and the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1,5,1,3));
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
        
        //add these to the row
        serverAndPort.add(new JLabel("Server Address:"));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel(""));
        serverAndPort.add(new JLabel("Port Number:"));
        serverAndPort.add(tfPort);
        
        // add the row to the North
        northPanel.add(serverAndPort);

        // the second row
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        
        // the third row
        tf = new JTextField("Username");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        room = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(room));
        room.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);       // you have to login before being able to logout
        userlist = new JButton("User List");
        userlist.addActionListener(this);
        userlist.setEnabled(false);      // you have to login before being able to Who is in

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(userlist);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 600);
        setVisible(true);
        tf.requestFocus();

    }

    // called by the Client to append text in the TextArea 
    void append(String str) {
        room.append(str);
        room.setCaretPosition(room.getText().length() - 1);
    }
    
    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        userlist.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("Anonymous");
        // reset port number and host name as a construction time
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }
        
    //  Button or JTextField clicked
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it is the Logout button
        if(o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.STOP, "hi"));
            return;
        }
        
        // if it is the userlist button
        if(o == userlist) {
            client.sendMessage(new ChatMessage(ChatMessage.LIST, ""));               
            return;
        }

        // if it is coming from the JTextField
        if(connected) {
            // just have to send the message
            client.sendMessage(new ChatMessage(ChatMessage.BROADCAST, tf.getText()));             
            tf.setText("");
            return;
        }
        

        if(o == login) {
            // ok it is a connection request
            String username = tf.getText().trim();
            // empty username ignore it
            if(username.length() == 0)
                return;
            // empty serverAddress ignore it
            String server = tfServer.getText().trim();
            if(server.length() == 0)
                return;
            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0)
                return;
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                return;   // nothing I can do if port number is not valid
            }

            // try creating a new Client with GUI
            client = new Client(server, port, username, this);
            // test if we can start the Client
            if(!client.start()) 
                return;
            tf.setText("");
            label.setText("Enter your message below");
            connected = true;
            
            // disable login button
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            userlist.setEnabled(true);
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // Action listener for when the user enter a message
            tf.addActionListener(this);
        }

    }

    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1500);
    }

}
