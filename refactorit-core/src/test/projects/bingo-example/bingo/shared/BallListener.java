package bingo.shared;

import java.net.InetAddress;

public interface BallListener extends Listener {
    public void ballCalled(BingoBall b);
    public void noMoreBalls();
}
