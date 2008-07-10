package bingo.shared;

import java.net.*;
import java.io.*;

public class GameListenerThread extends ListenerThread {

    private GameListener notifyee;

    public GameListenerThread(GameListener notifyee) throws IOException {

	super(Constants.GameListeningGroup);
	this.notifyee = notifyee;
    }

    public synchronized void run() {
	DatagramPacket packet;

        while (stopListening == false) {
	    byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, 256);
	    try {
                socket.receive(packet);
		byte[] rcvd = packet.getData();
		String dataString = new String(rcvd);
		notifyee.updateStatus(dataString);
	    } catch (IOException e) {
		    // PENDING: what goes in here?
	    }
        }
    }
}
