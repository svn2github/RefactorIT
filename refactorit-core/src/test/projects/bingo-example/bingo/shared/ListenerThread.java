package bingo.shared;

import java.net.*;
import java.io.*;

public abstract class ListenerThread extends Thread {

    boolean stopListening = false;
    MulticastSocket socket;

    private InetAddress group;
    private String groupString;

    public ListenerThread(String groupString)
        throws UnknownHostException, IOException
    {
	super();

	this.groupString = groupString;

        this.group = InetAddress.getByName(groupString);
        socket = new MulticastSocket(Constants.portNumber);
	socket.joinGroup(group);
    }

    public void stopListening() throws IOException{
	stopListening = true;
	socket.leaveGroup(group);
	socket.close();
    }
}
