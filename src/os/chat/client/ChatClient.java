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
import java.util.List;
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

    private Vector<String> messageBuffer = new Vector<>();

    /**
     * The graphical user interface, accessed through its interface. In return,
     * the GUI will use the CommandsFromWindow interface to call methods to the
     * ChatClient implementation.
     */
    private final CommandsToWindow window;


    private void reconnect(String roomName){
        boolean connected = false;

        while(!connected){
            try {
                // try to reconnect
                try {
                    this.ip = Inet4Address.getLocalHost().getHostAddress(); // Q6
                    // we get the registry and the skeleton for the client
                    registry = LocateRegistry.getRegistry(ip, 1099);
                    csm = (ChatServerManagerInterface) registry.lookup("ChatServerManager");
                } catch (RemoteException e) {
                    System.out.println("can not locate registry");
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    System.out.println("can not lookup for ChatServerManager");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("can not get local ip");
                    e.printStackTrace();
                }
                connected = true;
                System.out.println("Reconnected to the server.");
                // resend the message if there is any
                for (String m : messageBuffer) {
                    myRooms.get(roomName).publish(m, userName);
                }
                // clear after resending all the message
                messageBuffer.clear();

            }
            catch (Exception e)
            {
                System.out.println("Reconnection failed, retrying...");
            }
        }
    }


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

        //Q1, Q2, Q6, Q8
        // instantiate the skeleton and register it to the RMI registry
        try {
            this.ip = Inet4Address.getLocalHost().getHostAddress(); // Q6
            // we get the registry and the skeleton for the client
            registry = LocateRegistry.getRegistry(ip, 1099);
            skeleton = (CommandsFromServer) UnicastRemoteObject.exportObject(this, 0);
            csm = (ChatServerManagerInterface) registry.lookup("ChatServerManager");
        } catch (RemoteException e) {
            System.out.println("can not locate registry");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println("can not lookup for ChatServerManager");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("can not get local ip");
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
            System.out.println("can not call ChatServerInterface.publish()");
            // Q8
            // buffer the message
            messageBuffer.add(message);
            // attempt to reconnect to the server and send message
            reconnect(roomName);
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
    public boolean joinChatRoom(String roomName) {
        try {
            //Q2
            // register the client to the room
            myRooms.put(roomName, (ChatServerInterface) registry.lookup("room_" + roomName));
            myRooms.get(roomName).register(skeleton);
            return true;
        } catch (NotBoundException e) {
            System.out.println("can not lookup for room");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("can not call ChatServerInterface.register()");
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
            System.out.println("Cannot lookup for room");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("Error occurred while leaving chat room: " + e.getMessage());
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
            System.out.println("can not call ChatServerManagerInterface.createRoom()");
            e.printStackTrace();
        }
        catch (NotBoundException e) {
            System.out.println("can not lookup for room");
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
