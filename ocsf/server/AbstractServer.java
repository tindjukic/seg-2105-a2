// This file contains material supporting section 3.8 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source

package ocsf.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
* The <code> AbstractServer </code> class maintains a thread that waits
* for connection attempts from clients. When a connection attempt occurs
* it creates a new <code> ConnectionToClient </code> instance which
* runs as a thread. When a client is thus connected to the
* server, the two programs can then exchange <code> Object </code>
* instances.<p>
*
* Method <code> handleMessageFromClient </code> must be defined by
* a concrete subclass. Several other hook methods may also be
* overriden.<p>
*
* Several public service methods are provided to applications that use
* this framework, and several hook methods are also available<p>
*
* Project Name: OCSF (Object Client-Server Framework)<p>
*
* @author Dr Robert Lagani&egrave;re
* @author Dr Timothy C. Lethbridge
* @author Fran&ccedil;ois B&eacute;langer
* @author Paul Holden
* @version September 2025 (2.14)
* @see ocsf.server.ConnectionToClient
*/

public abstract class AbstractServer implements Runnable
{
	// INSTANCE VARIABLES *********************************************

	/**
	 * The server socket: listens for clients who want to connect.
	 */
	private ServerSocket serverSocket = null;

	/**
	 * The connection listener thread.
	 */
	private Thread connectionListener;

	/**
	 * The port number
	 */
	private int port;

	/**
	 * The server timeout while for accepting connections.
	 */
	private int timeout = 500;

	/**
	 * The maximum queue length.
	 */
	private int backlog = 10;

	/**
	 * A thread-safe map of active client connections.
	 */
	private ConcurrentHashMap<Long, ConnectionToClient> clientConnections;
	

	/**
	 * Counter for assigning unique IDs to clients.
	 */
	private long clientIdCounter = 0;

	/**
	 * Indicates if the listening thread is ready to stop.
	 */
	private boolean readyToStop = false;


	// CONSTRUCTOR ******************************************************

	public AbstractServer(int port)
	{
		this.port = port;
		this.clientConnections = new ConcurrentHashMap<>();
	}


	// INSTANCE METHODS *************************************************

	final public void listen() throws IOException
	{
		if (!isListening())
		{
			if (serverSocket == null)
			{
				serverSocket = new ServerSocket(getPort(), backlog);
			}

			serverSocket.setSoTimeout(timeout);
			readyToStop = false;
			connectionListener = new Thread(this);
			connectionListener.start();
		}
	}

	final public void stopListening()
	{
		readyToStop = true;
	}

	final synchronized public void close() throws IOException
	{
		if (serverSocket == null)
			return;
		stopListening();
		try
		{
			serverSocket.close();
		}
		finally
		{
			// Close the client sockets of the already connected clients
			for (ConnectionToClient client : clientConnections.values())
			{
				try
				{
					client.close();
				}
				catch(Exception ex) {}
			}
			clientConnections.clear();
			serverSocket = null;
			serverClosed();
		}
	}

	public void sendToAllClients(Object msg)
	{
		for (ConnectionToClient client : clientConnections.values())
		{
			try
			{
				client.sendToClient(msg);
			}
			catch (Exception ex) {}
		}
	}


	// ACCESSING METHODS ------------------------------------------------

	final public boolean isListening()
	{
		return (connectionListener != null);
	}

	synchronized final public ConnectionToClient[] getClientConnections()
	{
		return clientConnections.values().toArray(new ConnectionToClient[0]);
	}

	final public int getNumberOfClients()
	{
		return clientConnections.size();
	}

	final public int getPort()
	{
		return port;
	}

	final public void setPort(int port)
	{
		this.port = port;
	}

	final public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	final public void setBacklog(int backlog)
	{
		this.backlog = backlog;
	}


	// RUN METHOD -------------------------------------------------------

	final public void run()
	{
		serverStarted();

		try
		{
			while(!readyToStop)
			{
				try
				{
					Socket clientSocket = serverSocket.accept();

					synchronized(this)
					{
						ConnectionToClient client = new ConnectionToClient(clientSocket, this);
						long id = ++clientIdCounter;
						clientConnections.put(id, client);
					}
				}
				catch (InterruptedIOException exception)
				{
					// timeout occurred
				}
			}

			serverStopped();
		}
		catch (IOException exception)
		{
			if (!readyToStop)
			{
				listeningException(exception);
			}
			else
			{
				serverStopped();
			}
		}
		finally
		{
			readyToStop = true;
			connectionListener = null;
		}
	}


	// METHODS DESIGNED TO BE OVERRIDDEN --------------------------------

	protected void clientConnected(ConnectionToClient client) {}

	synchronized protected void clientDisconnected(ConnectionToClient client) {
		// Since we don't track which ID belongs to this client directly,
		// remove by value.
		clientConnections.values().remove(client);
	}

	synchronized protected void clientException(ConnectionToClient client, Throwable exception) {}

	protected void listeningException(Throwable exception) {}

	protected void serverStarted() {}

	protected void serverStopped() {}

	protected void serverClosed() {}

	protected abstract void handleMessageFromClient(Object msg, ConnectionToClient client);


	// METHODS TO BE USED FROM WITHIN THE FRAMEWORK ONLY ----------------

	final synchronized void receiveMessageFromClient(Object msg, ConnectionToClient client)
	{
		this.handleMessageFromClient(msg, client);
	}
}
// End of AbstractServer Class
