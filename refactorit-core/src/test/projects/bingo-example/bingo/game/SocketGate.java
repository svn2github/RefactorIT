package bingo.game;

import java.net.*;
import bingo.shared.*;

class SocketGate implements Constants {

    private InetAddress ballListeningGroup;
    private InetAddress playerListeningGroup;
    private InetAddress gameListeningGroup;

    private MulticastSocket socket = null;

    SocketGate () throws java.io.IOException {

        socket = new MulticastSocket(Constants.portNumber);

        ballListeningGroup = InetAddress.getByName(Constants.BallListeningGroup);
        playerListeningGroup = InetAddress.getByName(Constants.PlayerListeningGroup);
        gameListeningGroup = InetAddress.getByName(Constants.GameListeningGroup);
    }

    void sendBall(BingoBall b) {
        sendBytes(b.getBytes(), ballListeningGroup);
    }

    void sendPlayerStatusMessage(PlayerRecord p) {
        sendBytes(p.getBytes(), playerListeningGroup);
    }

    void sendGameStatusMessage(String msg) {
        sendBytes(msg.getBytes(), gameListeningGroup);
    }

    private void sendBytes(byte[] data, InetAddress group) {
        DatagramPacket packet = new DatagramPacket(data, data.length, group,
                                                   Constants.portNumber);
        try {
            socket.send(packet);
        } catch (java.io.IOException e) {
            // PENDING: what should go in here?
        }
    }
}
