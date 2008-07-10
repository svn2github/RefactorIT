package bingo.game;

import java.rmi.*;
import java.rmi.server.*;
import java.util.Random;
import bingo.shared.*;

class RegistrarImpl extends UnicastRemoteObject implements Registrar, Constants
{
    private RingMaster ringMaster;
    private GameParameters gameParameters;
    private Roster roster;

    RegistrarImpl(RingMaster ringMaster) throws RemoteException {
	super();
	this.ringMaster = ringMaster;
	gameParameters = ringMaster.getGameParameters();
	roster = ringMaster.getRoster();
    }

    public String whatsHappening() {
	return ringMaster.statusString();
    }

    public Ticket mayIPlay(String playerName, int numCards, long seed)
                                   throws RemoteException
    {

	if (!ringMaster.ready())
	    return new Ticket("BINGO server not ready. You can't play.");
	else if (!ringMaster.isRegistering())
	    return new Ticket("Registration not open. You can't play.");
	    
	if (numCards > gameParameters.getMaxCards())
	    numCards = gameParameters.getMaxCards();

	synchronized (roster) {
	    if (roster.size() == gameParameters.getMaxPlayers())
	        return new Ticket("Game full. You can't play.");

	    Card[] cards = new Card[numCards];
            Random generator = new Random(seed);

	    for (int i = 0; i < numCards; i ++)
	        cards[i] = new Card(generator);

	    ringMaster.signTheCards(cards);

	    PlayerRecord p = new PlayerRecord(roster.nextPlayerID(), playerName, numCards);
	    String welcomeMessage = "Welcome to game # " + ringMaster.getGameNumber() + ".";
	    Ticket ticket = new Ticket(welcomeMessage, p.ID, cards);
	    roster.addElement(p, ringMaster);
	    return ticket;
	}
    }

    public Answer BINGO(int playerID, Card c) throws RemoteException {

	PlayerRecord p = roster.searchForPlayerWithID(playerID);

	if (p == null)
	    return new Answer(false, "Can't find player with ID: " + playerID + ".");

	if (p.wolfCries >= MAX_WOLF_CRIES)
	    return new Answer(false, "Sorry, wolf cryer, you're out of the game.");

	synchronized (ringMaster) {

	    ringMaster.setCheckingForWinner();

	    if (ringMaster.verify(c)) {

	        ringMaster.setGameOver();
	        return new Answer(true, "You won! Congratulations!");

	    } else {

		p.wolfCries++;
	        ringMaster.setGameResumed();
	        ringMaster.sendPlayerStatusMessage(p);
		if (p.wolfCries == MAX_WOLF_CRIES) {
	            return new Answer(false, "You've cried wolf 3 times. You're out.");
		} else {
	            return new Answer(false, "You cried wolf..." +
				      (MAX_WOLF_CRIES - p.wolfCries) +
				      " more and you're out.");
		}
	    }
	}
    }
}
