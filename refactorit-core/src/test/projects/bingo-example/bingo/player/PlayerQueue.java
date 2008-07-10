package bingo.player;

import bingo.shared.*;

import java.rmi.*;
import java.awt.*;
import java.awt.event.*;

class PlayerQueue extends EventQueue {

    private Player player;

    public PlayerQueue(Player player) {
	super();
	this.player = player;
    }

    /**
     * As of 1.2, this method gets called from the event-handling thread
     * that's dedicated to this PlayerQueue.  (In 1.1, this method doesn't
     * exist in EventQueue, and the thread calls event.src.dispatchEvent
     * instead.)
     */
    protected void dispatchEvent(AWTEvent event) {
	//XXX Duplicate 1.1 behavior (to avoid duplicating code).
	//XXX If this app is 1.2 only, then call player.handleIWonEvent
	//XXX here.
	//XXX (Or maybe we should perform a 1.2 vs. 1.1 test instead?)
	player.dispatchEvent(event); //duplicate 1.1 behavior (to avoid
				     //duplicating code)
    }
}
