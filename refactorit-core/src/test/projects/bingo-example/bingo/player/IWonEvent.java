package bingo.player;

import bingo.shared.*;
import java.awt.*;

/** Event posted when the user claims to win a game. */
class IWonEvent extends AWTEvent {
    private CardWindow cardWindow;
    private static int IWONEVENT_ID = AWTEvent.RESERVED_ID_MAX + 1;
    //private static int IWONEVENT_ID = 0;

    public IWonEvent(Object source, CardWindow cardWindow) {
	super(source, IWONEVENT_ID); 
	this.cardWindow = cardWindow;
    }

    public CardWindow getCardWindow() {
	if (Player.DEBUG) {
	    System.out.println("IWonEvent getCardWindow method called");
	}
	return cardWindow;
    }

    public Card getCard() {
	if (Player.DEBUG) {
	    System.out.println("IWonEvent getCard method called");
	}
	return cardWindow.getCard();
    }
}
