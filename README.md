# Distributed-Systems-Socket-Programming-Chat-application-in-Java

**Functions:**

- The server will be able to support multiple client connections. 
- Each time a client connects to the server the event is announced to all connected clients.
- The client can send and receive information from the server. Because of the realization of GUIs, the specific command format is not needed anymore.

	1. BROADCAST - {content}}: enables a client to send text to all the other clients connected to the server.
	2. {STOP}: forces the server to close the connection with the client that initiated the command, this event must be announced to all other clients.
	3. {LIST}: displays a list of all client IDs currently connected to the server.
	4. {KICK - ID}: closes the connection between the server and the IP client, and announces this to all clients.
	5. {STATS - ID}: gets a list of all commands used by the client identified by the ID.
- Provide a simple Graphical User Interface to allow a user access the system. 
- Code with detailed comments.

**Example:**
![example](https://github.com/Tiffanyzth/Distributed-Systems-Socket-Programming-Chat-application-in-Java/blob/master/img.png)
