package os.chat.server;

import os.chat.client.CommandsFromServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Vector;

/**
 * Each instance of this class is a server for one room.
 * <p>
 * At first there is only one room server, and the names of the room available
 * is fixed.
 * <p>
 * Later you will have multiple room server, each managed by its own
 * <code>ChatServer</code>. A {@link ChatServerManager} will then be responsible
 * for creating and adding new rooms.
 */
public class ChatServer implements ChatServerInterface
	{

	private String roomName;
	private Vector<CommandsFromServer> registeredClients;

	/**
	 * Constructs and initializes the chat room before registering it to the RMI
	 * registry.
	 *
	 * @param roomName the name of the chat room
	 */
	public ChatServer(String roomName)
		{
		this.roomName = roomName;
		this.registeredClients = new Vector< CommandsFromServer >();

		//Q2
		// bind the chat server to the RMI registry
		try
			{
				// retrieve the RMI registry and add the skeleton to it
				Registry registry = LocateRegistry.getRegistry("localhost", 1099);
				ChatServerInterface skeleton = (ChatServerInterface)UnicastRemoteObject.exportObject(
					this, 0);

				registry.rebind("room_" + roomName, skeleton);
			}
		catch (RemoteException e)
			{
			System.out.println("(RemoteException) Cannot create the chat server");
			e.printStackTrace();
			}
		}

	/**
	 * Publishes to all subscribed clients (i.e. all clients registered to a
	 * chat room) a message send from a client.
	 *
	 * @param message   the message to propagate
	 * @param publisher the client from which the message originates
	 */
	public void publish(String message, String publisher)
		{
			//Q3,Q5
			// method to send the message to all registered clients

			// use iterator to avoid concurrency problems (because we are modifying the list while iterating over it)
			Iterator<CommandsFromServer> iterator = registeredClients.iterator();
			while(iterator.hasNext()){
				CommandsFromServer client = iterator.next();
				try
				{
					client.receiveMsg(roomName, publisher + ": " + message);
					System.out.println("publishing '" + message + "' from '" + publisher + "'");
				}
				catch (RemoteException e)
				{
					iterator.remove();;
					System.out.println("(RemoteException) Cannot send message to client");
				}
			}
		}

	/**
	 * Registers a new client to the chat room.
	 *
	 * @param client the name of the client as registered with the RMI
	 *               registry
	 */
	public void register(CommandsFromServer client)
		{
		this.registeredClients.add(client);
		//print console message
		System.out.println("client " + client + " has been registered to the chat room " + roomName);
		}

	/**
	 * Unregisters a client from the chat room.
	 *
	 * @param client the name of the client as registered with the RMI
	 *               registry
	 */
	public void unregister(CommandsFromServer client)
		{
		this.registeredClients.remove(client);
		System.out.println("client " + client + " has been unregister to the chat room " + roomName);
		}

	public static void main(String[] args)
		{
			// empty main method
		}
	}
