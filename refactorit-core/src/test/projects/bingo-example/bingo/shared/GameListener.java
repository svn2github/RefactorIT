package bingo.shared;

// implement this interface if you are interested
// in getting updates as to the current status
// of the game keeper
public interface GameListener extends Listener {
    public void updateStatus(String message);
}
