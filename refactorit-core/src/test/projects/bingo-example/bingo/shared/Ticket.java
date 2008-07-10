package bingo.shared;

import java.io.Serializable;

public class Ticket implements Serializable {

    public static final int DENIED = -1;

    public String message;
    public Card[] cards = null;
    public int ID = DENIED;

    public Ticket(String registrationDenied) {
	message = registrationDenied;
    }

    public Ticket(String message, int ID, Card[] cards) {
	this.message = message;
	this.ID = ID;
	this.cards = cards;
    }
}
