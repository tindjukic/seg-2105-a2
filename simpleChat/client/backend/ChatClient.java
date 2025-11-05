// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package simpleChat.client.backend;

import ocsf.client.*;

import java.io.*;

import simpleChat.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient {
	// Instance variables **********************************************

	/**
	 * The interface type variable. It allows the implementation of
	 * the display method in the client.
	 */
	ChatIF clientUI;

	private String loginID;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the chat client.
	 *
	 * @param loginID  The client's login ID.
	 * @param host     The server to connect to.
	 * @param port     The port number to connect on.
	 * @param clientUI The interface type variable.
	 */

	public ChatClient(String loginID, String host, int port, ChatIF clientUI)
			throws IOException {
		super(host, port); // Call the superclass constructor
		this.clientUI = clientUI;
		this.loginID = loginID;
		openConnection();
	}

	// Instance methods ************************************************
	@Override
	protected void connectionEstablished() {
		try {
			sendToServer("#login " + this.loginID);
		} catch (IOException e) {
			clientUI.display("ERROR: Could not send login command. Disconnecting...");
			quit();
		}
	}

	private String getLoginID() {
		return loginID;
	}

	/**
	 * This method handles all data that comes in from the server.
	 *
	 * @param msg The message from the server.
	 */
	public void handleMessageFromServer(Object msg) {
		clientUI.display(msg.toString());

	}

	/**
	 * This method handles all data coming from the UI
	 *
	 * @param message The message from the UI.
	 */
	public void handleMessageFromClientUI(String message) {
		if (message.startsWith("#")) {
			handleClientCommand(message.substring(1)); // Remove the cmd prefix (#)
		} else {
			try {
				sendToServer(message);
			} catch (IOException e) {
				clientUI.display("Could not send message to server. Terminating client.");
				quit();
			}
		}
	}

	/**
	 * This method handles client commands
	 * 
	 * @param command Command w/o the prefix (#)
	 */
	private void handleClientCommand(String command) {
		String[] cmdParts = command.trim().split("\\s+", 2);
		String cmd = cmdParts[0].toLowerCase();
		String arg  = cmdParts.length > 1 ? cmdParts[1].trim() : null; // if the command has an argument

		try {
			switch (cmd) {
				case "quit":
					clientUI.display("Client terminating...");
					quit();
					break;
				case "logoff":
					if (isConnected()) {
						closeConnection();
						clientUI.display("Logged off from server");
					} else {
						clientUI.display("ERROR: You are not connected to any server");
					}
					
					break;
				case "sethost":
					if (!isConnected()) {
						setHost(arg);
						clientUI.display("Set host to " + getHost());
					} else {
						clientUI.display("ERROR: Cannot set host while connected to a server. Please #logoff first");
					}

					break;
				case "setport": 
					if (!isConnected()) {
						try {
							setPort(Integer.parseInt(arg));
							clientUI.display("Set port to " + getPort());
						} catch (NumberFormatException e) {
							clientUI.display("ERROR: Invalid port number: " + arg);
						}
					} else {
						clientUI.display("ERROR: Cannot set port while connected to a server. Please #logoff first");
					}

					break;
				case "login": 
					if (!isConnected()) {
						openConnection();
						clientUI.display("Reconnecting to " + getHost() + " on port " + getPort());
					} else {
						clientUI.display("ERROR: You are already connected to a server");
					}
					
					break;
				case "gethost": 
					clientUI.display("Current host: " + getHost());
					
					break;
				case "getport": 
					clientUI.display("Current port: " + getPort());
					
					break;
				default: 
					clientUI.display("ERROR: Unknown command: #" + cmd);

					break;
			}
		} catch (IOException e) {
			clientUI.display("Error executing command: " + e.getMessage());
		}
	}

	@Override 
	protected void connectionClosed() {
		clientUI.display("Connection closed.");
	}

	@Override
	protected void connectionException(Exception exception) {
		clientUI.display("Connection to server lost. Terminating client.");
		quit();
	}

	/**
	 * This method terminates the client.
	 */
	public void quit() {
		try {
			closeConnection();
		} catch (IOException e) {
		}

		System.exit(0);
	}
}
// End of ChatClient class
