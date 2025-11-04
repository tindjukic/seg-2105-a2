package simpleChat.client.ui;
// This file contains material supporting section 3.7 of the textbook:

// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.util.Scanner;

import simpleChat.client.backend.ChatClient;
import simpleChat.client.common.*;

/**
 * This class constructs the UI for a chat client. It implements the
 * chat interface in order to activate the display() method.
 * Warning: Some of the code here is cloned in ServerConsole
 *
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 */
public class ClientConsole implements ChatIF {
	// Class variables *************************************************

	/**
	 * The default port to connect on.
	 */
	final public static int DEFAULT_PORT = 5555;

	// Instance variables **********************************************

	/**
	 * The instance of the client that created this ConsoleChat.
	 */
	ChatClient client;

	/**
	 * Scanner to read from the console
	 */
	Scanner fromConsole;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the ClientConsole UI.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect on.
	 */
	public ClientConsole(String loginID, String host, int port) {
		try {
			client = new ChatClient(loginID, host, port, this);
		} catch (IOException exception) {
			System.out.println("Error: Can't setup connection!"
					+ " Terminating client.");
			System.exit(1);
		}

		// Create scanner object to read from console
		fromConsole = new Scanner(System.in);
	}

	// Instance methods ************************************************

	/**
	 * This method waits for input from the console. Once it is
	 * received, it sends it to the client's message handler.
	 */
	public void accept() {
		try {

			String message;

			while (true) {
				message = fromConsole.nextLine();
				client.handleMessageFromClientUI(message);
			}
		} catch (Exception ex) {
			System.out.println("Unexpected error while reading from console!");
		}
	}

	/**
	 * This method overrides the method in the ChatIF interface. It
	 * displays a message onto the screen.
	 *
	 * @param message The string to be displayed.
	 */
	public void display(String message) {
		System.out.println("> " + message);
	}

	// Class methods ***************************************************

	/**
	 * This method is responsible for the creation of the Client UI.
	 *
	 * @param args[0] The host to connect to.
	 */
	public static void main(String[] args) {
		String loginID = "";
		String host = "localhost";
		int port = DEFAULT_PORT;

		// Get the mandatory login arg
		try {
			loginID = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("ERROR: Login ID must be provided as the first argument");
			System.exit(1);
		}

		try {
			if (args.length > 1) {
				host = args[1];
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Don't need to do anaything since the host defaults to localhost
		}

		// Get the port from cmd line
		try {
			if (args.length > 2) {
				port = Integer.parseInt(args[2]);
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid port provided. Using default port " + DEFAULT_PORT);
			port = DEFAULT_PORT;
		} catch (ArrayIndexOutOfBoundsException e) {
			// Don't need to do anything since default port is already set (no port arg provided)
		}

		System.out.println("Attempting to connect with login ID " + loginID + " to host " + host + " on port " + port);

		ClientConsole chat = new ClientConsole(loginID, host, port);
		chat.accept(); // Wait for console data
	}
}
// End of ConsoleChat class
