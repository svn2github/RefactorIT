package bingo.shared;

import java.net.*;
import java.io.*;
import javax.swing.SwingUtilities;

public class PlayerListenerThread extends ListenerThread {

    private PlayerListener notifyee;

    public PlayerListenerThread(PlayerListener notifyee) throws IOException {

	super(Constants.PlayerListeningGroup);
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
		final PlayerRecord p = new PlayerRecord(rcvd);
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
	                notifyee.updatePlayer(p);
		    }
		});
	    } catch (IOException e) {
		    // PENDING: what goes in here?
	    }
        }
    }
}
