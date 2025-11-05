package simpleChat.server.backend;
// This file contains material supporting section 3.7 of the textbook:

import java.io.IOException;

// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import ocsf.server.*;
import simpleChat.client.ui.ServerConsole;

/**
 * This class overrides some of the methods in the abstract
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer {
	// Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 */
	public EchoServer(int port) {
		super(port);
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String message = msg.toString();

		if (message.startsWith("#login")) {	
			System.out.println("Message received: " + message + " from " + (String) client.getInfo("loginID"));

			if (client.getInfo("loginID") != null) {
				System.out.println("ERROR: Client " + client + " sent #login more than once. Terminating connection...");

				try {
					client.sendToClient("ERROR: You are already logged in. Connection terminated");
					client.close();
				} catch (IOException e) {
					// Ignore
				}

				return;
			}

			// Get the login ID from their cmd
			String loginID = message.substring(6).trim(); // Remove the "#login" part

			if (loginID.isEmpty()) {
				System.out.println("ERROR: Client " + client + " did not provide a login ID. Terminating connection...");

				try {
					client.sendToClient("ERROR: Login ID not provided. Connection terminated");
					client.close();
				} catch (IOException e) {
					// Ignore
				}

				return;
			}

			// Save the login ID
			client.setInfo("loginID", loginID);
			this.sendToAllClients(loginID + " has logged on.");
			System.out.println(loginID + " has logged on.");
		} else {
			// Handle regular messages
			String loginID = (String) client.getInfo("loginID");

			if (loginID == null) {
				System.out.println("ERROR: Client " + client + " sent a message before #login. Terminating connection...");

				try {
					client.sendToClient("ERROR: You must login before sending messages. Connection terminated");
					client.close();
				} catch (IOException e) {
					// Ignore
				}

				return;
			}

			String prefixedMessage = loginID + "> " + message;
			
			System.out.println("Message received: " + message + " from " + loginID);
			this.sendToAllClients(prefixedMessage);
		}
	}

	@Override 
	protected void clientConnected(ConnectionToClient client) {
		System.out.println("A new client has connected to the server.");
	}

	@Override 
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		System.out.println((String) client.getInfo("loginID") + " has disconnected.");

		client.setInfo("loginID", null);
	}

	/**
	 * This method overrides the one in the superclass. Called
	 * when the server starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called
	 * when the server stops listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	// Class methods ***************************************************

	/**
	 * This method is responsible for the creation of
	 * the server instance (there is no UI in this phase).
	 *
	 * @param args[0] The port number to listen on. Defaults to 5555
	 *                if no argument is entered.
	 */
	public static void main(String[] args) {
		int port = 0; // Port to listen on

		try {
			port = Integer.parseInt(args[0]); // Get port from command line
		} catch (Throwable t) {
			port = DEFAULT_PORT; // Set port to 5555
		}

		EchoServer sv = new EchoServer(port);

		try {
			sv.listen(); // Start listening for connections

			// Instantiate server console to handle server input
			ServerConsole serverConsole = new ServerConsole(sv);
			serverConsole.accept();
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
		}
	}
}
// End of EchoServer class