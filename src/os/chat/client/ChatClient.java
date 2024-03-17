package os.chat.client;

import os.chat.server.ChatServer;
import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManagerInterface;

import java.net.Inet4Address;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class implements a chat client that can be run locally or remotely to
 * communicate with a {@link ChatServer} using RMI.
 */
public class ChatClient implements CommandsFromWindow, CommandsFromServer {

    ChatServerManagerInterface csm;
    Registry registry;

    /**
     * The name of the user of this client
     */
    private String userName;

    //Q2
    private CommandsFromServer skeleton;
    private HashMap<String, ChatServerInterface> myRooms;
    private String ip;

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
    public ChatClient(CommandsToWindow window, String userName, String ip) {
        this.window = window;
        this.userName = userName;
        myRooms = new HashMap<String, ChatServerInterface>();

        //Q1, Q2, Q6
        // instantiate the skeleton and register it to the RMI registry
        try {
            this.ip = Inet4Address.getLocalHost().getHostAddress(); // Q6
            // we get the registry and the skeleton for the client
            registry = LocateRegistry.getRegistry(ip, 1099);
            skeleton = (CommandsFromServer) UnicastRemoteObject.exportObject(this, 0);
            csm = (ChatServerManagerInterface) registry.lookup("ChatServerManager");

        } catch (RemoteException e) {
            System.out.println("(RemoteException) Cannot create the chat client");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println("(NotBoundException) Cannot create the chat client");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("(Exception) Cannot create the chat client");
            e.printStackTrace();
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
    public void sendText(String roomName, String message) {

        //Q3
        // method to send the message to the server
        try {
            // we call the publish method of the server
            myRooms.get(roomName).publish(message, userName);
        } catch (RemoteException e) {
            System.out.println("(RemoteException) Cannot send the text");
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the list of chat rooms from the server (as a {@link Vector}
     * of {@link String}s)
     *
     * @return a list of available chat rooms or an empty Vector if there is
     * none, or if the server is unavailable
     * @see Vector
     */
    public Vector<String> getChatRoomsList() {
        // method to receive a list of available chat rooms from the server.
        try {
            //Q2
            // return the list of chat rooms
            return csm.getRoomsList();
        }
        catch (RemoteException e) {
            System.out.println("(RemoteException) Cannot get the chat rooms list");
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
    public boolean joinChatRoom(String roomName) {
        try {
            //Q2
            // register the client to the room
            myRooms.put(roomName, (ChatServerInterface) registry.lookup("room_" + roomName));
            myRooms.get(roomName).register(skeleton);
            return true;
        } catch (NotBoundException e) {
            System.out.println("(NotBoundException) Cannot join the chat room");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("(RemoteException) Cannot join the chat room");
            e.printStackTrace();
        }
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
    public boolean leaveChatRoom(String roomName) {
        // method to leave a chat room and stop receiving notifications of new messages
        try {
            // we remove the room from the list of rooms
            myRooms.remove(roomName);
            ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("room_" + roomName);
            chatServer.unregister(skeleton);
            return true;
        } catch (NotBoundException e) {
            System.out.println("(NotBoundException) Cannot leave the chat room");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("(RemoteException) Cannot leave the chat room");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates a new room named <code>roomName</code> on the server.
     *
     * @param roomName the chat room name
     * @return <code>true</code> if chat room was successfully created,
     * <code>false</code> otherwise.
     */
    public boolean createNewRoom(String roomName) {
        //Q4
        // method to ask the server to create a new room
        try {
            if (csm.createRoom(roomName)) {
                // we add the room to the list of rooms
                myRooms.put(roomName, (ChatServerInterface) registry.lookup("room_" + roomName));

                //TODO : try to join the room by using the joinChatRoom method
                //this.joinChatRoom(roomName);

                return true;
            }
        } catch (RemoteException e) {
            System.out.println("(RemoteException) Cannot create a new room");
            e.printStackTrace();
        }
        catch (NotBoundException e) {
            System.out.println("(NotBoundException) Cannot create a new room");
            e.printStackTrace();
        }
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
    public void receiveMsg(String roomName, String message) {
        // method to allow server to publish message for client
        window.publish(roomName, message);
    }

    // This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.
}
