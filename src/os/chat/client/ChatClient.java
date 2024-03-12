package os.chat.client;

import os.chat.server.ChatServer;
import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManager;
import os.chat.server.ChatServerManagerInterface;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;
import java.net.Inet4Address;

/**
 * This class implements a chat client that can be run locally or remotely to
 * communicate with a {@link ChatServer} using RMI.
 */
public class ChatClient implements CommandsFromWindow, CommandsFromServer
	{

	ChatServerManagerInterface csm;
	Registry registry;

	/**
	 * The name of the user of this client
	 */
	private String userName;

	//Q2
	private CommandsFromServer skeleton;
	private HashMap<String, ChatServerInterface> myRooms;

	/**
	 * The graphical user interface, accessed through its interface. In return,
	 * the GUI will use the CommandsFromWindow interface to call methods to the
	 * ChatClient implementation.
	 */
	private final CommandsToWindow window;

	/**
	 * Constructor for the <code>ChatClient</code>. Must perform the connection to the
	 * server. If the connection is not successful, it must exit with an error.
	 *
	 * @param window   reference to the GUI operating the chat client
	 * @param userName the name of the user for this client
	 * @since Q1
	 */
	public ChatClient(CommandsToWindow window, String userName) {
		this.window = window;
		this.userName = userName;
		myRooms = new HashMap<String, ChatServerInterface>();
		//System.err.println("TODO: implement ChatClient constructor and connection to the server");

		/*
		 * TODO implement constructor
		 */

		try
			{
			registry = LocateRegistry.getRegistry(1099);
			csm = (ChatServerManagerInterface)registry.lookup("ChatServerManager");

			}
		catch (RemoteException e)
			{
			System.out.println("can not locate registry");
			e.printStackTrace();
			}
		catch (NotBoundException e)
			{
			System.out.println("can not lookup for ChatServerManager");
			e.printStackTrace();
			}

		try
			{
			skeleton = (CommandsFromServer) UnicastRemoteObject.exportObject(
					this, 0);

			}
		catch (RemoteException e)
			{
			}
		}

	/*
	 * Implementation of the functions from the CommandsFromWindow interface.
	 * See methods description in the interface definition.
	 */

	/**
	 * Sends a new <code>message</code> to the server to propagate to all clients
	 * registered to the chat room <code>roomName</code>.
	 *
	 * @param roomName the chat room name
	 * @param message  the message to send to the chat room on the server
	 */
	public void sendText(String roomName, String message)
		{

		System.err.println("TODO: sendText is not implemented.");

		/*
		 * TODO implement the method to send the message to the server.
		 */
		}

	/**
	 * Retrieves the list of chat rooms from the server (as a {@link Vector}
	 * of {@link String}s)
	 *
	 * @return a list of available chat rooms or an empty Vector if there is
	 * none, or if the server is unavailable
	 * @see Vector
	 */
	public Vector<String> getChatRoomsList()
		{

		//System.err.println("TODO: getChatRoomsList is not implemented.");

		/*
		 * TODO implement the method to receive a list of available chat rooms from the server.
		 */

		//return null;

		try
			{
			return csm.getRoomsList();
			}
			//QUESTION : Exception or RemoteException?
		catch (Exception e)
			{
			System.out.println("can not call ChatServerManager.getRoomsList()");
			e.printStackTrace();
			return null;
			}
		}

	/**
	 * Join the chat room. Does not leave previously joined chat rooms. To
	 * join a chat room we need to know only the chat room's name.
	 *
	 * @param roomName the name (unique identifier) of the chat room
	 * @return <code>true</code> if joining the chat room was successful,
	 * <code>false</code> otherwise
	 */
	public boolean joinChatRoom(String roomName)
		{
		try
			{
				myRooms.put(roomName, (ChatServerInterface) registry.lookup("room_" + roomName));
				myRooms.get(roomName).register(skeleton);
			//ChatServerInterface server = (ChatServerInterface) registry.lookup("room_" + roomName);
			// chatServer.put(roomName, server);
			//server.register(skeleton);
			return true;
			}
		catch (NotBoundException e)
		{
			System.out.println("can not lookup for room");
			e.printStackTrace();
		}
		catch (RemoteException e)
			{
			return false;
			}


		System.err.println("TODO: joinChatRoom is not implemented.");

		/*
		 * TODO implement the method to join a chat room and receive notifications of new messages.
		 */

		return false;
		}

	/**
	 * Leaves the chat room with the specified name
	 * <code>roomName</code>. The operation has no effect if has not
	 * previously joined the chat room.
	 *
	 * @param roomName the name (unique identifier) of the chat room
	 * @return <code>true</code> if leaving the chat room was successful,
	 * <code>false</code> otherwise
	 */
	public boolean leaveChatRoom(String roomName)
		{
			try {
				myRooms.remove(roomName);
				ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("room_" + roomName);
				chatServer.unregister(skeleton);
				return true;
			} catch (NotBoundException e) {
				System.out.println("Cannot lookup for room");
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Error occurred while leaving chat room: " + e.getMessage());
				e.printStackTrace();
			}
		System.err.println("TODO: leaveChatRoom is not implemented.");

		/*
		 * TODO implement the method to leave a chat room and stop receiving notifications of new messages.
		 */

		return false;
		}

	/**
	 * Creates a new room named <code>roomName</code> on the server.
	 *
	 * @param roomName the chat room name
	 * @return <code>true</code> if chat room was successfully created,
	 * <code>false</code> otherwise.
	 */
	public boolean createNewRoom(String roomName)
		{


		System.err.println("TODO: createNewRoom is not implemented.");

		/*
		 * TODO implement the method to ask the server to create a new room (second part of the assignment only).
		 */

		return false;
		}

	/*
	 * Implementation of the functions from the CommandsFromServer interface.
	 * See methods description in the interface definition.
	 */

	/**
	 * Publish a <code>message</code> in the chat room <code>roomName</code>
	 * of the GUI interface. This method acts as a proxy for the
	 * {@link CommandsToWindow#publish(String chatName, String message)}
	 * interface i.e., when the server calls this method, the {@link
	 * ChatClient} calls the
	 * {@link CommandsToWindow#publish(String chatName, String message)} method
	 * of it's window to display the message.
	 *
	 * @param roomName the name of the chat room
	 * @param message  the message to display
	 */
	public void receiveMsg(String roomName, String message)
		{

		System.err.println("TODO: getName is not implemented.");
		/*
		 * TODO implement the method to allow server to publish message for client.
		 */
		}

	// This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.
	}
