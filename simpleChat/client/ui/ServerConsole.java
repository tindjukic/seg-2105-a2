package simpleChat.client.ui;

import java.io.*;
import java.util.Scanner;

import simpleChat.client.common.ChatIF;
import simpleChat.server.backend.EchoServer;

public class ServerConsole implements ChatIF {
    // Default Vars
    EchoServer server;
    Scanner fromConsole;

    // Constructor
    public ServerConsole(EchoServer server) {
        this.server = server;
        fromConsole = new Scanner(System.in);
    }

    // Main Methods
    public void accept() {
        try {
            String message;

            while (true) {
                message = fromConsole.nextLine();
                handleMessageFromServerUI(message);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console");
        }
    }

    public void handleMessageFromServerUI(String message) {
        if (message.startsWith("#")) {
            handleServerCommand(message.substring(1)); // Remove the cmd prefix (#)
        } else {
            String fullMessage = "SERVER MSG> " + message;

            display(fullMessage);
            server.sendToAllClients(fullMessage);
        }
    }

    public void display(String message) {
        System.out.println(message);
    }

    private void handleServerCommand(String command) {
        String[] cmdParts = command.trim().split("\\s+", 2);
		String cmd = cmdParts[0].toLowerCase();
		String arg  = cmdParts.length > 1 ? cmdParts[1].trim() : null; // if the command has an argument

        try {
            switch (cmd) {
                case "quit":
                    display("Server shutting down...");
                    server.sendToAllClients("SERVER MSG> Server is shutting down");
                    server.close();
                    System.exit(0);

                    break;
                case "stop":
                    if (server.isListening()) {
                        server.stopListening();
                        display("Server stopped listening for new clients");
                    } else {
                        display("ERROR: Server is already stopped");
                    }

                    break;
                case "close": 
                    if (server.isListening() || server.getNumberOfClients() > 0) {
                        server.sendToAllClients("SERVER MSG> Server is closing connections");
                        server.close();
                        display("Server stopped listening & disconnected all clients");
                    } else {
                        display("ERROR: Server is already closed/stopped & has no clients");
                    }

                    break;
                case "setport":
                    if (!server.isListening() && server.getNumberOfClients() == 0) {
                        try {
                            server.setPort(Integer.parseInt(arg));
                            display("Port set to " + server.getPort());
                        } catch (NumberFormatException e) {
                            display("ERROR: Invalid port number provided");
                        }
                    } else {
                        display("ERROR: Cannot set port while running or connected. Please #close first");
                    }

                    break;
                case "start": 
                    if (!server.isListening()) {
                        server.listen();
                        display("Server started listening for new clients on port " + server.getPort());
                    } else {
                        display("ERROR: Server is already listening");
                    }

                    break;
                case "getport":
                    display("Current port: " + server.getPort());
                    
                    break;
                default:
                    display("ERROR: Unknown server command: #" + cmd);

                    break;
            }
        } catch (IOException e) {
            display("Error executing command: " + e.getMessage());
        }
    }
}