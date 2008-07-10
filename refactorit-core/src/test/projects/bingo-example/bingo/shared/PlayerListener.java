package bingo.shared;

// implement this interface if you are interested
// in finding out when new players register for a
// game
public interface PlayerListener extends Listener {
    public void updatePlayer(PlayerRecord p);
}
