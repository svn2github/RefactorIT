package bingo.game;

import java.util.Stack;
import bingo.shared.*;

class RingMaster implements States {

    private SocketGate socketGate;
    private GameParameters gameParameters;
    private NotaryPublic notary;
    private Roster roster;
    private Stack announcedBalls;

    private int gameNumber = 0;
    private int state = BEFOREINITIALIZATION;

    RingMaster() throws java.io.IOException {
	socketGate = new SocketGate();
	gameParameters = new GameParameters();
	notary = new NotaryPublic();
	roster = new Roster();
	announcedBalls = new Stack();
	announcedBalls.push(new BingoBall(BingoBall.FREE_SPACE));
    }

    GameParameters getGameParameters() {
	return gameParameters;
    }

    Roster getRoster() {
	return roster;
    }

    int getGameNumber() {
	return gameNumber;
    }

    synchronized void waitForFirstPlayer() {
	gameNumber++;
	state = WAITING;
	socketGate.sendGameStatusMessage(statusString());

        while (state == WAITING) {
            try {
               wait();
            } catch (InterruptedException e) {
            }
        }

        socketGate.sendGameStatusMessage("Beginning count down ... ");
    }

    synchronized void waitForGameToEnd() {
        while (gameInProgress()) {
            try {
               wait();
            } catch (InterruptedException e) {
            }
        }
    }

    boolean ready() {
	return (state == BEFOREINITIALIZATION) ? false : true;
    }

    boolean isCountingDown() {
	return (state == COUNTINGDOWN) ? true : false;
    }

    synchronized boolean isRegistering() {
	return (state == WAITING || state == COUNTINGDOWN) ? true : false;
    }

    boolean isCheckingForWinner() {
	return (state == CHECKINGFORWINNER) ? true : false;
    }

    synchronized boolean gameInProgress() {
	return (state == PLAYING || state == CHECKINGFORWINNER) ? true : false;
    }

    synchronized void startCountDown() {
	state = COUNTINGDOWN;
	notifyAll();
    }

    void startGame() {
	state = PLAYING;
	socketGate.sendGameStatusMessage("Game Starting...");
    }

    void setCheckingForWinner() {
	state = CHECKINGFORWINNER;
    }

    synchronized void setGameResumed() {
	if (state == CHECKINGFORWINNER)
	    state = PLAYING;
    }

    synchronized void setGameOver() {
	state = GAMEOVER;
        announceBall(new BingoBall(BingoBall.GAME_OVER));
	announcedBalls.removeAllElements();
	announcedBalls.push(new BingoBall(BingoBall.FREE_SPACE));
	roster.removeAllElements();
	notifyAll();
    }

    void signTheCards(Card[] cards) {
	try {
	    for (int i = 0; i < cards.length; i++)
	        notary.signTheCard(cards[i], gameNumber);
	} catch (Exception e) {
	    // PENDING: can't sign the cards, what to do?
	}
    }

    boolean verify(Card c) {

	int colMatches = 0;
	int rowMatches = 0;
	int diagMatches = 0;
	int otherDiagMatches = 0;

	if (!(notary.verifyTheSignature(c, gameNumber)))
	    return false;

	for (int i = 0; i < Card.SIZE; i ++) {
	    for (int j = 0; j < Card.SIZE; j ++) {
		if (announcedBalls.contains(c.boardValues[i][j]))
		    rowMatches ++;
		if (announcedBalls.contains(c.boardValues[j][i]))
		    colMatches ++;
		if ((i == j) && announcedBalls.contains(c.boardValues[i][j]))
		    diagMatches ++;
		if (((i + j) == (Card.SIZE-1)) && announcedBalls.contains(c.boardValues[j][i]))
		    otherDiagMatches ++;
	    }
	    if (colMatches == Card.SIZE) {
		return true;
	    } else if (rowMatches == Card.SIZE) {
		return true;
	    } else {
		rowMatches = 0;
		colMatches = 0;
	    }
	}
	if (diagMatches == Card.SIZE) {
	    return true;
	} else if (otherDiagMatches == Card.SIZE) {
	    return true;
	}

	return false;
    }

    void announceBall(BingoBall b) {
	socketGate.sendBall(b);
	socketGate.sendGameStatusMessage(statusString());
	announcedBalls.push(b);
    }

    void sendTimeRemainingMessage(int timeRemaining) {
	socketGate.sendGameStatusMessage(statusString(timeRemaining));
    }

    void sendPlayerStatusMessage(PlayerRecord p) {
	socketGate.sendPlayerStatusMessage(p);
    }

    String statusString() {
	return statusString(-1);
    }

    private String statusString(int anArgument) {
	switch (state) {
	case BEFOREINITIALIZATION:
	    return "The BINGO server isn't ready yet.";
	case WAITING:
	    return "Waiting for first player to register for game # " + gameNumber + ".";
	case COUNTINGDOWN:
	    return "Game # " + gameNumber + " starts in "
		   + anArgument + " seconds.";
	case PLAYING:
	    return "Game #" + gameNumber + " in progress. "
		   + announcedBalls.size() + " balls announced.";
	case CHECKINGFORWINNER:
	    return "Game paused while checking for winner."
		   + announcedBalls.size() + " balls announced.";
	case GAMEOVER:
	    return "Game over.";
	default:
	    return "Nuttin'.";
	}
    }
}
