package bingo.shared;

import java.awt.*;
import javax.swing.*;

// Used by the game keeper to show its current status.
// Not thread-safe, so call updateStatus from the AWT thread. XXX
public class GameStatusLabel extends JLabel implements GameListener {
//public class GameStatusLabel extends Label implements GameListener {
    static protected String initialStatusString = 
	    "Watch this space for game status information.";

    public GameStatusLabel() {
	super(initialStatusString, LEFT);

        try {
            new GameListenerThread(this).start();
        } catch (java.io.IOException e) {
	    //XXX: what to do?
	    System.err.println("IOException when starting GameListenerThread.");
        }
    }

    public void updateStatus(String message) {
	setText(message);
    }

    //public Dimension getMaximumSize() {
	//Dimension d = getPreferredSize();
	//d.width = Short.MAX_VALUE;
	//return d;
    //}
}
