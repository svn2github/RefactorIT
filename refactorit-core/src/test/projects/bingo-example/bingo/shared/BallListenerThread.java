package bingo.shared;

import java.net.*;
import java.io.*;

public class BallListenerThread extends ListenerThread {

    private BallListener notifyee;

    public BallListenerThread(BallListener notifyee)
        throws IOException
    {

	super(Constants.BallListeningGroup);
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
		BingoBall b = new BingoBall(rcvd);
		if (b.getNumber() == BingoBall.GAME_OVER) {
		    notifyee.noMoreBalls();
		} else
		    notifyee.ballCalled(b);

	    } catch (IOException e) {
		    // PENDING: what goes in here?
	    }
        }
    }
}
