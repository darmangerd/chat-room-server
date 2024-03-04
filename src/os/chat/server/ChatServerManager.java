package os.chat.server;

import os.chat.client.ChatClient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

/**
 * This class manages the available {@link ChatServer}s and available rooms.
 * <p>
 * At first you should not modify its functionalities but only export
 * them for being called by the {@link ChatClient}.
 * <p>
 * Later you will modify this to allow creating new rooms and
 * looking them up from the {@link ChatClient}.
 */
public class ChatServerManager implements ChatServerManagerInterface
	{

	/**
	 * NOTE: technically this vector is redundant, since the room name can also
	 * be retrieved from the chat server vector.
	 */
	private Vector<String> chatRoomsList;

	private Vector<ChatServer> chatRooms;

	//COMMENT
	private Registry registry;

	private static ChatServerManager instance = null;

	/**
	 * Constructor of the <code>ChatServerManager</code>.
	 * <p>
	 * Must register its functionalities as stubs to be called from RMI by
	 * the {@link ChatClient}.
	 */
	public ChatServerManager()
		{

		/* initialize variables */
		chatRoomsList = new Vector<String>();
		chatRooms = new Vector<ChatServer>();

		// initial: we create a single chat room and the corresponding ChatServer
		chatRooms.add(new ChatServer("sports"));
		chatRoomsList.add("sports");

		/*
		 * TODO register the server manager object as a "ChatServerManager" on the RMI registry
		 * so it can be called by clients.
		 */

		// create stub
		try
			{
			ChatServerManagerInterface stub = (ChatServerManagerInterface)UnicastRemoteObject.exportObject(
					this, 0);
			registry = LocateRegistry.getRegistry();
			registry.rebind("ChatServerManager", stub);
			}
		catch (RemoteException e)
			{
			System.out.println("can not export the object");
			e.printStackTrace();
			}
		System.out.println("ChatServerManager was created");

		}

	/**
	 * Retrieves the chat server manager instance. This method creates a
	 * singleton chat server manager instance if none was previously created.
	 *
	 * @return a reference to the singleton chat server manager instance
	 */
	public static ChatServerManager getInstance()
		{
		if (instance == null)
			instance = new ChatServerManager();

		return instance;
		}

	/**
	 * Getter method for list of chat rooms.
	 *
	 * @return a list of chat rooms
	 * @see Vector
	 */
	public Vector<String> getRoomsList()
		{
		return chatRoomsList;
		}

	/**
	 * Creates a chat room with a specified room name <code>roomName</code>.
	 *
	 * @param roomName the name of the chat room
	 * @return <code>true</code> if the chat room was successfully created,
	 * <code>false</code> otherwise.
	 */
	public boolean createRoom(String roomName)
		{

		System.err.println("server manager method createRoom not implemented.");

		/*
		 * TODO add the code to create a new room
		 */

		return false;
		}

	public static void main(String[] args)
		{
		try
			{
			LocateRegistry.createRegistry(1099);
			}
		catch (RemoteException e)
			{
			System.out.println("error: can not create registry");
			e.printStackTrace();
			}
		System.out.println("registry was created");
		getInstance();
		}

	}